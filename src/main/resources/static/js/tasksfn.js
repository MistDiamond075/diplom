const files_input_max_size=10;
let file_list_zone;
let task_form_processing=false;
let group_selector_element;
let file_list_constant=[];
let task_dates_original_value=undefined;

function redirectToTask(taskId){
    document.location.href='task/'+taskId;
}

function redirectToTaskUpdatePage(taskId){
    event.stopPropagation();
    console.log('redirecting');
    window.location.href='task/'+taskId+'/update';
}

function showTaskSettings(id){
    event.stopPropagation();
    const tasksettings=document.getElementById('task_settings_'+id);
    if(tasksettings.style['display']==='none'){
        const tasks=Array.from(document.getElementsByClassName('task-settings-block'));
        tasks.forEach(task=>task.style['display']='none');
        tasksettings.style['display']='flex';
    }else{
        tasksettings.style['display']='none';
    }
}

function displayAttachedFiles(event){
    file_list_zone.innerHTML = '';
    file_list_constant.forEach(file =>{
        console.log(file);
        file_list_zone.appendChild(file);
    });
    const files = event.target.files;
    console.log('--'+files);
    if (files.length === 0) {
        file_list_zone.innerHTML += '<p style="font-size: 0.6em">Ничего нет ¯\\_(ツ)_/¯</p>';
        return;
    }
    for (const file of files) {
        let element=document.createElement('a');
        element.textContent = file.name;
        element.href = URL.createObjectURL(file);
        element.target="_blank";
        element.className='task-file';
        element.rel="noopener noreferrer";
        file_list_zone.appendChild(element);
    }
}

function limitInputFormFileCount(event){
    const files = event.target.files;
    if (files.length > files_input_max_size) {
        alert('A maximum of 10 files are allowed');
    }
}

function showFilters(){
    const element=document.getElementById('filter_zone');
    element.style['display']=element.style['display']==='none' ? '':'none';
}

function sendTaskToSv(id){
    if(!task_form_processing) {
        task_form_processing = true;
        const form_data = new FormData();
        const name=document.getElementById('task_name').value;
        const dateend=document.getElementById('task_date').value;
        const group=Array.from(document.getElementById('group_list').innerText.split(' '));
        const subjectname=document.getElementById('subject_selector').value;
        const userId = document.getElementById('user_id').value;
        const text = document.getElementById('task_desc').value;
        const files = Array.from(document.getElementById('attached_file').files);
        const senddata = {
            id: id,
            name: name,
            datestart: getDateTime(),
            dateend: dateend.replaceAll('T',' '),
            text: text,
            groups: null,
            createdby:null,
            tasksubjectId: null,
        }
        console.log(senddata);
        form_data.append('user_id', parseInt(userId));
        if (files.length > 0) {
            files.forEach(file => form_data.append('files',file));
        }
        form_data.append('groups',group);
       form_data.append('subjectname',subjectname);
        form_data.append('taskdata',new Blob([JSON.stringify(senddata)],{type: 'application/json'}));
        //console.log(form_data);
        const url=window.location.href.includes('/create') ? '/tasks/addTask':'/tasks/update/'+document.getElementById('task_id').value;
        const method=window.location.href.includes('/create') ? 'post':'PATCH';
        fetch(url,{
            method:method,
            headers: {
                'Accept': 'application/json',
                [csrfHeader]:csrfToken
            },
            body:form_data
        }).then(response =>{
            task_form_processing=false;
            if(!response.ok){
                console.log('error occured');
                return;
            }
            return response.json();
        }).then(data => {
            console.log(data);
        });
    }
}

