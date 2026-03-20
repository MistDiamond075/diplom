import {sounds, defaultStates, Actions} from "./videocall/constants.js";
import  {FeedManager} from "./videocall/feedmanager.js";
import {UiManager} from "./videocall/UiManager.js";
import {PushToTalkManager} from "./videocall/pushtotalk.js";
import {JanusManager} from "./videocall/janusmanager.js";
import {VideoCallUtils} from "./videocall/utils.js";
import {CONFIG} from "./videocall/config.js";

let janus = null;
const feedManager = new FeedManager();
const pushToTalkManager = new PushToTalkManager();
const janusManager = new JanusManager(feedManager,pushToTalkManager);

let isLeaving = false;
let isSoundMuted = false;
let localMediaStream = null;
let devices_start_state_updated = false;
let isDemonstrationActive = false;
let ws;
let debugDisplayReplacement=false;

function connectToVideocallWs(room_id, user_id, videoroomHandle) {
    const ws_addr = CONFIG.ws;
    ws = new WebSocket(ws_addr);
    console.log('server WS started');

    ws.onopen = function () {
        const request = {
            event: "joined",
            eventType: "videocall",
            roomId: room_id,
            userId: user_id
        };
        ws.send(JSON.stringify(request));
    }

    ws.onmessage = async function (event) {
        const jsdata = JSON.parse(event.data);
        console.log(jsdata);
        if (jsdata.eventType === "videocall") {
            if (jsdata.event === "ping") {
                ws.send(JSON.stringify({event: "pong", eventType: "videocall", userId: user_id}));
            } else if (jsdata.event === "connected") {
                const participants = jsdata.users;
                participants.forEach(participant => {
                    const feedId=feedManager.get(feedManager.MapKey.USER, participant.id);
                    UiManager.createUserParticipantBlock(participant,feedId);
                    UiManager.updateUserDisplay(feedManager.get(feedManager.MapKey.USER, participant.id), participant.camera === defaultStates.ON);
                });
                const messages = jsdata.messageArray;
                messages.forEach(msg => {
                    addMessageToChat(msg, user_id);
                });
                await sounds.JOIN.play();
            } else if (jsdata.event === "disconnected") {
                if (jsdata.forced) {
                    leave(true);
                    return;
                }
                unsubscribeFromPublisher(subscriberHandle.get(
                    feedManager.get(feedManager.MapKey.USER, jsdata.id)
                ));
                document.querySelector(`#user_${jsdata.id}`)?.remove();
                await sounds.LEAVE.play();
            } else if (jsdata.event === "chatmsg") {
                addMessageToChat(jsdata, user_id);
            } else if (jsdata.event === "configure") {
                const message = jsdata.data;
                if (jsdata.self) {
                    if (jsdata.type === "janus") {
                        videoroomHandle.send(
                            message
                        );
                        let track;
                        let state;
                        if (message.message.audio !== undefined) {
                            track = localMediaStream.getAudioTracks()[0];
                            state = message.message.audio;
                        } else if (message.message.video !== undefined) {
                            track = localMediaStream.getVideoTracks()[0];
                            state = message.message.video;
                        }
                        if (track !== undefined && state !== undefined) {
                            track.enabled = state;
                        }
                    } else if (jsdata.type === "other") {
                        if (jsdata.data.message.sound !== undefined) {
                            console.log('muted');
                            isSoundMuted = jsdata.data.state !== defaultStates.ON;
                            feedManager.userId_feedId.forEach(value => {
                                const element = document.querySelector(`#${value}_audio`);
                                console.log(element);
                                console.log(isSoundMuted);
                                if (element) {
                                    element.muted = isSoundMuted;
                                }
                            });
                        } else if (jsdata.data.message.demonstration !== undefined) {
                            if (jsdata.data.state !== defaultStates.MUTED_BY_ADMIN) {
                                if (jsdata.data.state === defaultStates.ON || jsdata.data.state === defaultStates.OFF && isDemonstrationActive) {
                                    await ScreenSharing(videoroomHandle, jsdata.data.state === defaultStates.ON);
                                }
                            }
                        }
                    }
                    if (jsdata.data.message.video !== undefined) {
                        UiManager.setControlButtonIcon(jsdata.data.state, 'camstate');
                    } else if (jsdata.data.message.audio !== undefined) {
                        UiManager.setControlButtonIcon(jsdata.data.state, 'microstate');
                    } else if (jsdata.data.message.sound !== undefined) {
                        UiManager.setControlButtonIcon(jsdata.data.state, 'soundstate');
                    } else if (jsdata.data.message.demonstration !== undefined) {
                        UiManager.setControlButtonIcon(jsdata.data.state, 'demostate');
                    }
                } else {
                    const userId = jsdata.userId;
                    const participant = document.querySelector(`#user_${userId}`);
                    console.log(userId);
                    console.log(participant);
                    if (participant) {
                        if (jsdata.data.message.video !== undefined) {
                            UiManager.updateIcon(Actions.CAMERA,jsdata.data.state,participant);
                           await manageActiveFeeds(userId,jsdata);
                        } else if (jsdata.data.message.audio !== undefined) {
                            UiManager.updateIcon(Actions.MICROPHONE,jsdata.data.state,participant);
                        } else if (jsdata.data.message.sound !== undefined) {
                            UiManager.updateIcon(Actions.SOUND,jsdata.data.state,participant);
                        } else if (jsdata.data.message.demonstration !== undefined) {
                            UiManager.updateIcon(Actions.DEMONSTRATION,jsdata.data.state,participant);
                            if (jsdata.data.state === defaultStates.ON) {
                                sounds.DEMOSTART.play().catch(err => console.warn('Autoplay block?', err));
                            } else if (jsdata.data.state === defaultStates.OFF) {
                                sounds.DEMOEND.play().catch(err => console.warn('Autoplay block?', err));
                            }
                           await manageActiveFeeds(userId,jsdata);
                        }
                    }
                }
            }
        }
    }

    ws.onclose = function () {
        const request = {
            event: "leave",
            eventType: "videocall",
            roomId: room_id,
            userId: user_id
        };
        ws.send(JSON.stringify(request));
    }
}

