let janus = null;
let subscriberHandle=new Map();
let opaqueId;
let roomId;
let isLeaving=false;
let isSoundMuted=false;
let localMediaStream=null;
let devices_start_state_updated=false;
let isDemonstrationActive=false;
let ws;
let wsKeylogger=null;
let window_count=0;
let contentwindow_aspectratio=new Map();
const timeoutFeeds=new Map();
const activeFeeds=new Set();
const max_active_feeds=1;
const userId_feedId=new Map;
const feedId_userId=new Map();
const Actions= {MICROPHONE: 'AUDIO', CAMERA:'VIDEO',BAN:'BAN',SOUND:'SOUND',DEMONSTRATION:'DEMONSTRATION'};
const defaultStates={OFF:'OFF',ON:'ON',MUTED_BY_ADMIN:'MUTED_BY_ADMIN'};
const iconsVideocallUrl='/files/icons/videocall';
const sounds={
    DEMOSTART:new Audio('/files/sound/videocall/demo_start.wav'),
    DEMOEND:new Audio('/files/sound/videocall/demo_end.wav'),
    VOICESTART:new Audio('/files/sound/videocall/voice_start.wav'),
    VOICEEND:new Audio('/files/sound/videocall/voice_end.wav'),
    JOIN:new Audio('/files/sound/videocall/join.wav'),
    LEAVE:new Audio('/files/sound/videocall/leave.wav')
};

sounds.JOIN.playbackRate=1.3;

function isStringDefaultStates(str){
    return Object.values(defaultStates).includes(str);
}

function parseDefaultStateFromString(str){
    if(str.toString().includes('muted')){
        str=str+'_by_admin';
    }
    for(let item in defaultStates){
        const regex = new RegExp(`(?:^|[^a-zA-Z])${item.toLowerCase()}(?:[^a-zA-Z]|$)`, 'i');
        if(regex.test(str)){
            return item;
        }
    }
    return null;
}

