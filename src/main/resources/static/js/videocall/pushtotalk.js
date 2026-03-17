import {defaultStates, sounds} from "./constants";
import {VideoCallUtils} from "./utils.js";

export class PushToTalkManager{
    constructor() {
        this.reconnectDelay = 2000;
        this.localWs = undefined;
    }

    connectToKeyloggerWebsocket(keys, sender, track, user_id) {
        let isManuallyClosed = false;
        const user = document.querySelector(`#user_${user_id}`);

        this.connect(keys, sender, track,isManuallyClosed,user);

        return {
            disconnect: () => {
                isManuallyClosed = true;
                this.localWs.close();
            }
        }
    }

    connect(keys, sender, track,isManuallyClosed,user) {
        const settings = JSON.parse(localStorage.getItem('userSettings'));
        const port = settings?.portPushToTalk !== '' ? settings?.portPushToTalk : '60602';
        this.localWs = new WebSocket('ws://localhost:' + port);
        this.localWs.onopen = function () {
            const senddata = {
                event: 'connected',
                keys: keys
            };
            this.localWs.send(JSON.stringify(senddata));
            this.reconnectDelay = 2000;
        }

        this.localWs.onmessage = async function (event) {
            const jsdata = JSON.parse(event.data);
            if (jsdata.event === 'ping') {
                const resp = {event: 'pong'};
                this.localWs.send(JSON.stringify(resp));
            } else if (jsdata.event === 'pressed') {
                await sender.replaceTrack(track);
                if (VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(user, 'mic')) === defaultStates.ON) {
                    await sounds.VOICESTART.play();
                }
            } else if (jsdata.event === 'released') {
                await sender.replaceTrack(null);
                if (VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(user, 'mic')) === defaultStates.ON) {
                    await sounds.VOICEEND.play();
                }
            } else if (jsdata.event === 'shutdown') {
                this.localWs.close();
            }
        }

        this.localWs.onclose = () => {
          /*  if (this.localWs.readyState === WebSocket.CLOSED && !isLeaving) {
                const iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                iframe.src = 'pttutility://launch' + (port ? '?' + new URLSearchParams({port}) : '');
                document.body.appendChild(iframe);
            } else*/ if (!isManuallyClosed) {
                setTimeout(this.connect, this.reconnectDelay);
                this.reconnectDelay += 1500;
                if (this.reconnectDelay > 10000) {
                    isManuallyClosed = true;
                    this.reconnectDelay = 2000;
                }
            }
        };

        this.localWs.onerror = (e) => console.error("WebSocket error:", e);
    }

    setupPushToTalk(sender, track, user_id) {
        try {
            const settings = JSON.parse(localStorage.getItem('userSettings'));
            if (settings.voiceMode !== 'PUSH_TO_TALK') {
                return false;
            }
            const keys = Array.from(settings.keysPushToTalk);
            if (keys.length === 0) {
                showInfoMessage("Не заданы клавиши режима рации");
                return false;
            }
            this.connectToKeyloggerWebsocket(keys, sender, track, user_id);
        } catch (e) {
            showInfoMessage("Ошибка инициализации Push-To-Talk");
            console.error(e.message);
            return false;
        }
        return true;
    }

    disconnect(){
        this.localWs.disconnect();
    }
}