async function manageActiveFeeds(userId, jsdata) {
    const feedId = feedManager.get(feedManager.MapKey.USER, userId);
    if (feedId) {
        if (jsdata.data.state !== defaultStates.ON && feedManager.isActive(feedId)) {
            feedManager.removeActive(feedId);
            if (feedManager.checkActiveMax('lt')) {
                const users = document.querySelectorAll('[class*="user-participant"]');
                users.forEach(user => {
                    if (feedManager.checkActiveMax('gte')) {
                        return;
                    }
                    const state = VideoCallUtils.getParticipantSettingState(user, 'cam');
                    console.log(state);
                    if (state !== null) {
                        if (VideoCallUtils.parseDefaultState(state) === defaultStates.ON) {
                            toggleVideo(feedId, true);
                            feedManager.addActive(feedId);
                        }
                    }
                });
            }
        }
        if (feedManager.checkActiveMax('lt') || feedManager.isActive(feedId)) {
            if (!feedManager.isActive(feedId)) {
                feedManager.addActive(feedId);
            }
            console.log('TOGGLING DEMO WITH REWUEST');
            await toggleVideo(feedId, jsdata.data.state === defaultStates.ON);
        }
    }
}

function addMessageToChat(msg, userId) {
    const container = document.querySelector('.chat-block');
    if (!document.querySelector(`#message_${msg.id}`)) {
        const div = document.createElement('div');
        div.id = 'message_' + msg.id;
        div.className = 'chat-message';
        container.appendChild(div);
        const span_time = document.createElement('span');
        span_time.className = 'chat-message-time';
        const date = new Date(msg.timestamp);
        span_time.innerText = formatLeadingZero(date.getHours()) + ':' + formatLeadingZero(date.getMinutes()) + ' ';
        const span_name = document.createElement('span');
        span_name.className = 'chat-message-username';
        span_name.style['color'] = generateNameColor('#2e2e2e');
        span_name.innerText = getUserCredentials(msg) + ': ';
        div.appendChild(span_time);
        div.appendChild(span_name);
        const span_text = document.createElement('span');
        span_text.className = 'chat-message-text';
        let messageText = msg.text;
        if (msg.replyToId !== null && msg.replyToName !== null) {
            console.log(msg.replyToId === userId);
            if (msg.replyToId === userId) {
                const userName = msg.replyToName;
                const bgcolor = '#16b919';
                const textcolor = 'black';
                console.log(messageText);
                const pattern = new RegExp(`@${userName}\\b`, 'gi');
                messageText = messageText.toString().replace(pattern, `<span style="background:${bgcolor};color:${textcolor};padding: 1px;font-weight: bold;border-radius: 3px">@${userName}</span>`);
                console.log(messageText);
            }
        }
        span_text.innerHTML = messageText;
        div.appendChild(span_text);
    }
}

