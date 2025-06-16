let userOldData=null;
let banOldData=null;

function collectOriginalData(userId) {
    console.log(userId);
    return  {
        id: userId,
        login: document.getElementById(`user_login_${userId}`).value,
        fname: document.getElementById(`user_fname_${userId}`).value,
        lname: document.getElementById(`user_lname_${userId}`).value,
        sname: document.getElementById(`user_sname_${userId}`).value,
        date: document.getElementById(`user_date_${userId}`).value,
        email: document.getElementById(`user_email_${userId}`).value,
        question: document.getElementById(`user_question_${userId}`).innerText,
        answer: document.getElementById(`user_answer_${userId}`).innerText,
        card: document.getElementById(`user_card_${userId}`).value,
        group: document.getElementById(`user_group_${userId}`).value,
        roles: document.getElementById(`user_roles_${userId}`).innerText
    };
}

function restoreData() {
    if (!userOldData) return;

    const userId = userOldData.id;
    document.getElementById(`user_login_${userId}`).value = userOldData.login;
    document.getElementById(`user_fname_${userId}`).value = userOldData.fname;
    document.getElementById(`user_lname_${userId}`).value = userOldData.lname;
    document.getElementById(`user_sname_${userId}`).value = userOldData.sname;
    document.getElementById(`user_date_${userId}`).value = userOldData.date;
    document.getElementById(`user_email_${userId}`).value = userOldData.email;
    document.getElementById(`user_question_${userId}`).innerText = userOldData.question;
    document.getElementById(`user_answer_${userId}`).innerText = userOldData.answer;
    document.getElementById(`user_card_${userId}`).value = userOldData.card;
    document.getElementById(`user_group_${userId}`).value = userOldData.group;
    document.getElementById(`user_roles_${userId}`).innerText = userOldData.roles;
}

function addLabeledElement(data,datatype,labelText, className, value, parent, idSuffix,elementName='span',type=null) {
    const container=document.createElement('div');
    container.style['margin-right']='2px';
    parent.appendChild(container);
    const id = datatype+'_'+idSuffix+'_'+data.id;
    const label = document.createElement('label');
    label.setAttribute('for', id);
    label.innerText = labelText + ': ';
    container.appendChild(label);

    const element = document.createElement(elementName);
    if(elementName!=='input' && elementName!=='textdata') {
        element.innerText = value;
    }else{
        element.type= type!==null ? type : 'text';
        element.value=value;
        element.setAttribute('readonly','true');
    }
    element.className = className;
    element.id = id;
    addScrollEventListener(element);
    container.appendChild(element);
}