function deleteTaskFromSv(id,taskname){
    event.stopPropagation();
    const accepted=confirm('Удалить задание \"'+taskname+'\"?');
    if(!accepted){
        console.log("User has canceled their request");
        return;
    }
    fetch(window.location.href+'/'+id+'/deleteTask',{
        method: 'DELETE',
        headers:{
            [csrfHeader]: csrfToken,
        }
    }).then(response =>{
        if(!response.ok){
            console.log('error occured');
            return;
        }
        document.getElementById('task_'+id).remove();
        return response.json();
    }).then(data => {
        console.log(data);
    });
}

function sendCompletedTaskToSv(id,update=false){
    if(!task_form_processing) {
        task_form_processing = true;
        const form_data = new FormData();
        const userId = document.getElementById('user_id').value;
        const text = document.getElementById('textzone').value;
        const files = Array.from(document.getElementById('attached_file').files);
        const senddata = {
            id: id,
            tasks_id: null,
            dateofsubmit: getDateTime(),
            grade: null,
            commentary: text,
            feedback: "",
            dateofcheck:"",
            user_id: null,
        }
        console.log(senddata);
        form_data.append('user_id', parseInt(userId));
        if (files.length > 0) {
             files.forEach(file => form_data.append('files',file));
        }
        form_data.append('taskdata',new Blob([JSON.stringify(senddata)],{type: 'application/json'}));
        fetch(window.location.href+'/'+(update ? 'update' : 'add')+'TaskCompleted',{
            method:update ? 'PATCH' : 'post',
            headers: {
                'Accept': 'application/json',
                [csrfHeader]:csrfToken
            },
            body:form_data
        }).then(response =>{
            task_form_processing=false;
            if(!response.ok){
                console.log('error occured');
                return;
            }
            return response.json();
        }).then(data => {
            console.log(data);
        });
    }
}

function checkCompletedTask(){
    const id=document.getElementById('compltask_id').value;
    const grade=document.getElementById('task_grade').value!=='' ? document.getElementById('task_grade').value : null;
    const feedback=document.getElementById('task_feedback').value;
    const senddata = {
        id: id,
        tasks_id: null,
        dateofsubmit: getDateTime(),
        grade: grade,
        commentary: "",
        feedback: feedback,
        dateofcheck:getDateTime(),
        user_id: null,
    }
    const url=window.location.href.includes('?') ? window.location.href.substring(0,window.location.href.indexOf('?')) : window.location.href;
    fetch(url+'/checkTaskCompleted',{
        method:'PATCH',
        headers: {
            'Accept': 'application/json',
            'Content-Type' : 'application/json',
            [csrfHeader]:csrfToken
        },
        body:JSON.stringify(senddata)
    }).then(response =>{
        task_form_processing=false;
        if(!response.ok){
            console.log('error occured');
            return;
        }
        return response.json();
    }).then(data => {
        console.log(data);
    });
}

function getTaskFilesFromSv(element_id){
    if(window.location.href.includes('/tasks')){
        console.log('tasks page');
        return;
    }
    const url=window.location.href.includes('update') ? window.location.href.substring(0,window.location.href.indexOf('/update'))+'/file/' : window.location.href+'/file/';
    const urlrequest=window.location.href.includes('update') ? window.location.href.substring(0,window.location.href.indexOf('/update'))+'/files' : window.location.href+'/files';
    console.log('files for task');
    fetch(urlrequest,{
        method: 'get'
    }).then(response =>{
        if(!response.ok){
            console.log('failed to load data');
            throw new Error("request failed");
        }
        return response.json();
    }).then(data =>{
        if(data.length<1){
            console.log('no files');
            document.getElementById(element_id).innerText="Ничего нет ¯\\_(ツ)_/¯";
            document.getElementById(element_id).style['margin-top']='';
            return;
        }
        data.forEach(file =>{
            const div=document.createElement('div');
            div.className='task-file';
            div.id='file_'+file.id;
            document.getElementById(element_id).appendChild(div);
            const element=document.createElement('a');
            element.innerText=file.name+'.'+file.type+'  ';
            element.href=url+file.id+'/view?'+new URLSearchParams({path:file.url});
            element.target="_blank";
            element.rel="noopener noreferrer";
            div.appendChild(element);
            const current_user=document.getElementById('user_id').value;
            if(file.user_id===current_user) {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.innerText = 'X';
                btn.className = 'task-file-remove-btn';
                btn.setAttribute('onclick', 'deleteTaskFileFromSv(' + file.id + ')');
                div.appendChild(btn);
            }
        });
        console.log(data);
    });
}

