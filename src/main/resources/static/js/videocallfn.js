import {sounds, defaultStates, Actions, iconsVideocallUrl} from "./videocall/constants.js";
import  {FeedManager} from "./videocall/feedmanager.js";
import {CONFIG} from "./videocall/config.js";

let janus = null;
const feedManager = new FeedManager();
let subscriberHandle = new Map();
let opaqueId;
let roomId;
let isLeaving = false;
let isSoundMuted = false;
let localMediaStream = null;
let devices_start_state_updated = false;
let isDemonstrationActive = false;
let ws;
let wsKeylogger = null;
let debugDisplayReplacement=false
var debugHandle;

function isDefaultState(str) {
    return Object.values(defaultStates).includes(str);
}

function parseDefaultState(str) {
    if (str.toString() === 'muted') {
        str = `${str}_by_admin`;
    }
    for (let item in defaultStates) {
        const regex = new RegExp(`(?:^|[^a-zA-Z])${item.toLowerCase()}(?:[^a-zA-Z]|$)`, 'i');
        if (regex.test(str)) {
            return item;
        }
    }
    return null;
}

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
                    createUserParticipantBlock(participant);
                    updateUserDisplay(feedManager.get(feedManager.IdTypes.USER, participant.id), participant.camera === defaultStates.ON);
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
                    feedManager.get(feedManager.IdTypes.USER, jsdata.id)
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
                        setControlButtonIcon(jsdata.data.state, 'camstate');
                    } else if (jsdata.data.message.audio !== undefined) {
                        setControlButtonIcon(jsdata.data.state, 'microstate');
                    } else if (jsdata.data.message.sound !== undefined) {
                        setControlButtonIcon(jsdata.data.state, 'soundstate');
                    } else if (jsdata.data.message.demonstration !== undefined) {
                        setControlButtonIcon(jsdata.data.state, 'demostate');
                    }
                } else {
                    const userId = jsdata.userId;
                    const participant = document.querySelector(`#user_${userId}`);
                    console.log(userId);
                    console.log(participant);
                    if (participant) {
                        if (jsdata.data.message.video !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.CAMERA);
                            console.log(userId);
                           await manageActiveFeeds(userId,jsdata);
                        } else if (jsdata.data.message.audio !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.MICROPHONE);
                        } else if (jsdata.data.message.sound !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.SOUND);
                        } else if (jsdata.data.message.demonstration !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.DEMONSTRATION);
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
    const feedId = feedManager.get(feedManager.get(feedManager.IdTypes.USER, userId));
    if (feedId) {
        if (jsdata.data.state !== defaultStates.ON && feedManager.isActive(feedId)) {
            feedManager.removeActive(feedId);
            if (feedManager.checkActiveMax('lt')) {
                const users = document.querySelectorAll('[class*="user-participant"]');
                users.forEach(user => {
                    if (feedManager.checkActiveMax('gte')) {
                        return;
                    }
                    const state = getParticipantSettingState(user, 'cam');
                    console.log(state);
                    if (state !== null) {
                        if (parseDefaultState(state) === defaultStates.ON) {
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

function createUserParticipantBlock(participant) {
    const container = document.querySelector(".user-list-zone");
    if (!document.querySelector(`#user_${participant.id}`)) {
        const div1 = document.createElement("div");
        div1.className = "user-participant";
        div1.id = `user_${participant.id}`;
        div1.setAttribute("name", participant.login);
        container.appendChild(div1);
        const div2 = document.createElement('div');
        div2.className = 'user-participant-desc';
        const div3 = document.createElement('div');
        div3.className = 'user-participant-avatar-and-name';
        const img = document.createElement("img");
        img.src = `/useravatar/${participant.id}`;
        img.id = `user_avatar_${participant.id}`;
        img.className = "user-participant-avatar";
        const span = document.createElement("span");
        span.textContent = getUserCredentials(participant);
        div1.appendChild(div2);
        div2.appendChild(div3);
        div3.appendChild(img);
        div3.appendChild(span);
        const element = createSettingsBlock(div1, participant);
        const feedId=feedManager.get(feedManager.IdTypes.USER, participant.id);
        console.log('map has key:' + feedId);
        if (feedId) {
            const img = document.querySelector(`#${feedId}_image`);
            if (img) {
                img.src = `/useravatar/${participant.id}`;
            }
        }
        setParticipantPropertiesIcons(element, participant);
    }
}

function setParticipantPropertiesIcons(container, participant) {
    const div = document.createElement('div');
    div.className = 'user-participant-icons';
    container.appendChild(div);
    if (participant.microphone !== undefined) {
        createIcon('mic_', participant.microphone, div);
    }
    if (participant.camera !== undefined) {
        createIcon('cam_', participant.camera, div);
    }
    if (participant.sound !== undefined) {
        createIcon('snd_', participant.sound, div);
    }
    if (participant.demo !== undefined) {
        createIcon('demo_', participant.demo, div);
    }
}

function createIcon(className, state, container) {
    if (state.toString().includes('MUTED')) {
        state = 'MUTED';
    }
    const icon = document.createElement('img');
    icon.className = 'user-participant-icon';
    icon.classList.add(className + state.toLowerCase());
    icon.src = `${iconsVideocallUrl}/${className}${state.toLowerCase()}.png`;
    container.appendChild(icon);
    return icon;
}

function updateParticipantPropertiesIcons(container, state, action) {
    if (state.toString().includes('MUTED')) {
        state = 'MUTED';
    }
    //console.log(action);
    switch (action) {
        case Actions.MICROPHONE: {
            updateIcon('mic_', state, container);
            break;
        }
        case Actions.CAMERA: {
            updateIcon('cam_', state, container);
            break;
        }
        case Actions.SOUND: {
            updateIcon('snd_', state, container);
            break;
        }
        case Actions.DEMONSTRATION: {
            updateIcon('demo_', state, container);
            break;
        }
    }

    function updateIcon(className, state, container) {
        let icon = container.querySelector(`[class*='${className}']`);
        //console.log(icon);
        if (!icon) {
            icon = createIcon(className, state, container.querySelector('.user-participant-icons'));
        }
        icon.classList.forEach(name => {
            if (name.includes(className)) {
                icon.classList.remove(name);
            }
        });
        icon.classList.add(className + state.toLowerCase());
        icon.src = `${iconsVideocallUrl}/${className}${state.toString().toLowerCase()}.png`;
    }
}

function createSettingsBlock(container, participant) {
    console.log(participant);
    const actions = new Map([
        ['Заглушить',
            `updateRemoteMicrophone(${participant.id},false,this)`],
        [participant.microphone !== defaultStates.MUTED_BY_ADMIN ? 'Заглушить для всех' : 'Включить микрофон для всех',
            `updateRemoteMicrophone(${participant.id},true,this)`],
        ['Отключить камеру',
            `updateRemoteCamera(${participant.id},false,this)`],
        [participant.camera !== defaultStates.MUTED_BY_ADMIN ? 'Отключить камеру для всех' : 'Включить камеру для всех',
            `updateRemoteCamera(${participant.id},true,this)`],
        [participant.demonstration !== defaultStates.MUTED_BY_ADMIN ? 'Запретить демонстрацию экрана' : 'Разрешить демонстрацию экрана',
            `updateRemoteDevice(${participant.id},${this},'demo',${Actions.DEMONSTRATION},'Запретить демонстрацию экрана','Разрешить демонстрацию экрана')`],
        [participant.sound !== defaultStates.MUTED_BY_ADMIN ? 'Отключить звук' : 'Включить звук',
            `updateRemoteDevice(${participant.id},${this},'snd',${Actions.SOUND},'Отключить звук','Включить звук')`],
        ['Выгнать', 'banUser(' + participant.id + ')']
    ]);

    const div1 = document.createElement('div');
    div1.style['text-align'] = 'end';
    div1.className = 'user-participant-settings';
    container.appendChild(div1);
    const span = document.createElement('span');
    span.id = 'button_settings_' + participant.id;
    span.className = 'settings-btn';
    span.setAttribute('onclick', 'showSettingsMenu(' + participant.id + ')');
    span.innerText = '⚙️';
    div1.appendChild(span);
    const div2 = document.createElement('div');
    div2.id = 'settings_' + participant.id;
    div2.className = 'settings-block';
    div2.style['display'] = 'none';
    div1.appendChild(div2);
    actions.forEach((fn, name) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'setting-element';
        button.setAttribute('onclick', fn);
        button.innerText = name;
        div2.appendChild(button);
    });
    return div1;
}

function getParticipantSettingState(userContainer, settingClassName) {
    const settingsList = userContainer.querySelector('.user-participant-icons');
    let state = null;
    settingsList?.childNodes.forEach(setting => {
        const list = setting.classList;
        list.forEach(clname => {
            // console.log(clname,clname.toString().includes(settingClassName));
            if (clname.toString().includes(settingClassName)) {
                state = clname.substring(clname.indexOf('_') + 1);
            }
        });
    });
    return state;
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
    console.log('status: ' + status, isDefaultState(status));
    if (isDefaultState(status)) {
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
    const response = fetch(url, {
        method: 'post',
        headers: {[csrfHeader]: csrfToken}
    });
    if (!response.ok) {
        let js = response.json();
        const msg = await js;
        showInfoMessage(msg.message);
    }else{
        const data = await response.json();
        console.log(data);
        if (self) {
                isSoundMuted = data !== 'ON';
        }
    }
}

function setControlButtonIcon(state, id) {
    const element = document.querySelector(`#${id}`);
    element?.classList.remove(state === 'ON' ? 'videocall-setting-button-off' : "videocall-setting-button-on");
    element?.classList.add(state === 'ON' ? 'videocall-setting-button-on' : 'videocall-setting-button-off');
    if (state.toString().includes('MUTED')) {
        state = 'MUTED';
    }
    element?.classList.forEach(name => {
        if (name.includes(id)) {
            element?.classList.remove(name);
        }
    });
    element?.classList.add(id + '-' + state.toString().toLowerCase());
}

function lightUser(feedId, state) {
    const userId = feedManager.get(feedManager.IdTypes.FEED, feedId);
    if (userId) {
        const userBlock = document.querySelector(`#user_${userId}`);
        if (userBlock) {
            userBlock.style['border-color'] = state ? '#43db06' : '#304926';
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
    const response = fetch(window.location.href + '/addMessage', {
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
    let username;
    let user_id;
    let microstate = false;
    let camerastate = false;

    const confirmed = await createDialogWindow();
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
        const response = fetch(window.location.href + '/user/getData', {
            method: 'get'
        });
        if (!response.ok) {
            console.log('error');
            return;
        }
        try {
            const data = await response.json();
            roomId = data.videocallsId.roomId;
            username = data.videocalluserId.id.toString();
            user_id = data.videocalluserId.id;
            microstate = data.microstate;
            camerastate = data.camstate;
            console.log(data);
            opaqueId = `videoroom-${roomId}`;
            if (!devices_start_state_updated) {
                setControlButtonIcon(data.soundstate, 'soundstate');
                setControlButtonIcon(data.demostate, 'demostate');
                setControlButtonIcon(microstate, 'microstate');
                setControlButtonIcon(camerastate, 'camstate');
            }
            Janus.init({
                //  debug: "all",
                callback: function () {
                    startJanus(roomId, username, opaqueId, parseDefaultState(microstate), parseDefaultState(camerastate), user_id);
                }
            });
        }catch (e) {
            console.error(e);
        }
    }
}

function startJanus(roomId, username, opaqueId, microstate = defaultStates.OFF, camerastate = defaultStates.OFF, user_id) {
    let videoroomHandle;
    function generateTurnCredentials(secret) {
        const unixTimeStamp = Math.floor(Date.now() / 1000) + 3600;
        const username = `${unixTimeStamp}`;
        const password = CryptoJS.HmacSHA1(username, secret).toString(CryptoJS.enc.Base64);
        return {username, credential: password};
    }

    const {username: turnUsername, credential: turnCredential} = generateTurnCredentials(CONFIG.turn.secret);
    janus = new Janus({
        server:  CONFIG.janusServerWs,
        iceServers: [
            {urls: CONFIG.stun},
            {
                urls: CONFIG.turn.urls,
                username: turnUsername,
                credential: turnCredential
            }
        ],
        success: function () {
            janus.attach({
                plugin: "janus.plugin.videoroom",
                opaqueId: opaqueId,
                success: function (pluginHandle) {
                    videoroomHandle = pluginHandle;
                    debugHandle = pluginHandle;
                    const register = {
                        request: "join",
                        room: roomId,
                        ptype: "publisher",
                        display: username
                    };
                    videoroomHandle.send({message: register});
                },
                onmessage: async function (msg, jsep) {
                    console.log("Received message:", msg);
                    if (msg.videoroom === "joined") {
                        connectToVideocallWs(roomId, user_id, videoroomHandle);
                        feedManager.ownFeed = msg.id;
                        const publishers = msg.publishers || [];

                        if (publishers.length === 0) {
                            await publishOwnFeed(videoroomHandle, user_id);
                        } else {
                            for (let i = 0; i < publishers.length; i++) {
                                const publisher = publishers[i];
                                const display = publisher.display;
                                if (publisher.id !== feedManager.ownFeed) {
                                    console.log("👤 Новый участник:", display + ' ' + publisher.id);
                                    subscribe(publisher);
                                }
                            }
                            await publishOwnFeed(videoroomHandle);
                        }
                    }

                    if (msg.videoroom === "talking") {
                        const talkingFeedId = msg.id;
                        if (talkingFeedId === feedManager.ownFeed) {
                            return;
                        }
                        if (feedManager.checkActiveMax('gte')) {
                            let oldest = feedManager.getOldest();
                            if (oldest) {
                                await toggleVideo(talkingFeedId, false);
                                feedManager.removeActive(id);
                            }
                        }
                        feedManager.addActive(talkingFeedId);
                        const userId = feedManager.get(feedManager.IdTypes.FEED,talkingFeedId);
                        const container=document.querySelector(`#user_${userId}`);
                        if (parseDefaultState(getParticipantSettingState(container, 'cam')) === defaultStates.ON) {
                            await toggleVideo(talkingFeedId, true);
                        }
                        feedManager.removeTimeout(talkingFeedId);
                        lightUser(talkingFeedId, true);
                    }

                    if (msg.videoroom === "stopped-talking") {
                        const feedId = msg.id;
                        if (feedId === feedManager.ownFeed) {
                            return;
                        }

                        if (subscriberHandle.has(feedId) && feedManager.isActive(feedId)) {
                            if (feedManager.checkActiveMax('gt')) {
                                console.log('UNSUBBED');
                                const timeout = setTimeout(() => {
                                    console.log('TIMEOUT');
                                    const userId = feedManager.get(feedManager.IdTypes.FEED,feedId);
                                    const container=document.querySelector(`#user_${userId}`);
                                    if (parseDefaultState(getParticipantSettingState(container, 'cam')) === defaultStates.ON) {
                                        toggleVideo(feedId, false);
                                    }
                                    feedManager.removeTimeout(feedId);
                                }, 5000);

                                feedManager.addTimeout(feedId,timeout);
                            }
                            lightUser(feedId, false);
                        }
                    }

                    if (msg.videoroom === "event") {
                        if (msg.leaving || msg.unpublished) {
                            const leavingFeed = msg.leaving || msg.unpublished;
                            if (leavingFeed === feedManager.ownFeed) {
                                return;
                            }
                            unsubscribeFromPublisher(leavingFeed);
                            const userId = feedManager.get(feedManager.IdTypes.FEED,leavingFeed);
                            feedManager.remove(leavingFeed,userId);
                            const users = document.querySelectorAll('[class*="user-participant"]');
                            users.forEach(user => {
                                if (feedManager.checkActiveMax('gte')) {
                                    return;
                                }
                                const state = getParticipantSettingState(user, 'cam');
                                console.log(state);
                                if (state !== null) {
                                    if (parseDefaultState(state) === defaultStates.ON) {
                                        const userId = Number(user.id.substring(user.id.indexOf('_') + 1));
                                        const feedId = feedManager.get(feedManager.IdTypes.USER,userId);
                                        if (feedId) {
                                            toggleVideo(feedId, true);
                                            feedManager.addActive(feedId);
                                        }
                                    }
                                }
                            });
                        }
                        if (msg.publishers) {
                            const publishers = msg.publishers;
                            for (let i = 0; i < publishers.length; i++) {
                                const publisher = publishers[i];
                                console.log("📡 Новый опубликованный поток:", publisher.display, publisher.id);
                                if (publisher.id === feedManager.ownFeed) {
                                    return;
                                }
                                if (!subscriberHandle.has(publisher.id)) {
                                    subscribe(publisher);
                                }
                            }
                        }
                        if (msg.configured === "ok" && !devices_start_state_updated) {
                            devices_start_state_updated = true;
                            updateDeviceWithTracks(false, microstate);
                            updateDeviceWithTracks(true, camerastate);
                        }
                    }

                    if (jsep) {
                        videoroomHandle.handleRemoteJsep({jsep: jsep});
                    }
                }
            });
        }
    });
}

function subscribe(publisher){
    feedManager.add(feedManager.IdTypes.USER, Number(publisher.display), publisher.id);
    feedManager.add(feedManager.IdTypes.FEED, publisher.id, Number(publisher.display));
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
        updateUserDisplay(feedId, visible);
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
        updateUserDisplay(feedId, false);
    }
}

function createDialogWindow() {
    return new Promise((resolve) => {
        const documentTitle = document.title || "unknown";
        const existingDialog = document.querySelector("#confirm_join_dialog");
        if (existingDialog) existingDialog.remove();
        const dialog = document.createElement("div");
        dialog.id = "confirm_join_dialog";
        dialog.className = 'publish-dialog-window';
        dialog.innerHTML = `
        <p style="margin-bottom: 20px;">Вы присоединяетесь к конференции <strong>${documentTitle}</strong>. Продолжить?</p>
        <div style="text-align: right;">
            <button id="confirm_join_yes" style="margin-right: 10px;">Да</button>
            <button id="confirm_join_no">Нет</button>
        </div>
    `;
        document.body.appendChild(dialog);
        document.querySelector("#confirm_join_yes").onclick = () => {
            dialog.remove();
            resolve(true);
        };
        document.querySelector("#confirm_join_no").onclick = () => {
            dialog.remove();
            resolve(false);
        };
    });
}

function connectToKeyloggerWebsocket(keys, sender, track, user_id) {
    let reconnectDelay = 2000;
    let localWs;
    let isManuallyClosed = false;
    const user = document.querySelector(`#user_${user_id}`);

    function connect() {
        const settings = JSON.parse(localStorage.getItem('userSettings'));
        const port = settings?.portPushToTalk !== '' ? settings?.portPushToTalk : '60602';
        localWs = new WebSocket('ws://localhost:' + port);
        localWs.onopen = function (event) {
            const senddata = {
                event: 'connected',
                keys: keys
            };
            localWs.send(JSON.stringify(senddata));
            reconnectDelay = 2000;
        }

        localWs.onmessage = async function (event) {
            const jsdata = JSON.parse(event.data);
            if (jsdata.event === 'ping') {
                const resp = {event: 'pong'};
                localWs.send(JSON.stringify(resp));
            } else if (jsdata.event === 'pressed') {
                await sender.replaceTrack(track);
                if (parseDefaultState(getParticipantSettingState(user, 'mic')) === defaultStates.ON) {
                    await sounds.VOICESTART.play();
                }
            } else if (jsdata.event === 'released') {
                await sender.replaceTrack(null);
                if (parseDefaultState(getParticipantSettingState(user, 'mic')) === defaultStates.ON) {
                    await sounds.VOICEEND.play();
                }
            } else if (jsdata.event === 'shutdown') {
                localWs.close();
            }
        }

        localWs.onclose = () => {
            if (localWs.readyState === WebSocket.CLOSED && !isLeaving) {
                const iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                iframe.src = 'pttutility://launch' + (port ? '?' + new URLSearchParams({port}) : '');
                document.body.appendChild(iframe);
            } else if (!isManuallyClosed) {
                setTimeout(connect, reconnectDelay);
                reconnectDelay += 1500;
                if (reconnectDelay > 10000) {
                    isManuallyClosed = true;
                    reconnectDelay = 2000;
                }
            }
        };

        localWs.onerror = (e) => console.error("WebSocket error:", e);
    }

    connect();

    return {
        disconnect: () => {
            isManuallyClosed = true;
            localWs.close();
        }
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
            throw new Error("NO AVAILABLE DEVICES FOUND");
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
                    if (setupPushToTalk(sender, audioTrack, user_id)) {
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

    function setupPushToTalk(sender, track, user_id) {
        try {
            const settings = JSON.parse(localStorage.getItem('userSettings'));
            const k = Object.keys(settingVoiceDetection);
            if (settings.voiceMode !== 'PUSH_TO_TALK') {
                return false;
            }
            const keys = Array.from(settings.keysPushToTalk);
            if (keys.length === 0) {
                throw new Error();
            }
            const port = settings.portPushToTalk;
            wsKeylogger = connectToKeyloggerWebsocket(keys, sender, track, user_id);
        } catch (e) {
            showInfoMessage("Не заданы клавиши режима рации");
            console.error(e.message);
            return false;
        }
        return true;
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
                const element = createUserBlock(track.kind === "video", track.kind === "audio", feedId);

                if (track.kind === "video") {
                    Janus.attachMediaStream(element, stream);
                    pluginHandle.remoteStreams.video = element;
                    const userParticipant = document.getElementById('user_' + feedId_userId.get(feedId));
                    await toggleVideo(feedId, ((feedManager.isActive(feedId) && (parseDefaultState(getParticipantSettingState(userParticipant, 'cam')) === defaultStates.ON) || parseDefaultState(getParticipantSettingState(userParticipant, 'demo')) === defaultStates.ON)));
                    console.log(parseDefaultState(getParticipantSettingState(userParticipant, 'cam')));
                    console.log(parseDefaultState(getParticipantSettingState(userParticipant, 'demo')));
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
    let deleteSuccessful = feedManager.removeActive(feedId);
    console.log(deleteSuccessful ? 'DELETING ACTIVE FEED ' + feedId : '');
    lightUser(feedId, false);
    console.log('talk unsub: ' + feedId);
}

function setUserCameraState(feedId) {
    const userId = feedManager.get(feedManager.IdTypes.FEED,feedId);
    if (userId) {
        const userParticipant = document.querySelector(`#user_${userId}`);
        const camstate = getParticipantSettingState(userParticipant, 'cam');
        console.log(camstate);
        if (parseDefaultState(camstate) !== defaultStates.ON) {
            updateUserDisplay(feedId, false);
        }
    }
}

function createUserBlock(video = false, audio = false, feedId) {
    console.log('CREATING REMOTE STREAMS BLOCK FOR ' + feedId);
    let container = document.querySelector(`#remote_streams_${feedId}`);
    if (!container) {
        const div = document.createElement('div');
        div.id = 'remote_streams_' + feedId;
        div.className = 'remote-streams-zone';
        document.querySelector("#remote_videos_container")?.appendChild(div);
        container = document.querySelector(`#remote_streams_${feedId}`);
    }
    let element;
    if (video) {
        element = document.querySelector(`#${feedId}_video`);
        if (!element) {
            element = document.createElement('img');
            element.className = 'remote-video-image';
            element.id = feedId + '_image';
            if (feedId_userId.has(feedId)) {
                const userId = feedId_userId.get(feedId);
                const avatar = document.querySelector(`#user_avatar_${userId}`);
                if (avatar) {
                    element.src = avatar.src;
                }
                element.style['display'] = 'none';
            }
            container.appendChild(element);
            element = document.createElement("video");
            element.id = feedId + "_video";
            element.autoplay = true;
            element.playsInline = true;
            element.controls = false;
            container.appendChild(element);
            const controlContainer = document.createElement('div');
            controlContainer.style['position'] = 'absolute';
            controlContainer.style['bottom'] = '0';
            controlContainer.style['right'] = '0';
            container.appendChild(controlContainer);
            const control_fullDisplay = document.createElement('button');
            control_fullDisplay.type = 'button';
            control_fullDisplay.innerText = '+';
            control_fullDisplay.setAttribute('onclick', 'switchToFullscreen(\'' + container.id + '\')');
            controlContainer.appendChild(control_fullDisplay);
        }
    } else if (audio) {
        element = document.querySelector(`#${feedId}_audio`);
        if (!element) {
            element = document.createElement("audio");
            element.id = feedId + "_audio";
            element.style['display'] = 'none';
            element.autoplay = true;
            element.controls = true;
            container.appendChild(element);
        }
    }
    return element;
}

function switchToFullscreen(elementId) {
    const video = document.querySelector(`#${elementId}`);
    if (isFullscreen(video)) {
        video.querySelector('video').style['width'] = '300px';
        video.querySelector('button').innerText = '+';
        if (document.exitFullscreen) {
            document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
            document.webkitExitFullscreen();
        } else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
    } else {
        video.querySelector('video').style['width'] = '100%';
        video.querySelector('button').innerText = '-';
        if (video.requestFullscreen) {
            video.requestFullscreen();
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
            updateDeviceWithTracks(true, defaultStates.OFF);
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
                const bitrate = getAllDemonstrators();
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
                updateDevice(Actions.DEMONSTRATION);
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

    function getAllDemonstrators() {
        const users = document.querySelectorAll('[class="user-participant"]');
        let count = 0;
        users.forEach(user => {
            const setting = getParticipantSettingState(user, 'demo');
            const state = parseDefaultState(setting);
            if (state === defaultStates.ON) {
                count++;
            }
        });
        return count || 1;
    }
}

function updateSoundState(newstate = null) {
    updateDevice(Actions.SOUND,true,isSoundMuted);
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
        const state = (newstate !== null && isDefaultState(newstate.toString())) ? newstate : !track.enabled;
        track.enabled = (newstate !== null && isDefaultState(newstate.toString())) ? state === defaultStates.ON : state;
        console.log(`${video ? 'Камера' : 'Микрофон'}`, track.enabled ? " on" : " off");
        console.log(newstate, state);
        await updateUserSettings((newstate !== null && isDefaultState(newstate.toString())) ? state === defaultStates.ON : state === true, video ? Actions.CAMERA : Actions.MICROPHONE, true);
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
    const feedId = userId_feedId.get(id);
    const remoteAudio = document.querySelector(`#${feedId}_audio`);

    let newstate = updateRemoteDevice(
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
    const feedId = userId_feedId.get(id);
    const remoteVideo = document.querySelector(`#${feedId}_video`);

    let newstate = updateRemoteDevice(
        id,
        element,
        'cam',
        Actions.CAMERA,
        (visible ? 'Отключить' : 'Включить') + ' камеру',
        (visible ? 'Включить' : 'Отключить') + ' камеру',
        (visible === defaultStates.MUTED_BY_ADMIN ? 'Включить' : 'Отключить') + ' камеру для всех',
        (visible === defaultStates.MUTED_BY_ADMIN ? 'Отключить' : 'Включить') + ' камеру для всех'
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
    updateUserDisplay(feedId, (isDefaultState(newstate) && forAll) ? newstate === defaultStates.ON : visible);
}

function updateRemoteDevice(id,element,setting_name,action,text,text_fallback,text_admin='',text_admin_fallback='') {
    const userParticipant = document.querySelector(`#user_${id}`);
    let newstate = null;
    if (userParticipant) {
        const settingState = getParticipantSettingState(userParticipant, setting_name);
        newstate = settingState !== null ? parseDefaultState(settingState) : settingState;
    }
    if (isDefaultState(newstate)) {
        newstate = newstate === defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
    }
    try {
        updateUserSettings(newstate, action, false, id);
    } catch (e) {
        return;
    }
    if (isDefaultState(newstate)) {
        element.innerText = newstate === defaultStates.MUTED_BY_ADMIN ? text_admin : text_admin_fallback;
    } else {
        element.innerText = newstate ? text : text_fallback;
    }
    return newstate;
}

async function banUser(id) {
    await updateUserSettings(null, Actions.BAN, false, id);
}

function updateUserDisplay(feedId, visible) {
    if(!feedId){
        console.warn(`${feedId} is invalid`);
        return;
    }
    const img = document.querySelector(`#${feedId}_image`);
    const remoteVideo = document.querySelector(`#${feedId}_video`);
    if (remoteVideo?.classList.contains('disabled') && visible) {
        return;
    }
    remoteVideo.style['display'] = visible ? '' : 'none';
    if (img) {
        img.style['display'] = visible ? 'none' : '';
    }
}

function getUserNames() {
    return Array.from(document.querySelectorAll('.user-participant')).map(element => ({
        id: element.id,
        name: element.getAttribute('name'),
    }));
}

function showParticipantList(matches, position) {
    const dropdown = document.querySelector('#participants_list');

    dropdown.innerHTML = '';
    matches.forEach(user => {
        const item = document.createElement('div');
        item.className = 'participants-item';
        item.textContent = user.name;
        item.addEventListener('click', () => {
            insertParticipantIntoList(user.name);
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

function insertParticipantIntoList(name) {
    const input = document.querySelector('#message_input');
    const text = input.value;
    const cursorPos = input.selectionStart;
    const before = text.slice(0, cursorPos);
    const after = text.slice(cursorPos);
    const match = before.match(/@[\wа-яё]*$/i);
    if (match) {
        const start = match.index;
        input.value = before.slice(0, start) + '@' + name + ' ' + after;
        input.focus();
        input.selectionStart = input.selectionEnd = start + name.length + 2;
    }
}

function addMessageInputEventListener() {
    const input = document.querySelector('#message_input');
    input.addEventListener('input', (e) => {
        const text = input.value.slice(0, input.selectionStart);
        const match = text.match(/@([\wа-яё]*)$/i);
        if (match) {
            const search = match[1].toLowerCase();
            const users = getUserNames().filter(u => u.name.toLowerCase().includes(search));
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
    wsKeylogger?.disconnect();
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