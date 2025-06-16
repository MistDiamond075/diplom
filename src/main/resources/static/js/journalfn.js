let datepickerstart;
let datepickerend;
let subjectpicker;
let studentpicker;
let grouppicker;
let journaltable;
let journalformswitch;
let isDelete=undefined;
let isDelete_id=undefined;

function formatDateColumnsInJournalTable(){
    const tbody = journaltable.querySelector('tbody');
    let rows = Array.from(tbody.querySelectorAll('tr'));
    rows.shift();
    rows.forEach(row => {
        const dateCell = row.querySelector('td[data-column="date"]');
        if (dateCell) {
            const dateText = dateCell.innerText.trim();
            let [year, month, day] = dateText.split('-').map(Number);
            if(parseInt(day,10)<10){
                day='0'+day;
            }
            if(parseInt(month,10)<10){
                month='0'+month;
            }
            if(isNaN(year)){
                year=dateText.substring(0,dateText.indexOf('-'));
            }
            const rowDate = day + '-' + month + '-' + year;

                dateCell.innerText = rowDate;
        }
    });
}

function formatDateAsDefault(datetoformat){
    let [day, month, year] = datetoformat.split('-').map(Number);
    if(parseInt(day,10)<10){
        day='0'+day;
    }
    if(parseInt(month,10)<10){
        month='0'+month;
    }
    return  year + '-' + month + '-' + day;
}

function getUpdateOrDeleteJournal(deleting){
    const textelement=document.getElementById('table_action_plug');
    const elements=document.querySelectorAll('.table_td_entries');
    if(elements[0].style['display']!=='') {
        textelement.style['display']='';
        journalformswitch.style['display']='none';
        document.getElementById('lable_apply_all').style['display']='none';
        elements.forEach(element =>
            element.style['display'] = ''
        );
        if (!deleting) {
            isDelete=false;
            textelement.innerText="Изменение записи";
        } else {
            textelement.innerText="Удаление записи";
            isDelete=true;
        }
    }else{
        journalformswitch.style['display']='';
        document.getElementById('lable_apply_all').style['display']='';
        textelement.style['display']='none';
        elements.forEach(element =>
            element.style['display'] = 'none'
        );
        isDelete=undefined;
        isDelete_id=undefined;
    }
}

function addJournalTableDataToForm(id){
    const datatable=document.getElementById('journal_entry_'+id);
    const group=datatable.querySelector('td[data-column="group"]').innerText;
    const student=datatable.querySelector('td[data-column="student"]').innerText;
    const subject=datatable.querySelector('td[data-column="subject"]').innerText;
    const date=formatDateAsDefault(datatable.querySelector('td[data-column="date"]').innerText);
    const grade=datatable.querySelector('td[data-column="grade"]').innerText;
    const waspresent=datatable.querySelector('td[data-column="waspresent"]').innerText;
    isDelete_id=id;
    if(grade!=='==') {
        let element = document.getElementById('form_journal_grade');
        element.value = grade;
    }
   element=document.getElementById('form_journal_group');
    element.value=group;
   element=document.getElementById('form_journal_student');
    element.value=student;
   element=document.getElementById('form_journal_date');
    element.value=date;
    element=document.getElementById('form_journal_subject');
    element.value=subject;
    if(waspresent!=='==') {
        if (waspresent==='+') {
            element = document.getElementById('form_journal_waspresent_true');
            element.checked = true;
        } else if(waspresent==='-') {
            element = document.getElementById('form_journal_waspresent_false');
            element.checked = true;
        }
    }else{
        element = document.getElementById('form_journal_waspresent_false');
        element.checked = null;
        element = document.getElementById('form_journal_waspresent_true');
        element.checked = null;
    }
}

function switchJournalForm(event){
    const switched=journalformswitch.checked;
    const delbtn=document.getElementById('form_journal_delete');
    const updbtn=document.getElementById('form_journal_update');
    document.getElementById('form_journal_group').value='';
    document.getElementById('form_journal_student').value='';
    if(switched){
        document.getElementById('form_journal_group').style['display']='';
        document.getElementById('form_journal_student').style['display']='none';
        document.getElementById('form_journal_group_br').style['display']='';
        document.getElementById('form_journal_student_br').style['display']='none';
        delbtn.style['display']='none';
        updbtn.style['display']='none';
    }else{
        delbtn.style['display']='';
        updbtn.style['display']='';
        document.getElementById('form_journal_group').style['display']='none';
        document.getElementById('form_journal_student').style['display']='';
        document.getElementById('form_journal_group_br').style['display']='none';
        document.getElementById('form_journal_student_br').style['display']='';
    }
}