function getCompletedTaskDataFromSv(event){
    event.preventDefault();
    let user_id=document.getElementById('student_selector').value;
    const datalist = document.getElementById("filter_student_list").options;
    for (let option of datalist) {
        if (option.value === user_id) {
            user_id = parseInt(option.getAttribute("data-id"));
            break;
        }
    }
    if(isNaN(user_id) || user_id===''){
        return;
    }
    console.log('user_id: '+user_id);
    task_dates_original_value=task_dates_original_value===undefined ? document.getElementById('task_dates').innerHTML : task_dates_original_value;
    let str_url=window.location.href.toString().includes('?') ? window.location.href.toString().substring(0,window.location.href.toString().indexOf('?')):window.location.href;
    fetch(str_url+'/getTaskCompletedByUserId?'+new URLSearchParams({userId:parseInt(user_id)}),{
        method: 'get'
    }).then(response =>{
        if(!response.ok){
            console.log('failed to load data');
            throw new Error("request failed");
        }
        return response.json();
    }).then(data =>{
        document.getElementById('textzone').value=data.commentary;
        document.getElementById('compltask_id').value=data.id;
        document.getElementById('task_feedback').value=data.feedback;
        document.getElementById('task_grade').value=data.grade !==null ? data.grade : '';
        document.getElementById('task_dates').innerHTML=task_dates_original_value;
        document.getElementById('task_dates').innerHTML+='<br>Сдано: ' +data.dateofsubmit;
        document.getElementById('task_dates').innerHTML+=data.dateofcheck!=='' ? '<br>Проверено: ' +data.dateofcheck : '';
        getCompletedTasksFilesFromSv(data.id);
        console.log(data.id);
    });
}

function getCompletedTasksFilesFromSv(fileid){
        console.log(fileid);
    document.getElementById('file_list').innerHTML='';
    let str_url=window.location.href.toString().includes('?') ? window.location.href.toString().substring(0,window.location.href.toString().indexOf('?')):window.location.href;
        const url= str_url+ '/completedTask/file/';
        fetch(url + fileid.toString(), {
            method: 'get'
        }).then(response => {
            if (!response.ok) {
                console.log('failed to load data');
                throw new Error("request failed");
            }
            return response.json();
        }).then(data => {
            console.log('data:');
            console.log(data);
            data.forEach(file =>{
                const div=document.createElement('div');
                div.className='task-file';
                div.id='file_'+file.id;
                document.getElementById('file_list').appendChild(div);
                const element=document.createElement('a');
                element.innerText=file.name+'.'+file.type+'  ';
                element.href=url+'/'+file.id+'/view';
                element.target="_blank";
                element.rel="noopener noreferrer";
            div.appendChild(element);
            const current_user=document.getElementById('user_id').value;
            if(file.user_id===current_user) {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.innerText = 'X';
                btn.className = 'task-file-remove-btn';
                btn.setAttribute('onclick', 'deleteCompletedTaskFileFromSv(' + file.id + ')');
                div.appendChild(btn);
            }
            file_list_constant.push(element);
        });
        });
}

function deleteCompletedTaskFileFromSv(id,element){
    let str_url=window.location.href.toString().includes('?') ? window.location.href.toString().substring(0,window.location.href.toString().indexOf('?')):window.location.href;
    const url= str_url+ '/completedTask/file/' + id+'/delete';
    fetch(url,{
        method: 'DELETE',
        headers:{
            [csrfHeader]: csrfToken,
        }
    }).then(response =>{
        if(!response.ok){
            console.log('failed to load data');
            throw new Error("request failed");
        }
        return response.json();
    }).then(data =>{
        console.log(data);
        document.getElementById('file_'+id).remove();
    });
}

