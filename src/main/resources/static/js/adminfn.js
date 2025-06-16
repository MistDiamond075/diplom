function showUserUpdateButton(buttonId,userId){
    const btn=document.getElementById(buttonId);
    const element=document.getElementById('user_desc_'+userId);
    const elements=document.querySelectorAll('[class*="user-container"]');
    //console.log(elements);
    elements.forEach(otherElement=>{
        const btn=otherElement.querySelector('[id^="user_btn_update_"]');
        //console.log(btn);
        if(btn && btn.id!=='user_btn_update_'+userId){
            btn.style['display']='none';
        }
        const inputs=otherElement.querySelectorAll('input');
        inputs.forEach(input=>{
            if(!input.hasAttribute('readonly')){
                input.setAttribute('readonly','true');
            }
        });
    });
    restoreData();
    if(btn){
        btn.style['display']=btn.style['display']==='none' ? '' : 'none';
    }
    //console.log(element);
    if(element){
        const inputs=element.querySelectorAll('input');
       // console.log(inputs);
        inputs.forEach(input=>{
            if(input.hasAttribute('readonly')){
                input.removeAttribute('readonly');
                userOldData=collectOriginalData(userId);
            }else{
                input.setAttribute('readonly','true');
            }
        });
    }
}

function getData(element_id,pairDataSingle,pairDataAll,additionalType=null) {
    let id;
    if (isNaN(element_id)) {
        id = document.getElementById(element_id).value;
    } else {
        id = element_id;
    }
    let str_req = window.location.pathname;
    if (id !== undefined) {
        str_req += (id==='' ? pairDataAll.first :( pairDataSingle.first + id));
    }
    if (id==='') {
        sendGetRequestToSv(str_req, pairDataAll.second,additionalType);
    } else {
        sendGetRequestToSv(str_req, pairDataSingle.second,additionalType);
    }
}

function updateUser(userId){
    const userdata=collectOriginalData(userId);
    const senddata={
        id:null,
        login:userdata.login,
        firstname:userdata.fname,
        lastname:userdata.lname,
        surname:userdata.sname,
        qwestion:userdata.question,
        qwestionanswer:userdata.answer,
        dateofbirth:userdata.date.toString(),
        email:userdata.email,
        studentcard:userdata.card.toString(),
        usergroupId:null
    }
    const promise=fetch('/updUser/'+userId,{
        method:'PATCH',
        headers:{
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken,
        },
        body:JSON.stringify(senddata)
    });
    displayData(promise,'user');
}

function deleteUser(userId){
    const promise= fetch('/admin/menu/delUser/'+userId,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'user');
}

function sendBan(action,id=null){
    const form=document.getElementById('banform');
    if(form) {
        const reason=document.getElementById('banform_reason').value;
        const datenow = new Date();
        const start=DateTimeToFormat(datenow.getFullYear())+'-'+DateTimeToFormat(datenow.getMonth())+'-'+DateTimeToFormat(datenow.getDate())+' '+DateTimeToFormat(datenow.getHours())+':'+DateTimeToFormat(datenow.getMinutes());
        const end=document.getElementById('banform_end').value;
        const userId=document.getElementById('banform_user').value;
        const ip=document.getElementById('banform_address').value;
        const senddata={
            id:null,
            reason:reason,
            start:start,
            end:end.replace("T"," "),
            ipaddress:ip==='' ? null : ip
        };
        const url=window.location.href+(action==='add' ? '/addBan/'+userId : '/updateBan/'+id);
        const promise=fetch(url,{
            method:action==='add' ? 'post' : 'PATCH',
            headers:{
                [csrfHeader]: csrfToken,
                'Content-Type':'application/json'
            },
            body:JSON.stringify(senddata)
        });
        displayData(promise,'ban');
        closeForm();
    }
}

function deleteBan(banId){
    const promise= fetch('/admin/menu/deleteBan/'+banId,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'ban');
}

function deleteFile(parentType,id1,id2=null){
    let url=null;
    switch (parentType){
        case 'task':{
            url='/task/'+id2+'/file/'+id1+'/delete';
            break;}
        case 'taskcompleted':{
            url='/task/'+id2+'/completedTask/file/'+id1+'/delete';
            break;}
        case 'user':{
            url='/deleteUserAvatar/'+id1;
            break;}
    }
    if(url===null){
        return;
    }
    const promise=fetch(url,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'file');
}