function sortTable(event){
    event.preventDefault();
    const datestart = datepickerstart.value;
    const dateend = datepickerend.value;
    const subjectname=subjectpicker.value;
    const studentname =(studentpicker !== undefined && studentpicker !==null) ? studentpicker.value:null;
    const groupname =(grouppicker !== undefined && grouppicker !== null) ? grouppicker.value:null;
    const tbody = journaltable.querySelector('tbody');
    let rows = Array.from(tbody.querySelectorAll('tr'));
    rows.shift();
        rows=sortTableByDate(rows,datestart,dateend);
        rows=sortTableBySubjectName(rows,subjectname);
        if(studentname!=null) {
            rows = sortTableByStudentName(rows, studentname);
        }
        if(groupname!=null) {
            rows = sortTableByGroup(rows, groupname);
        }
}

function sortTableByDate(rows,datestart,dateend) {
    let k=0;
    return rows.filter(row => {
        const dateCell = row.querySelector('td[data-column="date"]');
        if (dateCell) {
            const dateText = dateCell.innerText.trim();
            let [day, month, year] = dateText.split('-').map(Number);
            if(parseInt(day,10)<10){
                day='0'+day;
            }
            if(parseInt(month,10)<10){
                month='0'+month;
            }
            const rowDate = year+'-'+month+'-'+day;
            console.log(rowDate);
            console.log(datestart);
            if ((datestart === '' || rowDate >= datestart) && (dateend === '' || rowDate <= dateend)) {
                row.style.display = '';
                return true;
            } else {
                row.style.display = 'none';
             return false;
            }
        }
       return false;
    });
}

function sortTableBySubjectName(rows,subjectname) {
        return rows.filter(row => {
           // console.log(row);
            const dateCell = row.querySelector('td[data-column="subject"]');
            if (dateCell) {
                const subjecttable = dateCell.innerText;
                if (subjecttable.includes(subjectname) || subjectname === '') {
                    row.style.display = '';
                    return true;
                } else {
                    row.style.display = 'none';
                    return false;
                }
            }
            return false;
        });
}

function sortTableByStudentName(rows,studentname){
    return rows.filter(row => {
       // console.log(row);
        const dateCell = row.querySelector('td[data-column="student"]');
        if (dateCell) {
            const studenttable = dateCell.innerText;
          //  console.log(studenttable);
            if (studenttable.includes(studentname) || studentname === '') {
                row.style.display = '';
                return true;
            } else {
                row.style.display = 'none';
                return false;
            }
        }
        return false;
    });
}

function sortTableByGroup(rows,groupname){
    return rows.filter(row => {
       // console.log(row);
        const dateCell = row.querySelector('td[data-column="group"]');
        if (dateCell) {
            const grouptable = dateCell.innerText;
            if (grouptable.includes(groupname) || groupname === '') {
                row.style.display = '';
                return true;
            } else {
                row.style.display = 'none';
                return false;
            }
        }
        return false;
    });
}

function showFilters(){
    const element=document.getElementById('filter_zone');
    if(element.style['display']==='none'){
        element.style['display']='';
    }else{
        element.style['display']='none';
    }
}

