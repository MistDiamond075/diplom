const allSettingsType={SETCSS:'SETCSS',SETSETTINGS:'SETSETTINGS'};
const settingVoiceDetection={VOICE:'По голосу',PUSH_TO_TALK:'Режим рации'};
const settingUsernameDisplay={USERNAME:'Логин',FULLNAME:'ФИО'};
const settingTheme={LIGHT:'Светлая',DARK:'Тёмная',CONTRAST:'Контрастная'};

function getUserSettingsFromSv(){
    if(!localStorage.getItem('userSettings')){
        fetch('/usersettings',{
            method:'get'
        }).then(response =>{
            if(!response.ok) {
                if (response.status !== 404) {
                    showInfoMessage("Непредвиденная ошибка");
                    return;
                }
            }
            return response.json();
        }).then(data =>{
            localStorage.setItem('userSettings',JSON.stringify(data));
        });
    }
}

function parseKey(event){
    const doubleKeys=['Alt','Shift','Control'];
    const numpadKeys=['Numpad1','Numpad2','Numpad3','Numpad4','Numpad5','Numpad6','Numpad7','Numpad8','Numpad9','Numpad0','NumpadMinus','NumpadMultiply','NumpadDecimal',
        'NumpadDivide', 'NumpadEnter'];
    return (doubleKeys.includes(event.key) || numpadKeys.includes(event.code)) ? event.code : event.key;
}

function showSettingsAll(event){
    event.stopPropagation();
    event.preventDefault();
    const settingsPanel=document.querySelector('.allsettings');
    console.log(settingsPanel);
    if(settingsPanel){
        settingsPanel.style['display']=settingsPanel.style['display']==='none' ? '' : 'none';
    }
    document.addEventListener('click',(event)=>{
        let visible=false;
        for(let i in settingsPanel.children) {
            if (i===event.target) {
                visible = true;
            }
        }
        settingsPanel.style.display = visible ? '' : 'none';
    });
}


function addSettingsContainerListener(){
    document.addEventListener("click", function(event) {
        const settings = document.querySelector(".setting-container");
        if (settings.style.display !== "none" && !settings.contains(event.target)) {
            settings.style.display = "none";
        }
    });
}