function createUserDataContainer(user, getterinfo) {
    const divBase=document.createElement('div');
    divBase.className='user-container';
    getterinfo.appendChild(divBase);
    const div = document.createElement('div');
    div.id = 'user_'+user.id;
    div.className = 'user-container-block';
    divBase.appendChild(div);

    const img = document.createElement('img');
    img.src = '/useravatar/' + user.id;
    img.id = 'user_avatar_' + user.id;
    div.appendChild(img);

    const div1 = document.createElement('div');
    div1.className = 'user-desc';
    div1.id='user_desc_'+user.id;
    div.appendChild(div1);

    addLabeledElement(user,'user','ID', 'user-desc-id', user.id, div1, 'id','input');
    addLabeledElement(user,'user','Логин', 'user-desc-login', user.login, div1, 'login','input');
    addLabeledElement(user,'user','Имя', 'user-desc-fname', user.firstname, div1, 'fname','input');
    addLabeledElement(user,'user','Фамилия', 'user-desc-lname', user.lastname, div1, 'lname','input');
    addLabeledElement(user,'user','Отчество', 'user-desc-sname', user.surname !== '' ? user.surname : 'none', div1, 'sname','input');
    addLabeledElement(user,'user','Дата рождения', 'user-desc-date', user.dateofbirth, div1, 'date','input','date');
    addLabeledElement(user,'user','Email', 'user-desc-email', user.email, div1, 'email','input');
    addLabeledElement(user,'user','Вопрос', 'user-desc-qwestion', user.qwestion, div1, 'question');
    addLabeledElement(user,'user','Ответ', 'user-desc-answer', user.qwestionanswer, div1, 'answer');
    addLabeledElement(user,'user','Студ.билет', 'user-desc-card', user.studentcard!==null ? user.studentcard : 'none', div1, 'card','input','number');
    addLabeledElement(user,'user','Группа', 'user-desc-group', user.usergroupName!==null ? user.usergroupName : 'none', div1, 'group','input');

    const rolesLabel = document.createElement('label');
    rolesLabel.setAttribute('for', `user_roles_${user.id}`);
    rolesLabel.innerText = 'Роли: ';
    div1.appendChild(rolesLabel);

    const roles = document.createElement('span');
    roles.className = 'user-desc-roles';
    roles.id = 'user_roles_'+user.id;
    if(user.userRoles) {
        Array.from(user.userRoles).forEach(role => {
            roles.innerText += role + ',';
        });
    }
    roles.innerText=roles.innerText.substring(0,roles.innerText.length-1);
    addScrollEventListener(roles);
    div1.appendChild(roles);
    const btn=document.createElement('button');
    btn.className='allbuttons';
    btn.id='user_btn_update_'+user.id;
    btn.innerText='Сохранить';
    btn.type='button';
    btn.style['display']='none';
    btn.setAttribute('onclick','updateUser('+user.id+')');
    divBase.appendChild(btn);
    const actions = new Map([
        ['Удалить','deleteUser('+div.id+')'],
        ['Изменить','showUserUpdateButton(\''+btn.id+'\',\''+user.id+'\')'],
        ['Забанить', 'controlForm(\'banform\',' + div.id + ')']
    ]);
    const settings=createSettingsBlock(div,user,actions);
    settings.style['alignSelf']='self-start';
}

function createBanDataContainer(ban,getterinfo){
    const divBase=document.createElement('div');
    divBase.className='ban-container';
    divBase.id='ban_container_'+ban.id;
    getterinfo.appendChild(divBase);
    const actions = new Map([
        ['Удалить','deleteBan('+ban.id+')'],
        ['Изменить','controlForm(\'banform\',' + divBase.id + ');fillForm(\'banform\',\''+divBase.id+'\')'],
    ]);
    const settings=createSettingsBlock(divBase,ban,actions);
    settings.style['alignSelf']='end';
    const desc=document.createElement('div');
    desc.className='ban-desc';
    divBase.appendChild(desc);
    addLabeledElement(ban,'ban','ID бана','ban-desc-id',ban.id,desc,'id');
    addLabeledElement(ban,'ban','Причина','ban-desc-reason',ban.reason,desc,'reason','input');
    addLabeledElement(ban,'ban','Дата бана','ban-desc-start',ban.start,desc,'start','input','datetime-local');
    addLabeledElement(ban,'ban','До','ban-desc-end',ban.end,desc,'end','input','datetime-local');
    addLabeledElement(ban,'ban','IP адрес','ban-desc-ip',(ban.ipaddress!=="" && ban.ipaddress!==null ? ban.ipaddress : "none"),desc,'ip','input');
    addLabeledElement(ban,'ban','ID админа','ban-desc-id-admin',ban.bannedBy.id,desc,'id_admin');
    addLabeledElement(ban,'ban','ID пользователя','ban-desc-id-user',ban.userId.id,desc,'id_user');
}