function addFilterByGroup(){
    const groupInput = document.getElementById('group_selector');
    const studentInput = document.getElementById('student_selector');
    const studentDatalist = document.getElementById('filter_student_list');
    const allStudentOptions = Array.from(studentDatalist.options).map(option => ({
        value: option.value,
        text: option.textContent || '',
        id: option.getAttribute('data-id')
    }));

    groupInput.addEventListener('input', function () {
        const selectedGroup = groupInput.value;
        studentDatalist.innerHTML = '';
        const filtered = allStudentOptions.filter(option =>
            selectedGroup === '' || option.text === selectedGroup
        );
        filtered.forEach(option => {
            const opt = document.createElement('option');
            opt.value = option.value;
            opt.textContent = option.text;
            opt.setAttribute('data-id', option.id);
            studentDatalist.appendChild(opt);
        });
        studentInput.value = '';
    });
}

function addGroupToGroupList(){
    document.getElementById('group_selector').addEventListener('input', function (e) {
        const input = e.target;
        const value = input.value;
        console.log(value);
        console.log(document.getElementById('group_list').innerText)
        if(document.getElementById('group_list').innerText.includes(value)){
            return;
        }
        const datalist = document.getElementById('group_selector_list');
        const options = Array.from(datalist.options);
        const match = options.find(opt => opt.value.toLowerCase() === value);
        if (match) {
           const element=document.createElement('span');
           const id_element='group_'+document.getElementById('group_list').children.length;
           element.innerText=value+'\t';
           element.className='group-from-list';
           element.id=id_element;
           element.setAttribute('onclick','deleteFromGroupList(\''+id_element+'\')');
           document.getElementById('group_list').appendChild(element);
            input.value = '';
        }
    });
}

function deleteFromGroupList(id_element){
    document.getElementById(id_element).remove();
    document.getElementById('group_selector').value='';
}

document.addEventListener('DOMContentLoaded', function (){
    file_list_zone=document.getElementById('file_list');
    group_selector_element=document.getElementById('group_selector');
    if(window.location.href.includes('create') || window.location.href.includes('update')){
        addGroupToGroupList();
    }
    if(window.location.href.includes('/tasks')){
        const elements=Array.from(document.getElementsByClassName('task-settings-block'));
        elements.forEach(element=>addSettingsMenuListener(element.className));
        const tasksort=setSort('filter_datepicker_start','filter_datepicker_end','filter_name','filter_group','filter_subject',
            '.task','.task-date','.task-name','.task-groups','.task-subject');
    }
    if (document.getElementById('attached_file')) {
        document.getElementById('attached_file').addEventListener('change',displayAttachedFiles);
        document.getElementById('attached_file').addEventListener('change',limitInputFormFileCount);
    }
    if (document.getElementById('student_selector')) {
        document.getElementById('student_selector').addEventListener('input',getCompletedTaskDataFromSv);
        addFilterByGroup();
    }
    if(document.getElementById('file_list') && document.getElementById('file_list').innerText!==''){
        getCompletedTasksFilesFromSv(document.getElementById('file_list').innerText);
    }
    if(window.location.href.toString().includes('?studentId')){
        const opts=Array.from(document.getElementById('student_selector').options);
        opts.forEach(opt =>{
          if(opt.value.includes(window.location.href.substring(window.location.href.indexOf('=')+1)+'@'))  {
              document.getElementById('student_selector').value=opt.value;
              return;
          }
        });
    }
    if(document.getElementById('taskfile_list')) {
        getTaskFilesFromSv('taskfile_list');
    }else{
        getTaskFilesFromSv('file_list');
    }
    console.log('LOADED');
},false);