async function updateUserSettings(status, action, self, userId = null) {
    let state;
    console.log('status: ' + status, VideoCallUtils.isDefaultState(status));
    if (VideoCallUtils.isDefaultState(status)) {
        state = status;
    } else {
        state = status !== null ? (status ? 'ON' : 'OFF') : status;
    }
    let url = `${window.location.href}/user/update?`;
    const searchParams = new URLSearchParams({action: action.toUpperCase(), self: self});
    if (userId !== null) {
        searchParams.append('userUpdatedId', userId);
    }
    if (state !== null) {
        searchParams.append('state', state);
    }
    url += searchParams;
    console.log(url);
    const response = await fetch(url, {
        method: 'post',
        headers: {[csrfHeader]: csrfToken}
    });
    if (!response.ok) {
        showInfoMessage('Ошибка применения настроек');
    }else{
        const data = await response.json();
        console.log(data);
        if (self) {
                isSoundMuted = data !== 'ON';
        }
    }
}

async function sendMessageToChat() {
    const text = document.querySelector('#message_input').value;
    if (text === '' || !text) {
        console.error('message cannot be empty');
    }
    let replyTo = null;
    if (text.includes("@")) {
        const name = text.substring(text.indexOf('@') + 1, text.indexOf(' ', text.indexOf('@') + 1));
        console.log(name);
        const users = document.querySelectorAll('.user-participant');
        users.forEach(user => {
            console.log(user);
            if (user.getAttribute('name').toString().includes(name)) {
                replyTo = user.id.substring(user.id.indexOf('_') + 1);
            }
        });
    }
    const senddate = {
        text: text,
        replyTo: replyTo
    };
    const response = await fetch(window.location.href + '/addMessage', {
        method: 'post',
        headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
        body: JSON.stringify(senddate)
    });
    let data = await response.json();
    if (!response.ok) {
        const msg = await data;
        showInfoMessage(msg.message)
    } else {
        console.log(data);
        document.querySelector('#message_input').value = '';
    }
}

async function join() {
    const confirmed = await UiManager.createDialogWindow();
    if (!confirmed) {
        window.location.href = '/conferences';
        return;
    }
    const response = await fetch(window.location.href + '/join', {
        method: 'get'
    });
    if (!response.ok) {
        const msg = await response.json();
        showInfoMessage(msg.message);
        return;
    }
    await init();

    async function init() {
        const response = await fetch(window.location.href + '/user/getData', {
            method: 'get'
        });
        if (!response.ok) {
            console.log('error');
            return;
        }
        try {
            const data = await response.json();
            const roomId = data.videocallsId.roomId;
            let username = data.videocalluserId.id.toString();
            let user_id = data.videocalluserId.id;
            let microstate = data.microstate ? data.microstate : false;
            let camerastate = data.camstate ? data.camstate : false;
            console.log(data);
            const opaqueId = `videoroom-${roomId}`;
            if (!devices_start_state_updated) {
                UiManager.setControlButtonIcon(data.soundstate, 'soundstate');
                UiManager.setControlButtonIcon(data.demostate, 'demostate');
                UiManager.setControlButtonIcon(microstate, 'microstate');
                UiManager.setControlButtonIcon(camerastate, 'camstate');
            }
            Janus.init({
                //  debug: "all",
                callback: function () {
                    janusManager.start(roomId, username, opaqueId, VideoCallUtils.parseDefaultState(microstate), VideoCallUtils.parseDefaultState(camerastate), user_id);
                }
            });
        }catch (e) {
            console.error(e);
        }
    }
}



function subscribe(publisher){
    feedManager.add(feedManager.MapKey.USER, Number(publisher.display), publisher.id);
    feedManager.add(feedManager.MapKey.FEED, publisher.id, Number(publisher.display));
    if (feedManager.checkActiveMax('lt') || feedManager.isActive(publisher.id)) {
        console.log('TOGGLING VIDEO ' + publisher.id);
        subscribeToPublisher(publisher.id, true);
        feedManager.addActive(publisher.id);
    } else {
        subscribeToPublisher(publisher.id, false);
    }
}