function fillSettingsContainer(type){
    const settingsContainer=document.querySelector('.setting-container');
    const elementsToAdd=[];
    settingsContainer.innerHTML='';
    settingsContainer.style['display']='';
    const header=document.createElement('span');
    header.className='setting-container-header';
    const btn=document.createElement('button');
    btn.type='button';
    btn.className='allbuttons';
    btn.style.marginTop='5px';
    btn.style.alignSelf='center';
    btn.innerText='Сохранить';
    switch (type){
        case allSettingsType.SETCSS:{
            const textarea=document.createElement('textarea');
            textarea.className='settings-setcss-area';
            textarea.id='custom_css_text';
            getCustomCssFromSv().then(data=>textarea.value=data);
            btn.setAttribute('onclick','sendCustomCssToSv()');
            elementsToAdd.push(new Pair(undefined,textarea));
            header.innerText='Настройка CSS';
        break;}
        case allSettingsType.SETSETTINGS:{
            const voiceSelector=createLabeledElement('select','settings-selector','settings_voice_mode','Голосовой ввод');
            for(let i in Object.entries(settingVoiceDetection)) {
                console.log(Object.entries(settingVoiceDetection)[i]);
                createOptionForSelector(voiceSelector.second, Object.entries(settingVoiceDetection)[i]);
            }
            btn.setAttribute('onclick','sendSettingsToSv()');
            elementsToAdd.push(voiceSelector);
            const pttKeysContainer=document.createElement('div');
            pttKeysContainer.className='settings-ptt-keys';
            const pttKeyInput=addPushToTalkKeyInput();
            const div2=document.createElement('div');
            div2.className='settings-ptt-keys-inputs';
            const pttKeysContainerInnerDiv=document.createElement('div');
            pttKeysContainerInnerDiv.className='settings-ptt-keys-input-container';
            pttKeysContainerInnerDiv.appendChild(pttKeyInput.first);
            pttKeysContainerInnerDiv.appendChild(pttKeyInput.second);
            div2.appendChild(pttKeysContainerInnerDiv);
            const pttKeyInputAdd=document.createElement('button');
            pttKeyInputAdd.type='button';
            pttKeyInputAdd.className='settings-ptt-keys-add-input';
            pttKeyInputAdd.setAttribute('onclick','addPushToTalkKeyInput()');
            pttKeyInputAdd.innerText='+';
            const pttKeyInputRemove=document.createElement('button');
            pttKeyInputRemove.type='button';
            pttKeyInputRemove.className='settings-ptt-keys-add-input';
            pttKeyInputRemove.setAttribute('onclick','removePushToTalkKeyInput()');
            pttKeyInputRemove.innerText='-';
            div2.appendChild(pttKeyInputAdd);
            div2.appendChild(pttKeyInputRemove);
            pttKeysContainer.appendChild(div2);
            elementsToAdd.push(new Pair(undefined,pttKeysContainer));
            const fontSize=createLabeledElement('input','settings-input','settings_font_size','Размер шрифта');
            fontSize.second.type='number';
            fontSize.second.min=0;
            fontSize.second.max=120;
            elementsToAdd.push(fontSize);
            const usernameDisplaySelector=createLabeledElement('select','settings-selector','settings_user_display_mode','Отображение пользователей');
            for(let i in Object.entries(settingUsernameDisplay)) {
                createOptionForSelector(usernameDisplaySelector.second, Object.entries(settingUsernameDisplay)[i]);
            }
            elementsToAdd.push(usernameDisplaySelector);
            const themeSelector=createLabeledElement('select','settings-selector','settings_theme','Тема');
            for(let i in Object.entries(settingTheme)) {
                createOptionForSelector(themeSelector.second, Object.entries(settingTheme)[i]);
            }
            elementsToAdd.push(themeSelector);
            header.innerText='Глобальные настройки';
            if(localStorage.getItem('userSettings')){
                const existedSettings=JSON.parse(localStorage.getItem('userSettings'));
                voiceSelector.second.value=existedSettings.voiceMode;
                usernameDisplaySelector.second.value=existedSettings.userDisplay;
                if(existedSettings.keysPushToTalk.length>0) {
                    pttKeyInput.second.value = existedSettings.keysPushToTalk[0];
                    if(existedSettings.keysPushToTalk.length>1){
                        const pttKeyInput2=addPushToTalkKeyInput();
                        pttKeyInput2.second.style.marginBottom='0px';
                        pttKeyInput2.second.value=existedSettings.keysPushToTalk[1];
                        pttKeyInput2.second.id='settings_ptt_key_input_'+1;
                        pttKeyInput2.first.setAttribute('for',pttKeyInput2.second.id);
                        pttKeysContainerInnerDiv.appendChild(pttKeyInput2.first);
                        pttKeysContainerInnerDiv.appendChild(pttKeyInput2.second);
                    }
                }
            }
        break;}
    }
    settingsContainer.appendChild(header);
    for(let i in elementsToAdd){
        if(elementsToAdd[i].first!==undefined) {
            settingsContainer.appendChild(elementsToAdd[i].first);
        }
        settingsContainer.appendChild(elementsToAdd[i].second);
    }
    settingsContainer.appendChild(btn);


    function createOptionForSelector(selector,[first,second]){
        const opt=document.createElement('option');
        opt.textContent=second;
        opt.value=first;
        selector.appendChild(opt);
    }

}

function createLabeledElement(type,className,id,name){
    const element=document.createElement(type);
    element.className=className;
    element.id=id;
    const label=document.createElement('label');
    label.setAttribute('for',id);
    label.innerText=name;
    return new Pair(label,element);
}