function createFileDataContainer(file,parentType,getterinfo){
    const imageMimeTypes = {
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'png': 'image/png',
        'gif': 'image/gif',
        'webp': 'image/webp',
        'bmp': 'image/bmp',
        'svg': 'image/svg+xml',
        'ico': 'image/x-icon',
        'tif': 'image/tiff',
        'tiff': 'image/tiff',
        'avif': 'image/avif',
        'heic': 'image/heic',
        'heif': 'image/heif'
    };

    const divBase=document.createElement('div');
    divBase.className='file-container';
    divBase.id='file_container_'+file.id;
    getterinfo.appendChild(divBase);
    const div = document.createElement('div');
    div.id = 'file_'+file.id;
    divBase.appendChild(div);

    const img = document.createElement('img');
    img.src =imageMimeTypes[file.path.substring(file.path.lastIndexOf('.')+1)] ? file.href : '/useravatar/'+(-1);
    img.id = 'file_preview_' + file.id;
    img.setAttribute('onclick','window.location.href=\''+file.href+'\'');
    div.appendChild(img);
    const desc=document.createElement('div');
    desc.className='file-desc';
    divBase.appendChild(desc);
    addLabeledElement(file,'file','ID файла','file-desc-id',file.id,desc,'id');
    addLabeledElement(file,'file','Путь к файлу','file-desc-path',file.path,desc,'path');
    let header='ID ';
    switch (parentType){
        case 'task':{
            header+='задания';
        break;}
        case 'taskcompleted':{
            header+='выполненного задания';
        break;}
        case 'user':{
            header+='пользователя';
        break;}
        default:{
            header+='сущности';
        }
    }
    addLabeledElement(file,'file',header,'file-desc-id-entity',file.parentEntityId,desc,'id_entity');
    const actions = new Map([
        ['Открыть','window.open(\''+file.href+'\',\'_blank\')'],
        ['Удалить','deleteFile(\''+parentType+'\','+file.id+')']
    ]);
    const settings=createSettingsBlock(divBase,file,actions);
    settings.style['alignSelf']='start';
}

function createGroupDataContainer(group,getterinfo){
    const divBase=document.createElement('div');
    divBase.className='group-container';
    divBase.id='group_container_'+group.id;
    getterinfo.appendChild(divBase);
    const actions = new Map([
        ['Удалить','deleteGroup('+group.id+')'],
        ['Изменить','controlForm(\'groupform\',' + divBase.id + ');fillForm(\'groupform\',\''+divBase.id+'\')'],
    ]);
    const settings=createSettingsBlock(divBase,group,actions);
    settings.style['alignSelf']='end';
    const desc=document.createElement('div');
    desc.className='group-desc';
    divBase.appendChild(desc);
    addLabeledElement(group,'group','ID группы','group-desc-id',group.id,desc,'id');
    addLabeledElement(group,'group','Название','group-desc-name',group.name,desc,'name','input');
    addLabeledElement(group,'group','Курс','group-desc-course',group.course,desc,'course','input','number');
}

function createRoleDataContainer(role,getterinfo){
    const divBase=document.createElement('div');
    divBase.className='role-container';
    divBase.id='role_container_'+role.id;
    getterinfo.appendChild(divBase);
    const actions = new Map([
        ['Удалить','deleteRole('+role.id+')'],
        ['Изменить','controlForm(\'roleform\',' + divBase.id + ');fillForm(\'roleform\',\''+divBase.id+'\')'],
    ]);
    const settings=createSettingsBlock(divBase,role,actions);
    settings.style['alignSelf']='end';
    const desc=document.createElement('div');
    desc.className='role-desc';
    divBase.appendChild(desc);
    addLabeledElement(role,'role','ID роли','role-desc-id',role.id,desc,'id');
    addLabeledElement(role,'role','Название','role-desc-name',role.name,desc,'name','input');
    addLabeledElement(role,'role','Сила','role-desc-power',role.power,desc,'power','input','number');
}

function createSubjectDataContainer(subject,getterinfo){
    const divBase=document.createElement('div');
    divBase.className='subject-container';
    divBase.id='subject_container_'+subject.id;
    getterinfo.appendChild(divBase);
    const actions = new Map([
        ['Удалить','deleteSubject('+subject.id+')'],
        ['Изменить','controlForm(\'subjectform\',' + divBase.id + ');fillForm(\'subjectform\',\''+divBase.id+'\')'],
    ]);
    const settings=createSettingsBlock(divBase,subject,actions);
    settings.style['alignSelf']='end';
    const desc=document.createElement('div');
    desc.className='subject-desc';
    divBase.appendChild(desc);
    addLabeledElement(subject,'subject','ID предмета','subject-desc-id',subject.id,desc,'id');
    addLabeledElement(subject,'subject','Название','subject-desc-name',subject.name,desc,'name','input');
}