async function toggleVideo(feedId, visible) {
    console.log(getCallerFunctionName());
    const handle = subscriberHandle.get(feedId);
    console.log(feedId, handle);
    const videoElement = document.querySelector(`#${feedId}_video`);
    console.log(videoElement);
    if (handle?.remoteStreams?.video) {
        console.log('--------------------------------------------------------------' + visible);
        UiManager.updateUserDisplay(feedId, visible);
        const tracks = handle?.remoteStreams?.video.srcObject?.getVideoTracks();
        console.log(tracks);
        tracks?.forEach(track => track.enabled = visible);
        if (visible) {
            try {
                await videoElement?.play();
                console.log("✅ Видео воспроизводится");
            } catch (e) {
                console.error("❌ Не удалось запустить видео: ",e);
                showInfoMessage("Ошибка воспроизведения видео");
            }
        }
    } else {
        UiManager.updateUserDisplay(feedId, false);
    }
}

function createDummyVideoTrack() {
    const canvas = document.createElement("canvas");
    canvas.width = 640;
    canvas.height = 480;

    const ctx = canvas.getContext("2d");
    ctx.fillStyle = "black";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    const stream = canvas.captureStream(1);
    return stream.getVideoTracks()[0];
}

async function publishOwnFeed(videoroomHandle, user_id) {
    try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        const hasAudio = devices.some(device => device.kind === 'audioinput');
        const hasVideo = devices.some(device => device.kind === 'videoinput');

        if (!hasAudio && !hasVideo) {
            showInfoMessage("Нет доступных устройств");
            console.error("NO AVAILABLE DEVICES FOUND");
            return;
        }

        const constraints = {
            audio: hasAudio,
            video: hasVideo ? {frameRate: 30} : false
        };

        let stream = await navigator.mediaDevices.getUserMedia(constraints);
        if (!hasVideo) {
            console.warn("No camera found → creating dummy video track");
            const dummyTrack = createDummyVideoTrack();
            const newStream = new MediaStream();
            stream.getTracks().forEach(t => newStream.addTrack(t));
            newStream.addTrack(dummyTrack);

            stream = newStream;
        }
        localMediaStream = stream;
        Janus.attachMediaStream(document.querySelector("#video_display_own"), stream);

        const audioTrack = stream.getAudioTracks()[0];
        const audioLevel = 40;
        console.warn('video enabled: ' + hasVideo + '\taudio enabled:' + hasAudio);
        console.log("STREAM TRACKS:", stream.getTracks());
        try {
            const jsep = await createOffer(videoroomHandle, {
                tracks: [
                    {type: "audio", capture: stream.getAudioTracks()[0]},
                    {type: "video", capture: stream.getVideoTracks()[0]}
                ],
                media: {
                    audioRecv: false,
                    videoRecv: false
                }
            });
            const publish = {
                request: "publish",
                audio: hasAudio,
                video: true,
                audio_level_event: hasAudio,
                active_active_packets: 2,
                audio_level_average: audioLevel
            };
            videoroomHandle.send({
                message: publish,
                jsep: jsep
            });

            setTimeout(() => {
                const pc = videoroomHandle.webrtcStuff.pc;
                const sender = pc.getSenders().find(s => s.track && s.track.kind === 'audio');
                if (sender) {
                    if (pushToTalkManager.setupPushToTalk(sender, audioTrack, user_id)) {
                        sender.replaceTrack(null);
                    }
                }
            }, 500);
        }catch (e) {
            console.error(e);
            showInfoMessage(`WebRTC createOffer error`);
        }
    }catch(err) {
        showInfoMessage("Ошибка доступа к медиа-устройствам: " + err.message);
        throw new Error("NO AVAILABLE DEVICES FOUND");
    }

    function createOffer(handle, options){
        return new Promise((resolve, reject) => {
            handle.createOffer({
                ...options,
                success: resolve,
                error: reject
            });
        });
    }
}