function addPushToTalkKeyInput(){
    const inputs=document.querySelectorAll('[id*="settings_ptt_key_input"]');
    const count=inputs.length;
    if(count>=2){
        return null;
    }
    const pttKeyInput=createLabeledElement('input','settings-input','settings_ptt_key_input_'+count,'Клавиша режима рации');
    addKeyRememberInputEventListener(pttKeyInput.second);
    pttKeyInput.second.type='text';
    pttKeyInput.second.setAttribute('readonly','true');
    pttKeyInput.second.title='Нажмите на это поле ввода и нажмите любую клавишу';
    pttKeyInput.second.style.marginBottom='0px';
    if(count===0) {
        return pttKeyInput;
    }else{
        const inputsContainer=document.querySelector('.settings-ptt-keys');
        if(inputsContainer){
            let pttKeysContainerInnerDiv=document.querySelector('.settings-ptt-keys-input-container');
            console.log(pttKeysContainerInnerDiv);
            if(!pttKeysContainerInnerDiv) {
                pttKeysContainerInnerDiv = document.createElement('div');
                pttKeysContainerInnerDiv.className = 'settings-ptt-keys-input-container';
                inputsContainer.appendChild(pttKeysContainerInnerDiv);
            }
            pttKeysContainerInnerDiv.appendChild(pttKeyInput.first);
            pttKeysContainerInnerDiv.appendChild(pttKeyInput.second);
        }
        return null;
    }
}

function removePushToTalkKeyInput(){
    const inputs=document.querySelectorAll('[id*="settings_ptt_key_input"]');
    if(inputs && inputs.length>1){
        const input=inputs[inputs.length-1];
        const id=input.id;
        input.remove();
        console.log(id);
        const label=document.querySelector(`label[for=${id}]`);
        if(label){
            label.remove();
        }
    }
}

function sendCustomCssToSv(){
    const cssText=document.getElementById('custom_css_text').value;
    const senddata={
        text:cssText
    };
    fetch('/addUserCss',{
        method:'post',
        headers:{
            [csrfHeader]: csrfToken,
            'Content-Type':'application/json'
        },
        body:JSON.stringify(senddata)
    }).then(response =>{
        if(!response.ok){
            showInfoMessage("Непредвиденная ошибка");
        }else {
            window.location.reload();
        }
    });
}

function sendSettingsToSv(){
    const voiceMode=document.getElementById('settings_voice_mode').value;
    const pttKeysArray=document.querySelectorAll('[id*="settings_ptt_key_input"]');
    const pttKeys=[];
    pttKeysArray.forEach(key => pttKeys.push(key.value));
    const userDisplayMode=document.getElementById('settings_user_display_mode').value;
    const senddata={
        voiceMode:voiceMode,
        userDisplay:userDisplayMode,
        keysPushToTalk:pttKeys
    }
    fetch('/addUserSettings',{
        method:'post',
        headers:{
            [csrfHeader]: csrfToken,
            'Content-Type':'application/json'
        },
        body:JSON.stringify(senddata)
    }).then(response =>{
        if(!response.ok){
            showInfoMessage("Непредвиденная ошибка");
        }else {
            showInfoMessage("Изменения сохранены",false);
            return response.json();
        }
    }).then(data =>{
        localStorage.setItem('userSettings',JSON.stringify(data));
    });
}

function getCustomCssFromSv(){
    return fetch('/usercss',{
        method:'get'
    }).then(response =>{
        if(!response.ok){
            if(response.status!==404) {
                showInfoMessage("Непредвиденная ошибка");
            }
            return "";
        }
        return response.text();
    });
}

function addKeyRememberInputEventListener(input){
    input.addEventListener('keydown', (event) => {
        event.preventDefault();
        console.log(event.code);
        input.value =parseKey(event);
    });
}

document.addEventListener('DOMContentLoaded',function (){
    getUserSettingsFromSv();
},false);