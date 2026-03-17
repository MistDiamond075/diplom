import {defaultStates} from "./constants.js";

export class VideoCallUtils{
    static isDefaultState(str) {
        return Object.values(defaultStates).includes(str);
    }

    static parseDefaultState(str) {
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

    static getUserNames() {
        return Array.from(document.querySelectorAll('.user-participant')).map(element => ({
            id: element.id,
            name: element.getAttribute('name'),
        }));
    }

    static getParticipantSettingState(userContainer, settingClassName) {
        const settingsList = userContainer.querySelector('.user-participant-icons');
        let state = null;
        settingsList?.childNodes.forEach(setting => {
            const list = setting.classList;
            list.forEach(name => {
                if (name.toString().includes(settingClassName)) {
                    state = name.substring(name.indexOf('_') + 1);
                }
            });
        });
        return state;
    }

    static getAllDemonstrators() {
        const users = document.querySelectorAll('[class="user-participant"]');
        let count = 0;
        users.forEach(user => {
            const setting = VideoCallUtils.getParticipantSettingState(user, 'demo');
            const state = VideoCallUtils.parseDefaultState(setting);
            if (state === defaultStates.ON) {
                count++;
            }
        });
        return count || 1;
    }
}