function subscribeToPublisher(feedId, videoAllowed = false) {
    janus.attach({
        plugin: "janus.plugin.videoroom",
        opaqueId: "subscriber-" + Janus.randomString(12),
        success: function (pluginHandle) {
            subscriberHandle.set(feedId, pluginHandle);

            pluginHandle.onmessage = function (msg, jsep) {
                console.log(msg);
                if (jsep) {
                    pluginHandle.createAnswer({
                        jsep: jsep,
                        media: {audioSend: false, videoSend: false, audioRecv: true, videoRecv: true},
                        success: function (jsep) {
                            pluginHandle.send({
                                message: {request: "start"},
                                jsep: jsep
                            });
                        },
                        error: function (error) {
                            console.error(error);
                            showInfoMessage("Ошибка подписки на участника");
                        }
                    });
                }
            };

            pluginHandle.onremotetrack = async function (track) {
                if (!subscriberHandle.has(feedId)) {
                    return;
                }
                if (!pluginHandle.remoteTracks) pluginHandle.remoteTracks = {};
                if (!pluginHandle.remoteStreams) pluginHandle.remoteStreams = {};
                pluginHandle.remoteTracks[track.kind] = track;

                const stream = new MediaStream([track]);
                const userId = feedManager.get(feedManager.MapKey.FEED,feedId);
                const element = UiManager.createUserBlock(track.kind === "video", track.kind === "audio", feedId,userId);

                if (track.kind === "video") {
                    Janus.attachMediaStream(element, stream);
                    pluginHandle.remoteStreams.video = element;
                    const userParticipant = document.querySelector(`#user_${feedManager.get(feedManager.MapKey.FEED,feedId)}`);
                    await toggleVideo(feedId, ((feedManager.isActive(feedId) && (VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(userParticipant, 'cam')) === defaultStates.ON) || VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(userParticipant, 'demo')) === defaultStates.ON)));
                    console.log(VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(userParticipant, 'cam')));
                    console.log(VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(userParticipant, 'demo')));
                    console.log(feedManager.isActive(feedId));
                    console.log(videoAllowed);
                }

                if (track.kind === "audio") {
                    Janus.attachMediaStream(element, stream);
                    pluginHandle.remoteStreams.audio = element;
                }
            };

            pluginHandle.oncleanup = function () {
                detachVideo(`remote_${feedId}_streams`);
                pluginHandle.remoteStream?.getTracks().forEach(track => track.stop())
                feedManager.removeActive(feedId);
                console.log('DELETING ACTIVE FEED ' + feedId)
            };

            pluginHandle.send({
                message: {
                    request: "join",
                    room: roomId,
                    ptype: "subscriber",
                    feed: feedId,
                    audio_level_event: true
                }
            });
        },
        error: function (error) {
            console.error(error);
            showInfoMessage("Ошибка attach подписчика");
        }
    });
}

function detachVideo(id){
    const video = document.querySelector(`#${id}`);
    console.log(video);
    if (video) {
        video.srcObject = null;
        video.remove();
    }
}

function unsubscribeFromPublisher(feedId) {
    if(!feedId){
        console.warn(`${feedId} is invalid`);
        return;
    }
    if (subscriberHandle.has(feedId)) {
        subscriberHandle.get(feedId).hangup();
        subscriberHandle.get(feedId).detach();
        subscriberHandle.delete(feedId);
    }
    console.log(feedId);
    detachVideo(`remote_streams_${feedId}`);
    feedManager.removeActive(feedId);
    console.log('DELETING ACTIVE FEED ' + feedId);
    const userId = feedManager.get(feedManager.MapKey.FEED,feedId);
    UiManager.lightUser(userId, false);
    console.log('talk unsub: ' + feedId);
}

function setUserCameraState(feedId) {
    const userId = feedManager.get(feedManager.MapKey.FEED,feedId);
    if (userId) {
        const userParticipant = document.querySelector(`#user_${userId}`);
        const camstate = VideoCallUtils.getParticipantSettingState(userParticipant, 'cam');
        console.log(camstate);
        if (VideoCallUtils.parseDefaultState(camstate) !== defaultStates.ON) {
            UiManager.updateUserDisplay(feedId, false);
        }
    }
}