function sendJournalToSv() {
    let sendmethod='post';
    const jdate = document.getElementById('form_journal_date').value;
    const jsubjectlist = document.getElementById('form_journal_subject_list');
    const jsubjectlistvalue = document.getElementById('form_journal_subject');
    const jsubjectlistoptions = Array.from(jsubjectlist.options);
    let jsubject = jsubjectlistoptions.find(option => option.value === jsubjectlistvalue.value);
    jsubject = jsubject.id.toString().substring(jsubject.id.toString().indexOf('journal_form_subject_') + 21);
    const jgrade = document.getElementById('form_journal_grade').value !=='' ? document.getElementById('form_journal_grade').value : null;
    const waspresentinputs = document.querySelectorAll('input[name="waspresent"]');
    const is_any_waspresent_inputs_selected = Array.from(waspresentinputs).some(radio => radio.checked);
    const jwaspresent = is_any_waspresent_inputs_selected ? document.getElementById('form_journal_waspresent_true').checked : null;
    const journalformswitched = journalformswitch.checked;
    let jgroupstudentlist;
    let jgroupstudent;
    let sendurl;
    if (isDelete === undefined) {
        if (journalformswitched) {
            jgroupstudentlist = document.getElementById('form_journal_group_list');
            const jgroupstudentistoptions = Array.from(jgroupstudentlist.options);
            const jgroupstudentlistvalue = document.getElementById('form_journal_group');
            jgroupstudent = jgroupstudentistoptions.find(option => option.value === jgroupstudentlistvalue.value);
            jgroupstudent = jgroupstudent.id.toString().substring(jgroupstudent.id.toString().indexOf('journal_form_group_') + 19);
            sendurl = 'addJournalGroup';
        } else {
            jgroupstudentlist = document.getElementById('form_journal_user_list');
            const jgroupstudentistoptions = Array.from(jgroupstudentlist.options);
            const jgroupstudentlistvalue = document.getElementById('form_journal_student');
            jgroupstudent = jgroupstudentistoptions.find(option => option.value === jgroupstudentlistvalue.value);
            jgroupstudent = jgroupstudent.id.toString().substring(jgroupstudent.id.toString().indexOf('journal_form_student_') + 21);
            sendurl = 'addJournalUser';
        }
    }else{
        jgroupstudentlist = document.getElementById('form_journal_user_list');
        const jgroupstudentistoptions = Array.from(jgroupstudentlist.options);
        const jgroupstudentlistvalue = document.getElementById('form_journal_student');
        console.log(jgroupstudentlistvalue.value);
        jgroupstudent = jgroupstudentistoptions.find(option => option.value.includes(jgroupstudentlistvalue.value));
        jgroupstudent = jgroupstudent.id.toString().substring(jgroupstudent.id.toString().indexOf('journal_form_student_') + 21);
        if(isDelete){
            sendurl = 'delJournalUser/'+isDelete_id;
            sendmethod='delete';
        }else{
            sendurl = 'updJournalUser/'+isDelete_id;
            sendmethod='PATCH';
        }
    }
    if(jdate==='' || jsubject==='' || jgrade==='' || jgroupstudent==='' || jsubject===''){
        console.log("not enough data");
        return;
    }
    const senddata={
        id:null,
        journaluserId:null,
        journalsubjectId:null,
        date:jdate,
        grade:parseInt(jgrade,10),
        waspresent:jwaspresent,
        journaltasksCompletedId:null,
    }
    console.log(senddata);
    fetch(sendurl+'?'+new URLSearchParams({journaluserId : parseInt(jgroupstudent,10),journalsubjectId:parseInt(jsubject,10),}),{
        method: sendmethod,
        headers: {'Content-Type':'application/json',[csrfHeader]:csrfToken},
        body:JSON.stringify(senddata),
    }).then(response =>{
        if(!response.ok){
            console.log('error occured');
            return;
        }
        return response.json();
    }).then(data => {
        console.log(data.waspresent);
        location.reload();
    });
}

document.addEventListener('DOMContentLoaded', function() {
    journaltable=document.getElementById('journal_table');
    datepickerend=document.getElementById('filter_datepicker_end');
    datepickerstart=document.getElementById('filter_datepicker_start');
    subjectpicker=document.getElementById('filter_subject');
    studentpicker=document.getElementById('filter_student');
    grouppicker=document.getElementById('filter_group');
    journalformswitch=document.getElementById('form_journal_apply_to_all');
    datepickerstart.addEventListener('change',sortTable);
    datepickerend.addEventListener('change',sortTable);
    subjectpicker.addEventListener('input',sortTable);
    if(studentpicker!==undefined && studentpicker!==null) {
        studentpicker.addEventListener('input', sortTable);
    }
    if(grouppicker!==undefined && grouppicker!==null) {
        grouppicker.addEventListener('input', sortTable);
    }
    if(journalformswitch!==undefined && journalformswitch!==null) {
        journalformswitch.addEventListener('change', switchJournalForm);
    }
    formatDateColumnsInJournalTable();
},false);