function sendGroup(action,id=null) {
    const form = document.getElementById('groupform');
    if (form) {
        const name = document.getElementById('groupform_name').value;
        const course = document.getElementById('groupform_course').value !== '' ? document.getElementById('groupform_course').value : 0;
        const senddata = {
            id: null,
            name: name,
            course: parseInt(course)
        };
        const url = window.location.href + (action === 'add' ? '/addGroup' : '/updateGroup/' + id);
        const promise = fetch(url, {
            method: action === 'add' ? 'post' : 'PATCH',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(senddata)
        });
        displayData(promise, 'group');
        closeForm();
    }
}

function deleteGroup(groupId){
    const promise= fetch('/admin/menu/deleteGroup/'+groupId,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'group');
}

function sendRole(action,id=null) {
    const form = document.getElementById('roleform');
    if (form) {
        const name = document.getElementById('roleform_name').value;
        const power = document.getElementById('roleform_power').value !== '' ? document.getElementById('roleform_power').value : 0;
        const senddata = {
            id: null,
            name: name,
            power: parseInt(power)
        };
        const url = window.location.href + (action === 'add' ? '/addRole' : '/updateRole/' + id);
        const promise = fetch(url, {
            method: action === 'add' ? 'post' : 'PATCH',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(senddata)
        });
        displayData(promise, 'role');
        closeForm();
    }
}

function deleteRole(roleId){
    const promise= fetch('/admin/menu/deleteRole/'+roleId,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'role');
}

function sendSubject(action,id=null) {
    const form = document.getElementById('subjectform');
    if (form) {
        const name = document.getElementById('subjectform_name').value;
        const senddata = {
            id: null,
            name: name
        };
        const url = window.location.href + (action === 'add' ? '/addSubject' : '/updateSubject/' + id);
        const promise = fetch(url, {
            method: action === 'add' ? 'post' : 'PATCH',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(senddata)
        });
        displayData(promise, 'subject');
        closeForm();
    }
}

function deleteSubject(roleId){
    const promise= fetch('/admin/menu/deleteRole/'+roleId,{
        method:'delete',
        headers:{
            [csrfHeader]: csrfToken,
        }
    });
    displayData(promise,'role');
}

function sendGetRequestToSv(req_url, entity_type,additional_type) {
    const promise=fetch(req_url, {
        method: 'get'
    });
    displayData(promise,entity_type,additional_type);
}

function displayData(promise,entityType,additionalType=null){
    promise.then(response => {
        if (!response.ok) {
            let resp_data = response.text().then(text => text ? JSON.parse(text) : null);
            resp_data.then(data => {
                document.getElementById('getter_info').textContent = data.message;
            });
            return;
        }
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("application/json")) {
            return response.json();
        } else {
            return response.text();
        }
    }).then(data => {
        if (data != null) {
            //  console.log(data);
            formatData(data, entityType,additionalType);
            return data;
        }
    });
}

function formatData(data, entity_type,additional_type=null) {
    const getterinfo = document.getElementById('getter_info');
    console.log(entity_type);
    while (getterinfo.children.length > 0) {
        getterinfo.removeChild(document.getElementById('getter_info').children[0]);
    }
    switch (entity_type) {
        case 'user':{
            createUserDataContainer(data,getterinfo);
        break;}
        case 'users':{
            data.forEach(user => {
                createUserDataContainer(user,getterinfo);
            });
        break;}
        case 'ban':{
            createBanDataContainer(data,getterinfo);
        break;}
        case 'bans':{
            data.forEach(ban=>{
                createBanDataContainer(ban,getterinfo);
            });
        break;}
        case 'file':{
            createFileDataContainer(data,additional_type,getterinfo);
        break;}
        case 'files':{
            data.forEach(file=>{
                createFileDataContainer(file,additional_type,getterinfo);
            });
        break;}
        case 'group':{
            createGroupDataContainer(data,getterinfo);
        break;}
        case 'groups':{
            data.forEach(group=>{
                createGroupDataContainer(group,getterinfo);
            });
        break;}
        case 'role':{
            createRoleDataContainer(data,getterinfo);
        break;}
        case 'roles':{
            data.forEach(role=>{
                createRoleDataContainer(role,getterinfo);
            });
        break;}
        case 'subject':{
            createSubjectDataContainer(data,getterinfo);
            break;}
        case 'subjects':{
            data.forEach(subject=>{
                createSubjectDataContainer(subject,getterinfo);
            });
            break;}    
        default: {
            document.getElementById('getter_info').innerHTML = '<pre style="white-space: break-spaces">' + JSON.stringify(data, null, 2) + '</pre>';
        }
    }
}

document.addEventListener('DOMContentLoaded',function (){
    addSettingsMenuListener('settings-block');
},false);