async function switchToFullscreen(elementId) {
    const video = document.querySelector(`#${elementId}`);
    if (isFullscreen(video)) {
        video.querySelector('video').style['width'] = '300px';
        video.querySelector('button').innerText = '+';
        if (document.exitFullscreen) {
            await document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
    } else {
        video.querySelector('video').style['width'] = '100%';
        video.querySelector('button').innerText = '-';
        if (video.requestFullscreen) {
            await video.requestFullscreen();
        } else if (video.webkitRequestFullscreen) { // Safari
            video.webkitRequestFullscreen();
        } else if (video.msRequestFullscreen) { // IE11
            video.msRequestFullscreen();
        }
    }

    function isFullscreen(container) {
        return container && (document.fullscreenElement === container ||
            document.webkitFullscreenElement === container ||
            document.msFullscreenElement === container);
    }
}

async function ScreenSharing(videoroomHandle, start) {
    async function startScreenWithAudioMix() {
        const [displayStream, micStream] = await Promise.all([
            navigator.mediaDevices.getDisplayMedia({
                video: {
                    frameRate: {ideal: 30, max: 50},
                    width: {ideal: 1280},
                    height: {ideal: 720}
                },
                audio: true
            }),
            navigator.mediaDevices.getUserMedia({audio: true})
        ]);
        const audioContext = new AudioContext();
        const destination = audioContext.createMediaStreamDestination();

        const micSource = audioContext.createMediaStreamSource(micStream);
        micSource.connect(destination);

        const sysAudioTracks = displayStream.getAudioTracks();
        if (sysAudioTracks.length > 0) {
            const sysStream = new MediaStream([sysAudioTracks[0]]);
            const sysSource = audioContext.createMediaStreamSource(sysStream);
            sysSource.connect(destination);
        }
        console.log("displayStream audioTracks:", displayStream.getAudioTracks());
        console.log("micStream audioTracks:", micStream.getAudioTracks());
        return new MediaStream([
            ...displayStream.getVideoTracks(),
            ...destination.stream.getAudioTracks()
        ]);
    }

    async function hasCamera() {
        const devices = await navigator.mediaDevices.enumerateDevices();
        return devices.some(device => device.kind === 'videoinput');
    }

    let stream;
    if (start) {
        try {
            stream = await startScreenWithAudioMix();
            await replaceDisplayStreams(Promise.resolve(stream), videoroomHandle, false);
            isDemonstrationActive = true;
        } catch (err) {
            console.error("Ошибка при старте демонстрации с миксом звука:", err);
            showInfoMessage("Ошибка: " + err.message);
        }
    } else {
        const hasCam = await hasCamera();
        const constraints = {
            audio: true,
            video: hasCam
        };
        try {
            stream = await navigator.mediaDevices.getUserMedia(constraints);
            await replaceDisplayStreams(stream, videoroomHandle, true);
            isDemonstrationActive = false;
            await updateDeviceWithTracks(true, defaultStates.OFF);
        }catch(err){
            console.error("Ошибка при получении камеры/микрофона:", err);
            showInfoMessage("Ошибка: " + err.message);
        }
    }
}

async function replaceDisplayStreams(stream, videoroomHandle, camera) {
    console.warn(getCallerFunctionName(), camera);
    try {
        const screenTrack = stream.getVideoTracks()[0];
        const audioTracks = stream.getAudioTracks();
        const senders = videoroomHandle.webrtcStuff.pc.getSenders();
        const videoSender = senders.find(sender => sender.track?.kind === "video") || senders.find(sender => sender.track === null);
        const audioSender = senders.find(sender => sender.track && sender.track.kind === "audio");

        if (videoSender) {
            const wasNullTrack = !videoSender.track;
            try{
            await videoSender.replaceTrack(screenTrack);
                console.log("Видео заменено на демонстрацию экрана");

                if (wasNullTrack) {
                    console.log("Sender был пустой — делаем renegotiation");
                    videoroomHandle.createOffer({
                        media: {
                            audioRecv: false,
                            videoRecv: false,
                            replaceAudio: true,
                            replaceVideo: true
                        },
                        success: function (jsep) {
                            videoroomHandle.send({
                                message: {
                                    request: "configure",
                                    audio: true,
                                    video: true
                                },
                                jsep: jsep
                            });
                        }
                    });
                }

                const maxBitrate = 6000000;
                const bitrate = VideoCallUtils.getAllDemonstrators();
                videoroomHandle.send({
                    message: {
                        request: "configure",
                        video: true,
                        bitrate: Math.min(maxBitrate / bitrate, 2000000)
                    }
                });
            }catch(err) {
                console.error(camera, err);
                showInfoMessage("Не удалось переключиться");
                await updateDevice(Actions.DEMONSTRATION);
            }
            const settings = screenTrack.getSettings();
            console.log(`🎥 Actual FPS: ${settings.frameRate}, resolution: ${settings.width}x${settings.height}`);
            if(debugDisplayReplacement) {
                const stats = await videoroomHandle.webrtcStuff.pc.getStats();
                stats.forEach(report => {
                    if (report.type === "outbound-rtp" && report.kind === "video") {
                        console.log("Sent FPS:", report.framesPerSecond);
                    }
                });
            }
        } else {
            console.warn("Видео-трек не найден");
        }

        if (audioSender && audioTracks.length > 0) {
            const audioTrack = audioTracks[0];
            try {
                await audioSender.replaceTrack(audioTrack);
                console.log("Аудио трек заменён");
            }catch(err){
                console.error("Ошибка при замене аудио:", err);
            }
        }

        const video = document.querySelector("#video_display_own");
        if (video) {
            video.srcObject = stream;
        }
        localMediaStream = stream;
        if (camera) {
            const videostream = localMediaStream.getVideoTracks()[0];
            if (videostream) {
                videostream.enabled = false;
            }
        }
        console.log(camera);
        if (screenTrack) {
            screenTrack.onended = () => {
                console.log("🛑 Демонстрация экрана завершена");
                updateDevice(Actions.DEMONSTRATION);
            };
        }
    } catch (err) {
        console.error(camera, err);
        showInfoMessage("Ошибка при получении экрана");
    }

}

async function updateSoundState(newstate = null) {
    await updateDevice(Actions.SOUND, true, isSoundMuted);
    document.querySelector('#remote_videos_container')?.querySelectorAll('audio')
        .forEach(a => a.muted = newstate === null ? isSoundMuted : newstate);
}

async function updateDeviceWithTracks(video = true, newstate = null) {
    const stream = localMediaStream;
    if (!stream) {
        showInfoMessage("Ошибка получения медиапотоков");
        return;
    }
    const track = video ? stream.getVideoTracks()[0] : stream.getAudioTracks()[0];
    if (track) {
        const state = (newstate !== null && VideoCallUtils.isDefaultState(newstate.toString())) ? newstate : !track.enabled;
        track.enabled = (newstate !== null && VideoCallUtils.isDefaultState(newstate.toString())) ? state === defaultStates.ON : state;
        console.log(`${video ? 'Камера' : 'Микрофон'}`, track.enabled ? " on" : " off");
        console.log(newstate, state);
        await updateUserSettings((newstate !== null && VideoCallUtils.isDefaultState(newstate.toString())) ? state === defaultStates.ON : state === true, video ? Actions.CAMERA : Actions.MICROPHONE, true);
    }
}

async function updateDevice(action, self = true, status = null) {
    try {
        await updateUserSettings(status, action, self)
    } catch (e) {
        console.error(e);
    }
}

function updateRemoteMicrophone(id, forAll, element) {
    const feedId = feedManager.get(feedManager.MapKey.USER,id);
    const remoteAudio = document.querySelector(`#${feedId}_audio`);

    updateRemoteDevice(
        id,
        element,
        'mic',
        Actions.MICROPHONE,
        'Включить микрофон',
        'Заглушить',
        'Включить микрофон для всех',
        'Заглушить для всех'
    )

    const state = remoteAudio.muted;
    if (!forAll) {
        remoteAudio.muted = !state;
        element.innerText = remoteAudio.muted ? 'Включить микрофон' : 'Заглушить';
    }

}

function updateRemoteCamera(id, forAll, element) {
    const feedId = feedManager.get(feedManager.MapKey.USER,id);
    const remoteVideo = document.querySelector(`#${feedId}_video`);
    const text = element.innerText;

    let newstate = updateRemoteDevice(
        id,
        element,
        'cam',
        Actions.CAMERA,
        (text.includes('Включить') ? 'Отключить' : 'Включить') + ' камеру',
        (text.includes('Отключить')  ? 'Включить' : 'Отключить') + ' камеру',
        (text.includes('Отключить') ? 'Включить' : 'Отключить') + ' камеру для всех',
        (text.includes('Включить') ? 'Отключить' : 'Включить') + ' камеру для всех'
    )

    let visible = (newstate !== null && forAll) ? newstate : remoteVideo?.style['display'] === 'none';

    if (forAll) {
            if (newstate !== null) {
                visible = !visible;
            }
    } else {
        visible = remoteVideo?.style['display'] === 'none';
        element.innerText = (visible ? 'Отключить' : 'Включить') + ' камеру';
        if (!visible) {
            remoteVideo?.classList.add('disabled');
        } else {
            remoteVideo?.classList.remove('disabled');
        }
    }
    UiManager.updateUserDisplay(feedId, (VideoCallUtils.isDefaultState(newstate) && forAll) ? newstate === defaultStates.ON : visible);
}

function updateRemoteDevice(id,element,setting_name,action,text,text_fallback,text_admin='',text_admin_fallback='') {
    const userParticipant = document.querySelector(`#user_${id}`);
    let newstate = null;
    if (userParticipant) {
        const settingState = VideoCallUtils.getParticipantSettingState(userParticipant, setting_name);
        newstate = settingState !== null ? VideoCallUtils.parseDefaultState(settingState) : settingState;
    }
    if (VideoCallUtils.isDefaultState(newstate)) {
        newstate = newstate === defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
    }
    try {
        updateUserSettings(newstate, action, false, id);
    } catch (e) {
        return;
    }
    if (VideoCallUtils.isDefaultState(newstate)) {
        element.innerText = newstate === defaultStates.MUTED_BY_ADMIN ? text_admin : text_admin_fallback;
    } else {
        element.innerText = newstate ? text : text_fallback;
    }
    return newstate;
}

async function banUser(id) {
    await updateUserSettings(null, Actions.BAN, false, id);
}

function showParticipantList(matches, position) {
    const dropdown = document.querySelector('#participants_list');

    dropdown.innerHTML = '';
    matches.forEach(user => {
        const item = document.createElement('div');
        item.className = 'participants-item';
        item.textContent = user.name;
        item.addEventListener('click', () => {
            UiManager.insertParticipantIntoList(user.name);
            dropdown.style.display = 'none';
        });
        dropdown.appendChild(item);
    });

    if (matches.length > 0) {
        dropdown.style.left = position.left + 'px';
        dropdown.style.top = position.top + 'px';
        dropdown.style.display = 'block';
    } else {
        dropdown.style.display = 'none';
    }
}

function addMessageInputEventListener() {
    const input = document.querySelector('#message_input');
    input.addEventListener('input', () => {
        const text = input.value.slice(0, input.selectionStart);
        const match = text.match(/@([\wа-яё]*)$/i);
        if (match) {
            const search = match[1].toLowerCase();
            const users = VideoCallUtils.getUserNames().filter(u => u.name.toLowerCase().includes(search));
            const rect = input.getBoundingClientRect();
            showParticipantList(users, {
                left: rect.left,
                top: rect.bottom + window.scrollY,
            });
        } else {
            document.querySelector('#participants_list').style.display = 'none';
        }
    });
}

function leave(withRequest = true) {
    const remoteVideos = document.querySelectorAll("video[id^='remote_']");
    remoteVideos.forEach(video => {
        const feedId = Number(video.id.replace("_video", ""));
        video.remove();
        if (subscriberHandle.has(feedId)) {
            subscriberHandle.get(feedId).detach();
            subscriberHandle.delete(feedId);
        }
    });
    janus?.destroy();
    localMediaStream?.getTracks().forEach(track => track.stop());
    pushToTalkManager.disconnect();
    const id = document.querySelector(`#videocall_id`)?.value;
    if (withRequest) {
        isLeaving = true;
        fetch(`/videocall/${id}/leave?` + new URLSearchParams({reason: 'EXIT'}), {
            method: 'post',
            headers:{[csrfHeader]: csrfToken}
        }).then(response => {
            if (!response.ok) {
                let js=response.json();
                js.then(msg=> showInfoMessage(msg.message))
            } else {
                window.location.href = '/conferences';
            }
        });
    }
}

function addScrollEventListenerToRemoteVideosContainer() {
    document.querySelector('#remote_videos_container')?.addEventListener('wheel', function (event) {
        if (event.deltaY !== 0) {
            event.preventDefault();
            document.querySelector('#remote_videos_container').scrollLeft += event.deltaY;
        }
    }, {passive: false});
}

function setSoundsVolume() {
    const settingsRaw = localStorage.getItem('userSettings');
    if (settingsRaw) {
        const settings = JSON.parse(settingsRaw);
        if (settings.soundsVolume) {
            Object.values(sounds).forEach(snd => snd.volume = settings.soundsVolume / 100);
        }
    }
}

document.addEventListener('DOMContentLoaded', async function () {
    console.log('loaded');
    setSoundsVolume();
    await join();
    addScrollEventListenerToRemoteVideosContainer();
    addSettingsMenuListener('settings-block');
    document.querySelector('#message_input').addEventListener('keydown', async function (event) {
        if (event.key === 'Enter') {
            await sendMessageToChat();
        }
    });
    document.addEventListener('click', (e) => {
        const dropdown = document.querySelector('#participants_list');
        const input = document.querySelector('#message_input');
        if (!dropdown.contains(e.target) && e.target !== input) {
            dropdown.style.display = 'none';
        }
    });
    addMessageInputEventListener();
}, false);

window.addEventListener("beforeunload", async () => {
    document.querySelectorAll("[id^='remote_streams_']").forEach(el => el.remove());
    subscriberHandle.clear();
    feedManager.clearAll();
    leave(false);
    if (isLeaving) {
        return;
    }
    const data = new URLSearchParams({
        reason: 'RELOAD',
        csrf: csrfToken
    });
    navigator.sendBeacon(window.location.href + "/leave?" + new URLSearchParams({reason: 'RELOAD'}), data);
});