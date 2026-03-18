import {Actions, defaultStates, iconsVideocallUrl} from "./constants";

export class UiManager {
    constructor() {
    }

    static updateIcon(className, state, container) {
        let icon = container.querySelector(`[class*='${className}']`);
        state = state.toString().toLowerCase();
        if (state.includes('MUTED')) {
            state = 'MUTED';
        }
        if (!icon) {
            icon = this.createIcon(className, state, container.querySelector('.user-participant-icons'));
        }
        icon.classList.forEach(name => {
            if (name.includes(className)) {
                icon.classList.remove(name);
            }
        });
        icon.classList.add(className + state.toLowerCase());
        icon.src = `${iconsVideocallUrl}/${className}/${state}.png`;
    }

    static createIcon(className, state, container) {
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

    static createSettingsBlock(container, participant) {
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
            ['Выгнать', `banUser('${participant.id}')`]
        ]);

        const div1 = document.createElement('div');
        div1.style['text-align'] = 'end';
        div1.className = 'user-participant-settings';
        container.appendChild(div1);
        const span = document.createElement('span');
        span.id = 'button_settings_' + participant.id;
        span.className = 'settings-btn';
        span.setAttribute('onclick', `showSettingsMenu('${participant.id}')`);
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

    static lightUser(userId, state) {
        if (userId) {
            const userBlock = document.querySelector(`#user_${userId}`);
            if (userBlock) {
                userBlock.style['border-color'] = state ? '#43db06' : '#304926';
            }
        }
    }

    static createDialogWindow() {
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

    static insertParticipantIntoList(name) {
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

    static updateUserDisplay(feedId, visible) {
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

    static createUserBlock(video = false, audio = false, feedId,userId) {
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
                if (userId) {
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

    static createUserParticipantBlock(participant,feedId) {
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
            const element = this.createSettingsBlock(div1, participant);
            console.log('map has key:' + feedId);
            if (feedId) {
                const img = document.querySelector(`#${feedId}_image`);
                if (img) {
                    img.src = `/useravatar/${participant.id}`;
                }
            }
            this.setParticipantPropertiesIcons(element, participant);
        }
    }

    static setParticipantPropertiesIcons(container, participant) {
        const div = document.createElement('div');
        div.className = 'user-participant-icons';
        container.appendChild(div);
        if (participant.microphone !== undefined) {
            this.createIcon(Actions.MICROPHONE, participant.microphone, div);
        }
        if (participant.camera !== undefined) {
            this.createIcon(Actions.CAMERA, participant.camera, div);
        }
        if (participant.sound !== undefined) {
            this.createIcon(Actions.SOUND, participant.sound, div);
        }
        if (participant.demo !== undefined) {
            this.createIcon(Actions.DEMONSTRATION, participant.demo, div);
        }
    }

    static setControlButtonIcon(state, id) {
        const element = document.querySelector(`#${id}`);
        state = state.toString().toLowerCase();
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
        element?.classList.add(`${id}-${state}`);
    }
}