function connectToVideocallWs(room_id,user_id,videoroomHandle) {
    const ws_addr = "wss://5.189.10.253:60600";//"wss://192.168.0.106:60600";
    ws = new WebSocket(ws_addr);
    console.log('server WS started');

    ws.onopen = function () {
        const request = {
            event: "joined",
            eventType:"videocall",
            roomId: room_id,
            userId: user_id
        };
        ws.send(JSON.stringify(request));
    }

    ws.onmessage = function (event) {
        console.log('received server ws message');
        const jsdata = JSON.parse(event.data);
        console.log(jsdata);
        if (jsdata.eventType === "videocall") {
            if (jsdata.event === "ping") {
                ws.send(JSON.stringify({event: "pong", eventType: "videocall", userId: user_id}));
            } else if (jsdata.event === "connected") {
                const participants = jsdata.users;
                participants.forEach(participant => {
                    createUserParticipantBlock(participant);
                    if (userId_feedId.has(participant.id)) {
                        updateUserDisplay(userId_feedId.get(participant.id), participant.camera === defaultStates.ON);
                    }
                });
                const messages = jsdata.messageArray;
                messages.forEach(msg => {
                    addMessageToChat(msg, user_id);
                });
                sounds.JOIN.play();
            } else if (jsdata.event === "disconnected") {
                if (jsdata.forced) {
                    leave(true);
                }
                if (userId_feedId.has(jsdata.id)) {
                    if (subscriberHandle.has(userId_feedId.get(jsdata.id))) {
                        unsubscribeFromPublisher(subscriberHandle.get(userId_feedId.get(jsdata.id)));
                    }
                }
                const element = document.getElementById('user_' + jsdata.id);
                if (element) {
                    element.remove();
                }
                sounds.LEAVE.play();
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
                        //console.log(track);
                        // console.log(state);
                        if (track !== undefined && state !== undefined) {
                            track.enabled = state;
                        }
                    } else if (jsdata.type === "other") {
                        if (jsdata.data.message.sound !== undefined) {
                            console.log('muted');
                            isSoundMuted = jsdata.data.state !== defaultStates.ON;
                            userId_feedId.forEach(value => {
                                const element = document.getElementById(value + '_audio');
                                console.log(element);
                                console.log(isSoundMuted);
                                if (element) {
                                    element.muted = isSoundMuted;
                                }
                            });
                        } else if (jsdata.data.message.demonstration !== undefined) {
                            if (jsdata.data.state !== defaultStates.MUTED_BY_ADMIN) {
                                if (jsdata.data.state === defaultStates.ON || jsdata.data.state === defaultStates.OFF && isDemonstrationActive) {
                                    ScreenSharing(videoroomHandle, jsdata.data.state === defaultStates.ON);
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
                    const participant = document.getElementById('user_' + userId);
                    console.log(userId);
                    console.log(participant);
                    if (participant) {
                        if (jsdata.data.message.video !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.CAMERA);
                            if (userId_feedId.has(userId)) {
                                //  updateUserDisplay(userId_feedId.get(userId),jsdata.data.state===defaultStates.ON);
                            }
                            console.log(userId);
                            if (userId_feedId.has(userId)) {
                                if(jsdata.data.state !== defaultStates.ON &&activeFeeds.has(userId_feedId.get(userId))){
                                    activeFeeds.delete(userId_feedId.get(userId));
                                    if(activeFeeds.size<max_active_feeds){
                                        const users=document.querySelectorAll('[class*="user-participant"]');
                                        users.forEach(user=> {
                                            console.log(activeFeeds.size);
                                            if(activeFeeds.size>=max_active_feeds){
                                                return;
                                            }
                                            const state=getParticipantSettingState(user,'cam');
                                            console.log(state);
                                            if(state!==null){
                                                if(parseDefaultStateFromString(state)===defaultStates.ON){
                                                    const userId=Number(user.id.substring(user.id.indexOf('_')+1));
                                                    if(userId_feedId.has(userId)) {
                                                        toggleVideo(userId_feedId.get(userId),true);
                                                        activeFeeds.add(userId_feedId.get(userId));
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                                if (activeFeeds.size < max_active_feeds || activeFeeds.has(userId_feedId.get(userId))) {
                                    if(!activeFeeds.has(userId_feedId.get(userId))) {
                                        if(jsdata.data.state === defaultStates.ON) {
                                            activeFeeds.add(userId_feedId.get(userId));
                                        }
                                    }
                                    console.log('TOGGLING VIDEO WITH REWUEST');
                                    toggleVideo(userId_feedId.get(userId), jsdata.data.state === defaultStates.ON);
                                }
                            }
                        } else if (jsdata.data.message.audio !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.MICROPHONE);
                        } else if (jsdata.data.message.sound !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.SOUND);
                        } else if (jsdata.data.message.demonstration !== undefined) {
                            updateParticipantPropertiesIcons(participant, jsdata.data.state, Actions.DEMONSTRATION);
                            if(jsdata.data.state === defaultStates.ON) {
                                sounds.DEMOSTART.play().catch(err => console.warn('Autoplay block?', err));
                            }else if(jsdata.data.state === defaultStates.OFF){
                                sounds.DEMOEND.play().catch(err => console.warn('Autoplay block?', err));
                            }
                            if (userId_feedId.has(userId)) {
                                if(jsdata.data.state !== defaultStates.ON &&activeFeeds.has(userId_feedId.get(userId))){
                                    activeFeeds.delete(userId_feedId.get(userId));
                                    if(activeFeeds.size<max_active_feeds){
                                        const users=document.querySelectorAll('[class*="user-participant"]');
                                        users.forEach(user=> {
                                            console.log(activeFeeds.size);
                                            if(activeFeeds.size>=max_active_feeds){
                                                return;
                                            }
                                            const state=getParticipantSettingState(user,'cam');
                                            console.log(state);
                                            if(state!==null){
                                                if(parseDefaultStateFromString(state)===defaultStates.ON){
                                                    const userId=Number(user.id.substring(user.id.indexOf('_')+1));
                                                    if(userId_feedId.has(userId)) {
                                                        toggleVideo(userId_feedId.get(userId),true);
                                                        activeFeeds.add(userId_feedId.get(userId));
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                                if (activeFeeds.size < max_active_feeds || activeFeeds.has(userId_feedId.get(userId))) {
                                    if(!activeFeeds.has(userId_feedId.get(userId))) {
                                        activeFeeds.add(userId_feedId.get(userId));
                                    }
                                    console.log('TOGGLING DEMO WITH REWUEST');
                                    toggleVideo(userId_feedId.get(userId), jsdata.data.state === defaultStates.ON);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ws.onclose = function () {
        const request = {
            event: "leave",
            eventType:"videocall",
            roomId: room_id,
            userId: user_id
        };
        ws.send(JSON.stringify(request));
    }
}

    function createUserParticipantBlock(participant){
        const container = document.querySelector(".user-list-zone");
        if(!document.getElementById('user_'+participant.id)) {
            const div1 = document.createElement("div");
            div1.className = "user-participant";
            div1.id = 'user_' + participant.id;
            div1.setAttribute("name",participant.login);
            container.appendChild(div1);
            const div2=document.createElement('div');
            div2.className='user-participant-desc';
            const div3=document.createElement('div');
            div3.className='user-participant-avatar-and-name';
            const img = document.createElement("img");
            img.src = "/useravatar/" + participant.id;
            img.id='user_avatar_'+participant.id;
            img.className = "user-participant-avatar";
            const span = document.createElement("span");
            span.textContent = getUserCredentials(participant);
            div1.appendChild(div2);
            div2.appendChild(div3);
            div3.appendChild(img);
            div3.appendChild(span);
            const element=createSettingsBlock(div1,participant);
            console.log('map has key:'+userId_feedId.has(participant.id));
            if(userId_feedId.has(participant.id)){
                const feedId=userId_feedId.get(participant.id);
                const img=document.getElementById(feedId+'_image');
                console.log(feedId);
                if(img){
                    console.log('adding src');
                    img.src="/useravatar/" + participant.id;
                }
            }
            setParticipantPropertiesIcons(element,participant);
        }
    }

function setParticipantPropertiesIcons(container,participant){
    const div=document.createElement('div');
    div.className='user-participant-icons';
    container.appendChild(div);
    if(participant.microphone!==undefined){
        createIcon('mic_',participant.microphone,div);
    }
    if(participant.camera!==undefined){
        createIcon('cam_',participant.camera,div);
    }
    if(participant.sound!==undefined){
        createIcon('snd_',participant.sound,div);
    }
    if(participant.demo!==undefined){
        createIcon('demo_',participant.demo,div);
    }
}

function createIcon(className,state,container) {
    if(state.toString().includes('MUTED')){
        state='MUTED';
    }
    const icon = document.createElement('img');
    icon.className = 'user-participant-icon';
    icon.classList.add(className + state.toLowerCase());
    icon.src = iconsVideocallUrl + '/' + className + state.toLowerCase() + '.png';
    container.appendChild(icon);
    return icon;
}

function updateParticipantPropertiesIcons(container,state,action){
    if(state.toString().includes('MUTED')){
        state='MUTED';
    }
    //console.log(action);
    switch (action){
        case Actions.MICROPHONE:{
            updateIcon('mic_',state,container);
        break;}
        case Actions.CAMERA:{
            updateIcon('cam_',state,container);
        break;}
        case Actions.SOUND:{
            updateIcon('snd_',state,container);
        break;}
        case Actions.DEMONSTRATION:{
            updateIcon('demo_',state,container);
        break;}
    }

    function updateIcon(className,state,container){
        let icon = container.querySelector(`[class*='${className}']`);
        //console.log(icon);
        if(!icon) {
            icon=createIcon(className,state,container.querySelector('.user-participant-icons'));
        }
        icon.classList.forEach(name=>{
            if(name.includes(className)){
                icon.classList.remove(name);
            }
        });
        icon.classList.add(className + state.toLowerCase());
        icon.src = iconsVideocallUrl + '/' + className + state.toString().toLowerCase() + '.png';
    }
}

function createSettingsBlock(container,participant) {
    console.log(participant);
        const actions = new Map([
            ['Заглушить',
                'updateRemoteMicrophone(' + participant.id + ',false,this)'],
            [participant.microphone!==defaultStates.MUTED_BY_ADMIN ? 'Заглушить для всех' : 'Включить микрофон для всех',
                'updateRemoteMicrophone(' + participant.id + ',true,this)'],
            ['Отключить камеру',
                'updateRemoteCamera(' + participant.id + ',false,this)'],
            [participant.camera!==defaultStates.MUTED_BY_ADMIN ? 'Отключить камеру для всех' : 'Включить камеру для всех',
                'updateRemoteCamera(' + participant.id + ',true,this)'],
            [participant.demonstration!==defaultStates.MUTED_BY_ADMIN ? 'Запретить демонстрацию экрана' : 'Разрешить демонстрацию экрана',
                'updateRemoteDemonstration('+participant.id+',this)'] ,
            [participant.sound!==defaultStates.MUTED_BY_ADMIN ? 'Отключить звук' : 'Включить звук',
                'updateRemoteSound('+participant.id+',this)'],
            ['Выгнать', 'banUser(' + participant.id + ')']
        ]);

        const div1 = document.createElement('div');
        div1.style['text-align'] = 'end';
        div1.className='user-participant-settings';
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

    function getParticipantSettingState(userContainer,settingClassName) {
        const settingsList = userContainer.querySelector('.user-participant-icons');
        let state=null;
        if (settingsList) {
            settingsList.childNodes.forEach(setting => {
                const list=setting.classList;
                list.forEach(clname => {
                   // console.log(clname,clname.toString().includes(settingClassName));
                    if (clname.toString().includes(settingClassName)) {
                        state=clname.substring(clname.indexOf('_')+1);
                    }
                });
            });
        }
        return state;
    }

    function addMessageToChat(msg,userId){
        const container=document.querySelector('.chat-block');
        if(!document.getElementById('message_'+msg.id)){
            const div=document.createElement('div');
            div.id='message_'+msg.id;
            div.className='chat-message';
            container.appendChild(div);
            const span_time=document.createElement('span');
            span_time.className='chat-message-time';
            const date=new Date(msg.timestamp);
            span_time.innerText=DateTimeToFormat(date.getHours())+':'+DateTimeToFormat(date.getMinutes())+' ';
            const span_name=document.createElement('span');
            span_name.className='chat-message-username';
            span_name.style['color']=generateNameColor('#2e2e2e');
            span_name.innerText=getUserCredentials(msg)+': ';
            div.appendChild(span_time);
            div.appendChild(span_name);
            const span_text=document.createElement('span');
            span_text.className='chat-message-text';
            let messageText=msg.text;
            if(msg.replyToId!==null && msg.replyToName!==null) {
                console.log(msg.replyToId === userId);
                if (msg.replyToId === userId) {
                    const userName=msg.replyToName;
                    const bgcolor= '#16b919';
                    const textcolor= 'black';
                    console.log(messageText);
                    const pattern=new RegExp(`@${userName}\\b`, 'gi');
                    messageText = messageText.toString().replace(pattern,`<span style="background:${bgcolor};color:${textcolor};padding: 1px;font-weight: bold;border-radius: 3px">@${userName}</span>`);
                    console.log(messageText);
                }
            }
            span_text.innerHTML = messageText;
            div.appendChild(span_text);
        }
    }

function updateUserSettings(status,action,self,userId=null){
    let state;
    console.log('status: '+status,isStringDefaultStates(status));
    if(isStringDefaultStates(status)){
      state=status;
    }else {
        state = status !== null ? (status ? 'ON' : 'OFF') : status;
    }
    let url=window.location.href+'/user/update?';
    const searchParams=new URLSearchParams({action:action.toUpperCase(),self:self});
    if(userId!==null){
        searchParams.append('userUpdatedId',userId);
    }
    if(state!==null){
        searchParams.append('state',state);
    }
    url+=searchParams;
    console.log(url);
    fetch(url,{
        method:'post',
        headers:{[csrfHeader]: csrfToken}
    }).then(response =>{
        if(!response.ok){
            let js=response.json();
            js.then(msg=> showInfoMessage(msg.message));
            return;
        }
        return response.json();
    }).then(data =>{
        console.log(data);
        if(self) {
            if (data) {
                isSoundMuted = data !== 'ON';
            }
        }
    });
}

function setControlButtonIcon(state,id){
    const element=document.getElementById(id);
   // console.log(element);
    if(!element){
        return;
    }
    if(state==='ON'){
        element.classList.remove('videocall-setting-button-off');
        element.classList.add('videocall-setting-button-on');
    }else{
        element.classList.remove("videocall-setting-button-on");
        element.classList.add('videocall-setting-button-off');
    }
    if(state.toString().includes('MUTED')){
        state='MUTED';
    }
    element.classList.forEach(name =>{
        if(name.includes(id)){
            element.classList.remove(name);
        }
    });
    element.classList.add(id+'-'+state.toString().toLowerCase());
}

function lightUser(feedId,state){
    if(feedId_userId.has(feedId)) {
        const userId = feedId_userId.get(feedId);
        const userBlock=document.getElementById('user_'+userId);
        if(userBlock){
            userBlock.style['border-color']=state ? '#43db06' : '#304926';
        }
    }
}

function sendMessageToChat(){
    const text=document.getElementById('message_input').value;
    if(text==='' || !text){
        console.error('message cannot be empty');
    }
    let replyTo=null;
    if(text.includes("@")){
        const name=text.substring(text.indexOf('@')+1,text.indexOf(' ',text.indexOf('@')+1));
        console.log(name);
        const users=document.querySelectorAll('.user-participant');
        users.forEach(user=>{
            console.log(user);
            if(user.getAttribute('name').toString().includes(name)){
                replyTo=user.id.substring(user.id.indexOf('_')+1);
            }
        });
    }
    const senddate={
        text:text,
        replyTo:replyTo
    };
    fetch(window.location.href+'/addMessage',{
        method:'post',
        headers:{'Content-Type':'application/json',[csrfHeader]: csrfToken},
        body:JSON.stringify(senddate)
    }).then(response => {
        if (!response.ok) {
            let js=response.json();
            js.then(msg=> showInfoMessage(msg.message))
            return;
        } else {
            console.log('done');
        }
        return response.json();
    }).then(data => {
        console.log(data);
        document.getElementById('message_input').value='';
    });
}

function join() {
    const serverUrl = "wss://5.189.10.253:60859" //"wss://192.168.0.104:60859";
    let username;
    let user_id;
    let microstate = false;
    let camerastate = false;

    createDialogWindow().then(function (confirmed) {
        if (!confirmed) {
            window.location.href='/conferences';
            return;
        }

        fetch(window.location.href + '/join', {
            method: 'get'
        }).then(response => {
            if (!response.ok) {
                let js = response.json();
                js.then(msg => showInfoMessage(msg.message))
                return;
            }
            init();
        });
    });

    function init() {
        fetch(window.location.href + '/user/getData', {
            method: 'get'
        }).then(response => {
            if (!response.ok) {
                console.log('error');
                return;
            } else {
                console.log('done');
            }
            return response.json();
        }).then(data => {
            roomId = data.videocallsId.roomId;
            username = data.videocalluserId.id.toString();
            user_id = data.videocalluserId.id;
            microstate = data.microstate;
            camerastate = data.camstate;
            console.log(data);
            opaqueId = 'videoroom-' + roomId;
            if(!devices_start_state_updated){
                setControlButtonIcon(data.soundstate,'soundstate');
                setControlButtonIcon(data.demostate,'demostate');
                setControlButtonIcon(microstate,'microstate');
                setControlButtonIcon(camerastate,'camstate');
            }
            Janus.init({
                debug: "all",
                callback: function () {
                    startJanus(roomId, username, opaqueId, serverUrl, parseDefaultStateFromString(microstate), parseDefaultStateFromString(camerastate), user_id);
                }
            });
        })
            .catch(err => console.error(err));
    }
}

function startJanus(roomId, username, opaqueId, serverUrl,microstate=defaultStates.OFF,camerastate=defaultStates.OFF,user_id) {
    let videoroomHandle;
    let ownFeedId;

    function generateTurnCredentials(secret) {
        const unixTimeStamp = Math.floor(Date.now() / 1000) + 3600;
        const username = `${unixTimeStamp}`;
        const password = CryptoJS.HmacSHA1(username, secret).toString(CryptoJS.enc.Base64);
        return {username, credential: password};
    }

    const {username: turnUsername, credential: turnCredential} = generateTurnCredentials("mn0dye2k54");
    janus = new Janus({
        server: serverUrl,
        iceServers: [
            {urls: "stun:5.189.10.253:60868"/*"stun:192.168.0.105:60868"*/},
            {
                urls: "turn:5.189.10.253:60868?transport=udp"/*"turn:192.168.0.105:60868?transport=udp"*/,
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
                    const register = {
                        request: "join",
                        room: roomId,
                        ptype: "publisher",
                        display: username
                    };
                    videoroomHandle.send({message: register});
                },
                onmessage: function (msg, jsep) {
                    console.log("Received message:", msg);
                    if (msg.videoroom === "joined") {
                        connectToVideocallWs(roomId, user_id, videoroomHandle);
                        ownFeedId = msg.id;
                        const publishers = msg.publishers || [];

                        /*createDialogWindow().then(function (confirmed) {
                            if(!confirmed){
                                leave();
                                return;
                            }*/

                            if (publishers.length === 0) {
                                publishOwnFeed(videoroomHandle,user_id);
                            } else {
                                for (let i = 0; i < publishers.length; i++) {
                                    const publisher = publishers[i];
                                    const display = publisher.display;
                                    if (publisher.id !== ownFeedId) {
                                        console.log("👤 Новый участник:", display + ' ' + publisher.id);
                                        userId_feedId.set(Number(publisher.display), publisher.id);
                                        feedId_userId.set(publisher.id, Number(publisher.display));
                                        console.log(publisher.id, activeFeeds.size);
                                        if (activeFeeds.size < max_active_feeds || activeFeeds.has(publisher.id)) {
                                            console.log('TOGGLING VIDEO ' + publisher.id);
                                            subscribeToPublisher(publisher.id, true);
                                            activeFeeds.add(publisher.id);
                                        } else {
                                            subscribeToPublisher(publisher.id, false);
                                        }
                                    }
                                }
                                publishOwnFeed(videoroomHandle);
                            }
                       // });
                    }

                    if (msg.videoroom === "talking") {
                        const talkingFeedId = msg.id;
                        if(talkingFeedId===ownFeedId){
                            return;
                        }
                        if(activeFeeds.size>=max_active_feeds) {
                            let oldest = null;
                            for (const [id, entry] of Object.entries(activeFeeds)) {
                                if (!oldest || entry.date < oldest.date) {
                                    oldest = {id, ...entry};
                                }
                            }
                            if (oldest !== null) {
                                toggleVideo(talkingFeedId,false);
                                activeFeeds.delete(id);
                            }
                        }
                        activeFeeds.add(talkingFeedId);
                        const userId=feedId_userId.get(talkingFeedId);
                        if(parseDefaultStateFromString(getParticipantSettingState('user_'+userId,'cam'))===defaultStates.ON) {
                            toggleVideo(talkingFeedId, true);
                        }
                        if (timeoutFeeds.has(talkingFeedId)) {
                            clearTimeout(timeoutFeeds.get(talkingFeedId).entries().next().value[0]);
                            timeoutFeeds.delete(talkingFeedId);
                        }
                        lightUser(talkingFeedId, true);
                    }

                    if (msg.videoroom === "stopped-talking") {
                        const feedId = msg.id;
                        if(feedId===ownFeedId){
                            return;
                        }

                        if (subscriberHandle.has(feedId) && activeFeeds.has(feedId)) {
                            if (activeFeeds.size > max_active_feeds) {
                                console.log('UNSUBBED');
                                const timeout = setTimeout(() => {
                                    console.log('TIMEOUT');
                                    const userId=feedId_userId.get(feedId);
                                    if(parseDefaultStateFromString(getParticipantSettingState('user_'+userId,'cam'))===defaultStates.ON) {
                                        toggleVideo(feedId, false);
                                    }
                                    timeoutFeeds.delete(feedId);
                                }, 5000);

                                timeoutFeeds.set(feedId, new Map().set(timeout,Date.now()));
                            }
                            lightUser(feedId, false);
                        }
                    }

                    if (msg.videoroom === "event") {
                        if (msg.leaving || msg.unpublished) {
                            const leavingFeed = msg.leaving || msg.unpublished;
                            // console.log("🚪 Участник покинул комнату:", leavingFeed);
                            if(leavingFeed===ownFeedId){
                                return;
                            }
                            unsubscribeFromPublisher(leavingFeed);
                            if (feedId_userId.has(leavingFeed)) {
                                const userId = feedId_userId.get(leavingFeed);
                                feedId_userId.delete(leavingFeed);
                                if (userId_feedId.has(userId)) {
                                    userId_feedId.delete(userId);
                                }
                            }
                            const users=document.querySelectorAll('[class*="user-participant"]');
                            users.forEach(user=> {
                                console.log(activeFeeds.size);
                                if(activeFeeds.size>=max_active_feeds){
                                    return;
                                }
                                const state=getParticipantSettingState(user,'cam');
                                console.log(state);
                                if(state!==null){
                                    if(parseDefaultStateFromString(state)===defaultStates.ON){
                                        const userId=Number(user.id.substring(user.id.indexOf('_')+1));
                                        if(userId_feedId.has(userId)) {
                                            toggleVideo(userId_feedId.get(userId),true);
                                            activeFeeds.add(userId_feedId.get(userId));
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
                                if(publisher.id===ownFeedId){
                                    return;
                                }
                                if (!subscriberHandle.has(publisher.id)) {
                                    userId_feedId.set(Number(publisher.display), publisher.id);
                                    feedId_userId.set(publisher.id, Number(publisher.display));
                                    if(activeFeeds.size<max_active_feeds || activeFeeds.has(publisher.id)) {
                                        console.log('TOGGLING VIDEO '+publisher.id);
                                        subscribeToPublisher(publisher.id,true);
                                        activeFeeds.add(publisher.id);
                                    }else{
                                        subscribeToPublisher(publisher.id,false);
                                    }
                                }
                            }
                        }
                        if (msg.configured === "ok" && !devices_start_state_updated) {
                            devices_start_state_updated = true;
                            updateMicrophoneState(microstate);
                            updateCameraState(camerastate);
                        }
                    }

                    if (jsep) {
                        videoroomHandle.handleRemoteJsep({jsep: jsep});
                    }
                }
            });
        }
    });

    function isUserAbleToTalk(feedId) {
        if (feedId_userId.has(feedId)) {
            const userId = feedId_userId.get(feedId);
            const userParticipant = document.getElementById('user_' + userId);
            if (userParticipant) {
                const micState = getParticipantSettingState(userParticipant, 'mic');
                if (parseDefaultStateFromString(micState) === defaultStates.ON) {
                    console.log('talking allowed');
                    return true;
                }
            }
            console.log('talk user: '+userId);
        }
        console.log('🛑 Игнор talking: микрофон пользователя выключен');
        return false;
    }
}

function getCallerFunctionName() {
    const stack = new Error().stack?.split('\n');
    if (stack && stack.length >= 3) {
        const callerLine = stack[3]; // 0:Error, 1:this function, 2:called, 3:caller
        const match = callerLine.match(/at (\w+)/);
        return match ? match[1] : 'anonymous';
    }
    return 'unknown';
}

function toggleVideo(feedId, visible) {
    console.log(getCallerFunctionName());
    const handle = subscriberHandle.get(feedId);
    console.log(feedId,handle);
    //if (!handle || !handle.remoteStreams || !handle.remoteStreams.video) return;
    const videoElement = document.getElementById(feedId+'_video');
    console.log(videoElement);
  /*  if(!feedId_userId.has(feedId)){
        unsubscribeFromPublisher(feedId);
    }*/
    if(videoElement && handle && handle.remoteStreams && handle.remoteStreams.video) {
        console.log('--------------------------------------------------------------'+visible);
        updateUserDisplay(feedId,visible);
        const tracks = handle.remoteStreams.video.srcObject?.getVideoTracks();
        console.log(tracks);
        if (tracks) {
            tracks.forEach(track => track.enabled = visible);
        }
        //videoElement.muted=true;
        if(visible) {
            try {
                videoElement.play().then(() => {
                    console.log("✅ Видео воспроизводится");
                }).catch(err => {
                    console.warn("❌ Не удалось запустить видео:", err);
                });
            } catch (e) {
                showInfoMessage("Ошибка воспроизведения видео");
            }
        }
    }else{
        updateUserDisplay(feedId,false);
    }
}

function createDialogWindow() {
    return new Promise((resolve) => {
        const documentTitle = document.title || "unknown";
        const existingDialog = document.getElementById("confirm_join_dialog");
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
        document.getElementById("confirm_join_yes").onclick = () => {
            dialog.remove();
            resolve(true);
        };
        document.getElementById("confirm_join_no").onclick = () => {
            dialog.remove();
            resolve(false);
        };
    });
}

function connectToKeyloggerWebsocket(keys,sender,track,user_id){
    let reconnectDelay = 2000;
    let localWs;
    let isManuallyClosed = false;
    const user=document.getElementById('user_'+user_id);

    function connect() {
        const settings=JSON.parse(localStorage.getItem('userSettings'));
        const port= (settings!==undefined && settings.portPushToTalk!=='') ? settings.portPushToTalk : '60602';
        localWs=new WebSocket('ws://localhost:'+port);
        localWs.onopen = function (event) {
            const senddata = {
                event: 'connected',
                keys: keys
            };
            localWs.send(JSON.stringify(senddata));
            reconnectDelay=2000;
        }

        localWs.onmessage = function (event) {
            const jsdata = JSON.parse(event.data);
            if (jsdata.event === 'ping') {
                const resp = {event: 'pong'};
                localWs.send(JSON.stringify(resp));
            } else if (jsdata.event === 'pressed') {
                sender.replaceTrack(track);
                if(parseDefaultStateFromString(getParticipantSettingState(user,'mic'))===defaultStates.ON) {
                    sounds.VOICESTART.play();
                }
            } else if (jsdata.event === 'released') {
                sender.replaceTrack(null);
                if(parseDefaultStateFromString(getParticipantSettingState(user,'mic'))===defaultStates.ON) {
                    sounds.VOICEEND.play();
                }
            }else if(jsdata.event==='shutdown'){
                localWs.close();
            }
        }

        localWs.onclose = (e) => {
            if(localWs.readyState === WebSocket.CLOSED && !isLeaving){
                const iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                iframe.src = 'pttutility://launch' + (port ? '?' + new URLSearchParams({ port }) : '');
                document.body.appendChild(iframe);
            }else if (!isManuallyClosed) {
                setTimeout(connect, reconnectDelay);
                reconnectDelay+=1500;
                if(reconnectDelay>10000){
                    isManuallyClosed=true;
                    reconnectDelay=2000;
                }
            }
        };

        localWs.onerror = (e) => console.error("WebSocket error:", e);
    }

    connect();

    return {
        disconnect: ()=>{
            isManuallyClosed=true;
            localWs.close();
        }
    }
}

function publishOwnFeed(videoroomHandle,user_id) {
    navigator.mediaDevices.enumerateDevices()
        .then(function (devices) {
            const hasAudio = devices.some(device => device.kind === 'audioinput');
            const hasVideo = devices.some(device => device.kind === 'videoinput');

            if (!hasAudio && !hasVideo) {
                showInfoMessage("Нет доступных устройств");
                throw new Error("NO AVAILABLE DEVICES FOUND");
            }

            const constraints = {
                audio: hasAudio,
                video: hasVideo ? { frameRate: 30 } : false
            };

            return navigator.mediaDevices.getUserMedia(constraints)
                .then(function (stream) {
                    localMediaStream = stream;
                    Janus.attachMediaStream(document.getElementById("video_display_own"), stream);

                    const audioTrack = stream.getAudioTracks()[0];
                    const sender = null;
                    const audioLevel = 40;
                    console.warn('video enabled: '+hasVideo+'\taudio enabled:'+hasAudio);
                    videoroomHandle.createOffer({
                        media: {
                            audioRecv: false,
                            videoRecv: false,
                            audioSend: hasAudio,
                            videoSend: hasVideo,
                            video: hasVideo ? { frameRate: 30 } : false
                        },
                        stream: stream,
                        success: function (jsep) {
                            const publish = {
                                request: "publish",
                                audio: hasAudio,
                                video: hasVideo,
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
                        },
                        error: function (error) {
                            showInfoMessage("WebRTC createOffer error:");
                        }
                    });
                });
        })
        .catch(function (err) {
            showInfoMessage("Ошибка доступа к медиа-устройствам: " + err.message);
            throw new Error("NO AVAILABLE DEVICES FOUND");
        });

    function setupPushToTalk(sender, track,user_id ){
        try {
            const settings = JSON.parse(localStorage.getItem('userSettings'));
            const k = Object.keys(settingVoiceDetection);
            let isEnabled = false;
            k.forEach(key => {
                if (settings.voiceMode === key && !isEnabled) {
                    isEnabled = true;
                }
            });
            if (!isEnabled) {
                return isEnabled;
            }
            const keys = Array.from(settings.keysPushToTalk);
            if (keys.length === 0) {
                throw new Error();
            }
            const port=settings.portPushToTalk;
            wsKeylogger=connectToKeyloggerWebsocket(keys,sender,track,user_id);
        } catch (e) {
            showInfoMessage("Не заданы клавиши режима рации");
            console.log(e);
            return false;
        }
        return true;
    }
}

function subscribeToPublisher(feedId,videoAllowed=false) {
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
                        media: { audioSend: false, videoSend: false, audioRecv: true, videoRecv: true },
                        success: function (jsep) {
                            pluginHandle.send({
                                message: { request: "start" },
                                jsep: jsep
                            });
                        },
                        error: function (error) {
                           showInfoMessage("Ошибка подписки на участника");
                        }
                    });
                }
            };

            pluginHandle.onremotetrack = function (track) {
                if(!subscriberHandle.has(feedId)){
                    return;
                }
                if (!pluginHandle.remoteTracks) pluginHandle.remoteTracks = {};
                if (!pluginHandle.remoteStreams) pluginHandle.remoteStreams = {};
                pluginHandle.remoteTracks[track.kind] = track;

                const remoteId = "remote_" + feedId;
                const stream = new MediaStream([track]);
                const element = createUserBlock(track.kind === "video", track.kind === "audio", feedId);

                if (track.kind === "video") {
                    Janus.attachMediaStream(element, stream);
                    pluginHandle.remoteStreams.video = element;
                    //activeFeeds.add
                    const userParticipant=document.getElementById('user_'+feedId_userId.get(feedId));
                    toggleVideo(feedId,((activeFeeds.has(feedId) && (parseDefaultStateFromString(getParticipantSettingState(userParticipant,'cam'))===defaultStates.ON) || parseDefaultStateFromString(getParticipantSettingState(userParticipant,'demo'))===defaultStates.ON)));
                    console.log(parseDefaultStateFromString(getParticipantSettingState(userParticipant,'cam')));
                    console.log(parseDefaultStateFromString(getParticipantSettingState(userParticipant,'demo')));
                    console.log(activeFeeds.has(feedId));
                    console.log(videoAllowed);
                }

                if (track.kind === "audio") {
                    Janus.attachMediaStream(element, stream);
                    pluginHandle.remoteStreams.audio = element;
                }
            };

            pluginHandle.oncleanup = function () {
                const video = document.getElementById("remote_" + feedId+'_streams');
                if (video) {
                    video.srcObject = null;
                    video.remove();
                }
                if (pluginHandle.remoteStream) {
                    pluginHandle.remoteStream.getTracks().forEach(track => track.stop());
                }
                if(activeFeeds.has(feedId)) {
                    activeFeeds.delete(feedId);
                    console.log('DELETING ACTIVE FEED '+feedId)
                }
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
            showInfoMessage("Ошибка attach подписчика");
        }
    });
}

function unsubscribeFromPublisher(feedId){
    if (subscriberHandle.has(feedId)) {
        subscriberHandle.get(feedId).hangup();
        subscriberHandle.get(feedId).detach();
        subscriberHandle.delete(feedId);
    }
        const video = document.getElementById('remote_streams_'+feedId);
       console.log(feedId);
    console.log(video);
        if (video) {
            video.srcObject = null;
            video.remove();
        }
    if(activeFeeds.has(feedId)){
        activeFeeds.delete(feedId);
        console.log('DELETING ACTIVE FEED '+feedId);
    }
    lightUser(feedId,false);
    console.log('talk unsub: '+feedId);
}

function setUserCameraState(feedId){
    if(feedId_userId.has(feedId)){
        const userParticipant=document.getElementById('user_'+feedId_userId.get(feedId));
        const camstate=getParticipantSettingState(userParticipant,'cam');
        console.log(camstate);
        if(parseDefaultStateFromString(camstate)!==defaultStates.ON){
            updateUserDisplay(feedId,false);
        }
    }
}

function createUserBlock(video=false,audio=false,feedId){
    console.log('CREATING REMOTE STREAMS BLOCK FOR '+feedId);
    let container=document.getElementById('remote_streams_'+feedId);
    if(!container) {
        const div = document.createElement('div');
        div.id = 'remote_streams_' + feedId;
        div.className='remote-streams-zone';
        document.getElementById("remote_videos_container").appendChild(div);
        container=document.getElementById('remote_streams_'+feedId);
        //container.setAttribute('onclick','createContentwindow('+feedId+')');
    }
    let element;
    if (video) {
        element = document.getElementById(feedId + "_video");
        if (!element) {
            element=document.createElement('img');
            element.className='remote-video-image';
            element.id=feedId+'_image';
            if(feedId_userId.has(feedId)){
                const userId=feedId_userId.get(feedId);
                const avatar=document.getElementById('user_avatar_'+userId);
                if(avatar){
                    element.src=avatar.src;
                }
                element.style['display']='none';
            }
            container.appendChild(element);
            element = document.createElement("video");
            element.id = feedId + "_video";
            element.autoplay = true;
            element.playsInline = true;
            element.controls = false;
           /* if(subscriberHandle.has(feedId)){
                if(subscriberHandle.get(feedId).remoteTracks?.video) {
                    element.srcObject = new MediaStream([subscriberHandle.get(feedId).remoteTracks?.video]);
                }
            }*/
            container.appendChild(element);
            const controlContainer=document.createElement('div');
            controlContainer.style['position']='absolute';
            controlContainer.style['bottom']='0';
            controlContainer.style['right']='0';
            container.appendChild(controlContainer);
            const control_fullDisplay=document.createElement('button');
            control_fullDisplay.type='button';
            control_fullDisplay.innerText='+';
            control_fullDisplay.setAttribute('onclick','switchToFullscreen(\''+container.id+'\')');
            controlContainer.appendChild(control_fullDisplay);
        }
    } else if (audio) {
        element = document.getElementById(feedId + "_audio");
        if (!element) {
            element = document.createElement("audio");
            element.id = feedId + "_audio";
            element.style['display']='none';
            element.autoplay = true;
            element.controls = true;
            container.appendChild(element);
        }
    }
    //setUserCameraState(feedId);
    return element;
}

function switchToFullscreen(elementId){
    const video=document.getElementById(elementId);
    if(video) {
        if(isFullscreen(video)){
            video.querySelector('video').style['width']='300px';
            video.querySelector('button').innerText='+';
            if (document.exitFullscreen) {
                document.exitFullscreen();
            } else if (document.webkitExitFullscreen) {
                document.webkitExitFullscreen();
            } else if (document.msExitFullscreen) {
                document.msExitFullscreen();
            }
        }else {
            video.querySelector('video').style['width']='100%';
            video.querySelector('button').innerText='-';
            if (video.requestFullscreen) {
                video.requestFullscreen();
            } else if (video.webkitRequestFullscreen) { // Safari
                video.webkitRequestFullscreen();
            } else if (video.msRequestFullscreen) { // IE11
                video.msRequestFullscreen();
            }
        }
    }

    function isFullscreen(container) {
        return document.fullscreenElement === container ||
            document.webkitFullscreenElement === container ||
            document.msFullscreenElement === container;
    }
}

function ScreenSharing(videoroomHandle,start) {
    function startScreenWithAudioMix() {
        return Promise.all([
            navigator.mediaDevices.getDisplayMedia({
                video: {
                    frameRate: { ideal: 30, max: 50 },
                    width: { ideal: 1280 },
                    height: { ideal: 720 }
                },
                audio: true
            }),
            navigator.mediaDevices.getUserMedia({ audio: true })
        ])
            .then(([displayStream, micStream]) => {
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

                return new MediaStream([
                    ...displayStream.getVideoTracks(),
                    ...destination.stream.getAudioTracks()
                ]);
            });
    }

    function hasCamera() {
        return navigator.mediaDevices.enumerateDevices()
            .then(devices => {
                return devices.some(device => device.kind === 'videoinput');
            });
    }

    if(start) {
        startScreenWithAudioMix()
            .then((stream) => {
                replaceDisplayStreams(Promise.resolve(stream), videoroomHandle, false);
                isDemonstrationActive = true;
            })
            .catch((err) => {
                console.error("Ошибка при старте демонстрации с миксом звука:", err);
                showInfoMessage("Ошибка: " + err.message);
            });
    }else {
        hasCamera().then(hasCam => {
            const constraints = {
                audio: true,
                video: hasCam
            };
            navigator.mediaDevices.getUserMedia(constraints)
                .then(stream => {
                    replaceDisplayStreams(Promise.resolve(stream), videoroomHandle, true);
                    isDemonstrationActive = false;
                    updateCameraState(defaultStates.OFF);
                })
                .catch(err => {
                    console.error("Ошибка при получении камеры/микрофона:", err);
                    showInfoMessage("Ошибка: " + err.message);
                });
        });
    }
}

function replaceDisplayStreams(promise,videoroomHandle,camera){
    console.warn(getCallerFunctionName(),camera);
    promise.then(stream => {
        const screenTrack = stream.getVideoTracks()[0];
        const audioTracks = stream.getAudioTracks();
        const senders = videoroomHandle.webrtcStuff.pc.getSenders();
        const videoSender = senders.find(sender => sender.track && sender.track.kind === "video");
        const audioSender = senders.find(sender => sender.track && sender.track.kind === "audio");
        if (videoSender) {
            videoSender.replaceTrack(screenTrack).then(() => {
                console.log("Видео заменено на демонстрацию экрана");
                const maxBitrate=6000000;
                const bitrate=getAllDemonstrators();
                videoroomHandle.send({
                    message: {
                        request: "configure",
                        video: true,
                        bitrate: Math.min(maxBitrate/bitrate,2000000)
                    }
                });
            }).catch(err => {
                console.error(camera,err);
                showInfoMessage("Не удалось переключиться");
                updateDemonstrationState();
               // ScreenSharing(videoroomHandle,false);
            });
            const settings = screenTrack.getSettings();
            console.log(`🎥 Actual FPS: ${settings.frameRate}, resolution: ${settings.width}x${settings.height}`);
            videoroomHandle.webrtcStuff.pc.getStats().then(stats=> {
                stats.forEach(report => {
                    if (report.type === "outbound-rtp" && report.kind === "video") {
                        console.log("Sent FPS:", report.framesPerSecond);
                    }
                });
            });
        } else {
            console.warn("Видео-трек не найден");
        }

        if (audioSender && audioTracks.length > 0) {
            const audioTrack = audioTracks[0];
            audioSender.replaceTrack(audioTrack).then(() => {
                console.log("Аудио трек заменён");
            }).catch(err => {
                console.error("Ошибка при замене аудио:", err);
            });
        }

        const video = document.getElementById("video_display_own");
        if (video) {
            video.srcObject = stream;
           // video.play();
        }
        localMediaStream=stream;
        if(camera){
            const videostream=localMediaStream.getVideoTracks()[0];
            if(videostream){
                videostream.enabled=false;
            }
        }
        console.log(camera);
        screenTrack.onended = () => {
            console.log("🛑 Демонстрация экрана завершена");
            updateDemonstrationState();
        };
    }).catch(err => {
            console.error(camera,err);
            showInfoMessage("Ошибка при получении экрана");
           // updateDemonstrationState();
           // ScreenSharing(videoroomHandle,false);
        });

    function getAllDemonstrators(){
        const users=document.querySelectorAll('[class="user-participant"]');
        let count=0;
        users.forEach(user=> {
            const setting=getParticipantSettingState(user,'demo');
            const state=parseDefaultStateFromString(setting);
            if(state===defaultStates.ON){
                count++;
            }
        });
        return count || 1;
    }
}

function updateMicrophoneState(newstate=null) {
    const stream=localMediaStream;
    if (!stream) {
        showInfoMessage("Ошибка получения медиапотоков");
        return;
    }
    const audioTrack = stream.getAudioTracks()[0];
    if (audioTrack) {
        const state=newstate!==null ? newstate : !audioTrack.enabled;
        console.log(state);
        console.log(newstate);
        audioTrack.enabled =isStringDefaultStates(state.toString()) ? state===defaultStates.ON : state;
        console.log('talk: '+state);
        console.log("🎤 Микрофон", audioTrack.enabled ? "включен" : "выключен");
        updateUserSettings( state, Actions.MICROPHONE,true);
    }
}

function updateCameraState(newstate=null) {
    const stream=localMediaStream;
    if (!stream) {
        showInfoMessage("Ошибка получения медиапотоков");
        return;
    }
    const videoTrack = stream.getVideoTracks()[0];
    if (videoTrack) {
        const state=(newstate!==null && isStringDefaultStates(newstate.toString())) ? newstate : !videoTrack.enabled;
        videoTrack.enabled = (newstate!==null && isStringDefaultStates(newstate.toString())) ? state===defaultStates.ON : state;
        console.log("🎤 Камера", videoTrack.enabled ? "включена" : "выключена");
        console.log(newstate,state);
        updateUserSettings((newstate!==null && isStringDefaultStates(newstate.toString())) ? state===defaultStates.ON : state === true, Actions.CAMERA,true);
    }
}

function updateSoundState(newstate=null){
    try {
        updateUserSettings(isSoundMuted, Actions.SOUND, true);
    }catch (e) {
        return;
    }
    console.log('sound muted: '+isSoundMuted);
    userId_feedId.forEach(value => {
        const element=document.getElementById(value+'_audio');
        if(element){
            element.muted=newstate===null ? isSoundMuted:newstate;
        }
    });
}

function updateDemonstrationState(){
    console.warn(getCallerFunctionName());
    try{
        updateUserSettings(null,Actions.DEMONSTRATION,true)
    }catch (e) {
        return;
    }
}

function updateRemoteMicrophone(id,forAll,element){
    const feedId=userId_feedId.get(id);
    console.log(feedId);
    const remoteAudio = document.getElementById(feedId + '_audio');
    const userParticipant=document.getElementById('user_'+id);
    let newstate=null;
    if(userParticipant){
        const settingState=getParticipantSettingState(userParticipant,'mic');
        newstate=settingState!==null ? parseDefaultStateFromString(settingState) : settingState;
    }
    if(isStringDefaultStates(newstate)){
        newstate= newstate===defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
    }
    if (remoteAudio) {
        const state= remoteAudio.muted;
        if (forAll) {
            try {
                updateUserSettings(newstate, Actions.MICROPHONE, false, id);
            }catch (e) {
                return;
            }
            if(isStringDefaultStates(newstate)){
                element.innerText = newstate===defaultStates.MUTED_BY_ADMIN ? 'Включить микрофон для всех' : 'Заглушить для всех';
            }else {
                element.innerText = newstate ? 'Включить микрофон для всех' : 'Заглушить для всех';
            }
        } else {
            remoteAudio.muted = !state;
            element.innerText = remoteAudio.muted ? 'Включить микрофон' : 'Заглушить';
        }
        //remoteAudio.muted = !state;
    }
}

function updateRemoteCamera(id,forAll,element){
    const feedId=userId_feedId.get(id);
    console.log(feedId);
    const remoteVideo = document.getElementById(feedId + '_video');
    const userParticipant=document.getElementById('user_'+id);
    console.log('id: '+id,userParticipant);
    let newstate=null;
    if(userParticipant){
        const settingState=getParticipantSettingState(userParticipant,'cam');
        console.log(settingState);
        newstate=settingState!==null ? parseDefaultStateFromString(settingState) : settingState;
    }
    console.log(newstate,forAll,isStringDefaultStates(newstate));
    if(remoteVideo) {
        if(isStringDefaultStates(newstate)){
            newstate= newstate===defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
        }
        let visible =(newstate!==null && forAll) ? newstate :  remoteVideo.style['display'] === 'none';
        console.log(visible);
        if (forAll) {
            try {
                updateUserSettings(visible, Actions.CAMERA, false, id);
            } catch (e) {
                return;
            }
            if(isStringDefaultStates(visible)) {
                element.innerText = (visible === defaultStates.MUTED_BY_ADMIN ? 'Включить' : 'Отключить') + ' камеру для всех';
                if(newstate!==null){
                    visible=!visible;
                }
            }else{
                element.innerText = (visible ? 'Включить' : 'Отключить') + ' камеру для всех';
            }
        } else {
            visible = remoteVideo.style['display'] === 'none';
            console.log(visible);
            element.innerText = (visible ? 'Отключить' : 'Включить') + ' камеру';
            if(!visible){
                remoteVideo.classList.add('disabled');
            }else{
                remoteVideo.classList.remove('disabled');
            }
        }
        updateUserDisplay(feedId,(isStringDefaultStates(newstate) && forAll) ? newstate===defaultStates.ON : visible);
    }
}

function updateRemoteSound(id,element){
    const userParticipant=document.getElementById('user_'+id);
    let newstate=null;
    if(userParticipant){
        const settingState=getParticipantSettingState(userParticipant,'snd');
        newstate=settingState!==null ? parseDefaultStateFromString(settingState) : settingState;
    }
    if(isStringDefaultStates(newstate)){
        newstate= newstate===defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
    }
    try {
        updateUserSettings(newstate, Actions.SOUND, false, id);
    }catch (e) {
        return;
    }
    if(newstate!==defaultStates.MUTED_BY_ADMIN){
        element.innerText='Отключить звук';
    }else{
        element.innerText='Включить звук';
    }
}

function updateRemoteDemonstration(id,element){
    const userParticipant=document.getElementById('user_'+id);
    let newstate=null;
    if(userParticipant){
        const settingState=getParticipantSettingState(userParticipant,'demo');
        newstate=settingState!==null ? parseDefaultStateFromString(settingState) : settingState;
    }
    if(isStringDefaultStates(newstate)){
        newstate= newstate===defaultStates.MUTED_BY_ADMIN ? defaultStates.OFF : defaultStates.MUTED_BY_ADMIN;
    }
    try {
        updateUserSettings(newstate, Actions.DEMONSTRATION, false, id);
    }catch (e) {
        return;
    }
    if(newstate!==defaultStates.MUTED_BY_ADMIN){
        element.innerText='Запретить демонстрацию экрана';
    }else{
        element.innerText='Разрешить демонстрацию экрана';
    }
}

function banUser(id){
    console.log('user '+id+' banned');
    updateUserSettings(null,Actions.BAN,false,id);
}

function updateUserDisplay(feedId,visible){
    const img = document.getElementById(feedId + '_image');
    const remoteVideo=document.getElementById(feedId+'_video');
    console.log(img);
    console.log(remoteVideo);
    if(remoteVideo) {
        if(remoteVideo.classList.contains('disabled') && visible){
            return;
        }
        remoteVideo.style['display'] = visible ? '' : 'none';
    }
    if (img) {
        console.log('image: '+visible);
        if (visible) {
            img.style['display']='none';
        } else {
            img.style['display']='';
        }
    }
   // console.log(remoteVideo);
}

function createContentwindow(feedId){
    window_count++;
    const win=document.createElement('div');
    win.id='content_window_'+window_count;
    document.body.appendChild(win);
    const xBtn=document.createElement('span');
    xBtn.id='close_window_btn_'+window_count;
    xBtn.className='content-window-close-icon';
    xBtn.addEventListener('click', closeWindow);
    xBtn.innerText='X';
    win.appendChild(xBtn);
    const streamContainer=document.getElementById('remote_streams_'+feedId);
    if(streamContainer){
        console.log('done');
        win.appendChild(streamContainer);
        streamContainer.setAttribute('onclick','');
     //   document.getElementById('remote_streams_'+feedId).remove();
    }
    console.log(streamContainer);

    let file_h=parseInt(streamContainer.querySelector('video').style['width']);
    let file_w=400;//streamContainer.querySelector('video').style['height'];

    function Resize(file_w,file_h,win){
        const maxWidth = window.innerWidth;
        const maxHeight = window.innerHeight;
        if(contentwindow_aspectratio.has(win.id)){
          contentwindow_aspectratio.set(win.id,0);
        }
        contentwindow_aspectratio.set(win.id,(file_w / file_h));
        console.log('w: '+file_w);
        console.log('h: '+file_h);
        if (file_h > maxHeight || file_w > maxWidth) {
            if (file_w > file_h) {
                file_w = maxWidth;
                file_h = maxWidth / contentwindow_aspectratio.get(win.id);
            } else {
                file_h = maxHeight;
                file_w = maxHeight * contentwindow_aspectratio.get(win.id);
            }
            if (file_h > maxHeight) {
                file_h = maxHeight;
                file_w = maxHeight * contentwindow_aspectratio.get(win.id);
            }
            if (file_w > maxWidth) {
                file_w = maxWidth;
                file_h = maxWidth / contentwindow_aspectratio.get(win.id);
            }
        }
        win.style.height = file_h + 'px';
        win.style.width = file_w + 'px';
        win.style.top=(window.innerHeight/2-file_h/2)+'px';
        win.style.left=(window.innerWidth/2-file_w/2)+'px';
    }

    Resize(file_w,file_h,win);

    win.style.display='block';
    win.style.position='fixed';
    addMovableProperty(win,win.id);
    addContentwindowResizeEvent(win);

    function closeWindow(e){
        e.preventDefault();
        const streamsContainer=document.getElementById('remote_videos_container');
        win.querySelector('.remote-streams-zone').setAttribute('onclick','createContentwindow('+feedId+')');
        streamsContainer.appendChild(win.querySelector('.remote-streams-zone'));
        win.remove();
    }
}

function addMovableProperty(element,id){
    let pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0;
    element.onmousedown = dragMouseDown;

    function dragMouseDown(e) {
        //console.log(e.type);
        if(e.type==="touchstart"){
            //  closeContentwindow();
        }
        else {
            e.preventDefault();
            pos3 = e.clientX;
            pos4 = e.clientY;
            document.onmouseup = closeDragElement;
            document.onmousemove = elementDrag;
        }
    }

    function elementDrag(e) {
        let client_x=null;
        let client_y=null;
        client_x =e.clientX;
        client_y =e.clientY;
        //console.log(client_x);
        pos1 = pos3 - client_x;
        pos2 = pos4 - client_y;
        pos3 = client_x;
        pos4 = client_y;
        element.style.top = (element.offsetTop - pos2) + "px";
        element.style.left = (element.offsetLeft - pos1) + "px";
    }

    function closeDragElement() {
        document.onmouseup = null;
        document.onmousemove = null;
    }

}

function addContentwindowResizeEvent(win){
    win.addEventListener('wheel',function (event){
        event.preventDefault();
        const window_element=event.currentTarget;
        let new_winw,new_winh;
        if(contentwindow_aspectratio.has(win.id)) {
            const ar=contentwindow_aspectratio.get(win.id);
            if (ar=== 1) {
                const delta = event.deltaY < 0 ? 1.1 : 0.9;
                new_winw = window_element.offsetWidth * delta;
            } else {
                if (event.deltaY < 0) {
                    console.log('increase');
                    new_winw = ar > 1 ? window_element.offsetWidth * ar : window_element.offsetWidth / ar;
                } else {
                    console.log(window_element.offsetWidth * ar);
                    console.log(window_element.offsetWidth / ar);
                    new_winw = ar < 1 ? window_element.offsetWidth * ar : window_element.offsetWidth / ar;
                }
            }
            new_winh = new_winw / ar;
        }
        console.log('w: '+new_winw);
        console.log('h: '+new_winh);
        //console.log('a: '+new_winw/new_winh);
        window_element.style.width = new_winw + 'px';
        window_element.style.height = new_winh + 'px';
    });
}

function getUserNames() {
    return Array.from(document.querySelectorAll('.user-participant')).map(element => ({
        id: element.id,
        name: element.getAttribute('name'),
    }));
}

function showParticipantList(matches, position) {
    const dropdown = document.getElementById('participants_list');

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
    const input = document.getElementById('message_input');
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

function addMessageInputEventListener(){
    const input = document.getElementById('message_input');
    const dropdown = document.getElementById('participants_list');
    input.addEventListener('input', (e) => {
        const cursorPos = input.selectionStart;
        const text = input.value.slice(0, cursorPos);
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
            dropdown.style.display = 'none';
        }
    });
}

function leave(withRequest=true){
    const remoteVideos = document.querySelectorAll("video[id^='remote_']");
    remoteVideos.forEach(video => {
        const feedId = Number(video.id.replace("_video", ""));
        video.remove();
        if(subscriberHandle.has(feedId)) {
            subscriberHandle.get(feedId).detach();
            subscriberHandle.delete(feedId);
        }
    });
    if (janus) {
        janus.destroy();
    }
    if (localMediaStream) {
        localMediaStream.getTracks().forEach(track => track.stop());
    }
    if(wsKeylogger!==null){
        wsKeylogger.disconnect();
    }
    const id=document.getElementById('videocall_id').value;
    if(withRequest) {
        isLeaving=true;
        fetch('/videocall/' + id + '/leave?' + new URLSearchParams({reason: 'EXIT'}), {
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

function addScrollEventListenerToRemoteVideosContainer(){
    document.getElementById('remote_videos_container').addEventListener('wheel', function (event) {
        if (event.deltaY !== 0) {
            event.preventDefault();
            document.getElementById('remote_videos_container').scrollLeft += event.deltaY;
        }
    }, { passive: false });
}

function setSoundsVolume(){
    const settingsRaw=localStorage.getItem('userSettings');
    if(settingsRaw){
        const settings=JSON.parse(settingsRaw);
        if(settings.soundsVolume){
            Object.values(sounds).forEach(snd => snd.volume=settings.soundsVolume/100);
        }
    }
}

document.addEventListener('DOMContentLoaded',function (){
    join();
    addScrollEventListenerToRemoteVideosContainer();
    addSettingsMenuListener('settings-block');
    document.getElementById('message_input').addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            sendMessageToChat();
        }
    });
    document.addEventListener('click', (e) => {
        const dropdown=document.getElementById('participants_list');
        const input = document.getElementById('message_input');
        if (!dropdown.contains(e.target) && e.target !== input) {
            dropdown.style.display = 'none';
        }
    });
    addMessageInputEventListener();
    setSoundsVolume();
},false);

window.addEventListener("beforeunload", () => {
    document.querySelectorAll("[id^='remote_streams_']").forEach(el => el.remove());
    subscriberHandle.clear();
    userId_feedId.clear();
    feedId_userId.clear();
    activeFeeds.clear();
    timeoutFeeds.clear();
    leave(false);
    if(isLeaving){return;}
    const data = new URLSearchParams({
        reason: 'RELOAD',
        csrf: csrfToken
    });
    navigator.sendBeacon(window.location.href+"/leave?"+new URLSearchParams({reason:'RELOAD'}),data);
});