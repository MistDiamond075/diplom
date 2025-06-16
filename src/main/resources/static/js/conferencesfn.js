let conference_form_processing = false;
let connecting_to_videocall=false;

function redirectToConf(confId){
    if(connecting_to_videocall){
        return;
    }
    connecting_to_videocall=true;
    fetch('/videocall/'+confId+'/add',{
        method: 'post',
        headers:{[csrfHeader]: csrfToken}
    }).then(response =>{
        connecting_to_videocall=false;
        if(!response.ok){
            console.log('error occured');
            return;
        }
        return response.json();
    }).then(data => {
        console.log(data);
        sessionStorage.setItem("joined_from_conferences_page","true");
        sessionStorage.setItem("conference_id",confId);
        document.location.href='videocall/'+data.id;
    });
}

function redirectToConfUpdatePage(conferenceId){
    event.stopPropagation();
    console.log('redirecting');
    window.location.href='conference/'+conferenceId+'/update';
}

function showConfSettings(id){
    event.stopPropagation();
    const confsettings=document.getElementById('conference_settings_'+id);
    if(confsettings.style['display']==='none'){
        const confs=Array.from(document.getElementsByClassName('conference-settings-block'));
        confs.forEach(confs=>confs.style['display']='none');
        confsettings.style['display']='flex';
    }else{
        confsettings.style['display']='none';
    }
}

function showFilters(id){
    const element=document.getElementById(id);
    element.style['display']=element.style['display']==='none' ? '':'none';
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

function sendConferenceToSv(id){
    if(!conference_form_processing) {
        conference_form_processing = true;
        const form_data = new FormData();
        const name=document.getElementById('conference_name').value;
        const datestart=document.getElementById('conference_date').value;
        let repeatable=document.getElementById('conference_period').value;
        const group=Array.from(document.getElementById('group_list').innerText.split(' '));
        const subjectname=document.getElementById('subject_selector').value;
        const senddata = {
            id: id,
            name: name,
            datestart: datestart.replaceAll('T',' '),
            dateend: "",
            repeatable:repeatable,
            groups: null,
            createdby:null,
            subjectId: null,
        }
        console.log(senddata);
        form_data.append('groups',group);
        form_data.append('subjectname',subjectname);
        form_data.append('conferencedata',new Blob([JSON.stringify(senddata)],{type: 'application/json'}));
        const url=window.location.href.includes('/create') ? window.location.href+'/addConference':window.location.href+'/updateConference';
        const method=window.location.href.includes('/create') ? 'post':'PATCH';
        fetch(url,{
            method:method,
            headers: {
                'Accept': 'application/json',
                [csrfHeader]: csrfToken
            },
            body:form_data
        }).then(response =>{
            conference_form_processing=false;
            if(!response.ok){
                showInfoMessage('error occured');
                return;
            }
            return response.json();
        }).then(data => {
            console.log(data);
        });
    }
}

function deleteConference(id,taskname){
    event.stopPropagation();
    console.log('deleting');
    const accepted=confirm('Удалить видеоконференцию \"'+taskname+'\"?');
    if(!accepted){
        console.log("User has canceled their request");
        return;
    }
    fetch(window.location.href+'/'+id+'/deleteConference',{
        method: 'DELETE',
        headers:{[csrfHeader]: csrfToken}
    }).then(response =>{
        if(!response.ok){
            console.log('error occured');
            return;
        }
        document.getElementById('conference_'+id).remove();
        return response.json();
    }).then(data => {
        console.log(data);
    });
}

document.addEventListener('DOMContentLoaded',function (){
    if(typeof setSort!=='undefined') {
        const confsort = setSort('filter_datepicker_start', 'filter_datepicker_end', 'filter_name', 'filter_group', 'filter_subject',
            '.conference', '.conference-date', '.conference-name', '.conference-group', '.conference-subject');
        const activeconfsort = setSort('filter_active_datepicker_start', 'filter_active_datepicker_end', 'filter_active_name', 'filter_active_group', 'filter_active_subject',
            '.conference-active', '.conference-active-date', '.conference-active-name', '.conference-active-group', '.conference-active-subject');
    }
    if(!window.location.href.includes('conferences')){
        addGroupToGroupList();
    }else if(window.location.href.includes('conferences')){
        const elements=Array.from(document.getElementsByClassName('conference-settings-block'));
        elements.forEach(element=>addSettingsMenuListener(element.className));
    }
    },false);