function createSettingsBlock(container,data,actions) {
     const div1 = document.createElement('div');
    div1.style['text-align'] = 'end';
    div1.className='user-participant-settings';
    container.appendChild(div1);
    const btn = document.createElement('span');
    btn.id = 'button_settings_' + data.id;
    btn.className = 'settings-btn';
    btn.style['alignSelf']='self-start';
    btn.setAttribute('onclick', 'showSettingsMenu(' + data.id + ')');
    btn.innerText = '⚙️';
    div1.appendChild(btn);
    const div2 = document.createElement('div');
    div2.id = 'settings_' + data.id;
    div2.className = 'settings-block';
    div2.style['right']='25px';
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

function fillForm(name,containerId){
    const form = document.querySelector('.admin-form');
    console.log(form,name);
    if (!form) {
        return;
    }
    const container=document.getElementById(containerId);
    console.log(container);
    if(container) {
        switch (name) {
            case 'banform': {
                form.querySelector('[id="banform_reason"]').value = container.querySelector('[id^="ban_reason"]').value;
                form.querySelector('[id="banform_end"]').value = container.querySelector('[id^="ban_end"]').value;
                form.querySelector('[id="banform_address"]').value = container.querySelector('[id^="ban_ip"]').value !== 'none' ? container.querySelector('[id^="ban_ip"]').value : "";
                form.querySelector('[id="banform_user"]').value = container.querySelector('[id^="ban_id_user"]').innerText;
                form.querySelector('[id="banform_user"]').setAttribute('readonly', 'true');
                form.querySelector('[id="banform_sendbutton"]').setAttribute('onclick', 'sendBan(\'update\',' + container.querySelector('[id^="ban_id"]').innerText + ')');
            break;}
            case 'groupform': {
                form.querySelector('[id="groupform_name"]').value=container.querySelector('[id^="group_name"]').value;
                form.querySelector('[id="groupform_course"]').value=container.querySelector('[id^="group_course"]').value;
                form.querySelector('[id="groupform_sendbutton"]').setAttribute('onclick', 'sendGroup(\'update\',' + container.querySelector('[id^="group_id"]').innerText + ')');
            break;}
            case 'roleform':{
                form.querySelector('[id="roleform_name"]').value=container.querySelector('[id^="role_name"]').value;
                form.querySelector('[id="roleform_power"]').value=container.querySelector('[id^="role_power"]').value;
                form.querySelector('[id="roleform_sendbutton"]').setAttribute('onclick', 'sendRole(\'update\',' + container.querySelector('[id^="role_id"]').innerText + ')');
            break;}
            case 'subjectform':{
                form.querySelector('[id="subjectform_name"]').value=container.querySelector('[id^="subject_name"]').value;
                form.querySelector('[id="subjectform_sendbutton"]').setAttribute('onclick', 'sendSubject(\'update\',' + container.querySelector('[id^="subject_id"]').innerText + ')');
            break;}
        }
    }
}

function controlForm(name,attributes) {
    const form = document.querySelector('.admin-form');
    if (form) {
        form.remove();
    }
    switch (name) {
        case 'banform': {
            const inputUser = createInput('banform_user', 'form-input', new Map([['required', 'true']]), 'number', 'ID пользователя');
            if(attributes) {
                inputUser[0].value = attributes;
            }
            createForm(
                new Map([
                    createInput('banform_reason', 'form-input',
                        new Map([
                            ['required', 'true']
                        ]),
                        'text', 'Причина'),
                    createInput('banform_end', 'form-input',
                        new Map([
                            ['required', 'true']
                        ]),
                        'datetime-local', 'Дата конца'),
                    inputUser,
                    createInput('banform_address', 'form-input',
                        new Map([['placeholder','(Опционально)']]),
                        'text', 'IP адрес'),
                    createInput('banform_sendbutton', 'allbuttons',
                        new Map([['style','margin-top: 5px;']]),
                        'button', null, 'sendBan(\'add\')','Отправить')
                ]),'БАН','banform'
            );
        break;}
        case 'groupform':{
            createForm(
            new Map([
                createInput('groupform_name', 'form-input',
                    new Map([
                        ['required', 'true']
                    ]),
                    'text', 'Название'),
                createInput('groupform_course', 'form-input',
                    new Map([
                        ['required', 'true']
                    ]),
                    'number', 'Курс'),
                createInput('groupform_sendbutton', 'allbuttons',
                    new Map([['style','margin-top: 5px;']]),
                    'button', null, 'sendGroup(\'add\')','Отправить')
            ]),'ГРУППА','groupform'
            );
        break;}
        case 'roleform':{
            createForm(
                new Map([
                    createInput('roleform_name', 'form-input',
                        new Map([
                            ['required', 'true']
                        ]),
                        'text', 'Название'),
                    createInput('roleform_power', 'form-input',
                        new Map([
                            ['required', 'true']
                        ]),
                        'number', 'Сила'),
                    createInput('roleform_sendbutton', 'allbuttons',
                        new Map([['style','margin-top: 5px;']]),
                        'button', null, 'sendRole(\'add\')','Отправить')
                ]),'РОЛЬ','roleform'
            );
        break;}
        case 'subjectform':{
            createForm(
                new Map([
                    createInput('subjectform_name', 'form-input',
                        new Map([
                            ['required', 'true']
                        ]),
                        'text', 'Название'),
                    createInput('subjectform_sendbutton', 'allbuttons',
                        new Map([['style','margin-top: 5px;']]),
                        'button', null, 'sendSubject(\'add\')','Отправить')
                ]),'ПРЕДМЕТ','subjectform'
            );
        break;}
    }
}

function createInput(id,className,attributesMap,type,labelText,onclickAction=null,buttonText='Кнопка'){
    let element;
    let label=undefined;
    if(type!=='button'){
        element=document.createElement('input');
        label=document.createElement('label');
        label.setAttribute('for',id);
        label.innerText=labelText!==null ? labelText : '';
    }else{
        element=document.createElement('button');
        if(onclickAction!==null){
            element.setAttribute('onclick',onclickAction);
        }
        element.innerText=buttonText;
    }
    element.type=type;
    element.className=className;
    if(id!==null) {
        element.id = id;
    }
    attributesMap.forEach((value,name)=>{
        element.setAttribute(name,value);
    });
    return [element,label];
}

function createForm(inputMap,header,id){
    //console.log(inputMap);
    const formContainer=document.createElement('div');
    formContainer.className='admin-form';
    formContainer.id=id;
    document.body.appendChild(formContainer);
    const span=document.createElement('span');
    span.className='admin-form-header';
    span.innerText=header;
    formContainer.appendChild(span);
    const closeButton=document.createElement('span');
    closeButton.className='admin-form-close-button';
    closeButton.innerText='X';
    closeButton.setAttribute('onclick','closeForm()');
    formContainer.appendChild(closeButton);
    inputMap.forEach((label,input)=>{
        const div=document.createElement('div');
        div.className='admin-form-input-container';
        formContainer.appendChild(div);
        if(label!==undefined) {
            div.appendChild(label);
        }
        div.appendChild(input);
    });
}

function closeForm(){
    const form = document.querySelector('.admin-form');
    if (form) {
        form.remove();
    }
}

function addScrollEventListener(element){
   // console.log(element);
    if(element.value) {
        element.setAttribute('title', element.value);
    }
    element.addEventListener('wheel', function (event) {
        if (event.deltaY !== 0) {
            event.preventDefault();
            element.scrollLeft += event.deltaY;
        }
    }, { passive: false });
}