var current_date;

function switchMonth(direction){
    let newmonth=direction==='left' ? current_date.getMonth()-1: current_date.getMonth()+1;
    let newyear=current_date.getFullYear();
    makeCalendar(new Date(newyear,newmonth,current_date.getDate()));
}

function showCalendarDayEvents(id){
    const parent= document.querySelector('.calendar-event-nothing');
    parent.style['display']='none';
    const elements=Array.from(document.querySelectorAll('.calendar-event'));
    console.log(elements);
    let isEventsExists=false;
    elements.forEach(element =>{
        console.log(element,isEventsExists);
        if(!isEventsExists) {
            isEventsExists = element.id.toString().includes(id);
        }
        element.style['display']=element.id.toString().includes(id) ? '' : 'none';
    });
    if(!isEventsExists){
        parent.style['display']='';
    }
}

function setCalendarActiveDay(id){
    const elements=Array.from(document.querySelectorAll('.today'));
    elements.forEach(element => element.classList.remove('today'));
    document.getElementById(id).classList.add('today');
}

function parseMonth(num,withEnding){
    const monthRusList=['Январь','Февраль','Март','Апрель','Май','Июнь','Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'];
    const monthRusListWithEnding=['Января','Февраля','Марта','Апреля','Мая','Июня','Июля','Августа','Сентября','Октября','Ноября','Декабря'];
    console.log('month '+num);
    return withEnding ?  monthRusListWithEnding[num] : monthRusList[num];
}

function parseTime(hours=null,minutes=null,seconds=null,separator=''){
    let newhours='';
    let newminutes='';
    let newseconds='';
    if(hours!==null) {
        if (parseInt(hours) < 10 && parseInt(hours) >= 0) {
            newhours = '0';
        }
        newhours += hours+separator;
    }
    if(minutes!==null){
        if(parseInt(minutes)<10 && parseInt(minutes)>=0){
            newminutes='0';
        }
        newminutes+=minutes+separator;
    }
    if(seconds!==null){
        if(parseInt(seconds)<10 && parseInt(seconds)>=0){
            newseconds='0';
        }
        newseconds+=seconds+separator;
    }
    return newhours+newminutes+newseconds;
}

function makeCalendar(date=new Date()) {// Sat Aug 31 2024 Thu Jun 06 2024
    const mainelement = document.getElementById('calendar_zone');
    const childs=Array.from(mainelement.children);
    childs.forEach(child =>{
        child.remove();
    });
    mainelement.innerHTML = '';
    console.log(date);
    const today = date;//135152353534
    const year = today.getFullYear();
    const month = today.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDay = firstDay.getDay() || 7;
    let dayscounter=startDay;
    const daysInMonth = lastDay.getDate();
    const daysOfWeek = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

    function fillEmptyDays(start,end,parent){
        for (let i = start; i < end; i++) {
            const el = document.createElement('div');
            el.className='calendar-empty-cell';
            parent.appendChild(el);
        }
    }

    function addSwitchMonthButton(direction,parent){
        let btn_month_switch=document.createElement('button');
        btn_month_switch.innerText=direction==='left' ? '◄':'►';
        btn_month_switch.setAttribute('onclick','switchMonth(\''+direction+'\')');
        parent.appendChild(btn_month_switch);
    }

    function makeCalendarUnderField(mainelement){
        const div=document.createElement('div');
        //div.innerText='joepeach';
        div.className='announcements-zone';
        mainelement.appendChild(div);
        const ulelement=document.createElement('ul');
        //testdata.innerHTML='<li>joepeach</li>';
        ulelement.id = 'announcements_zone';
        div.appendChild(ulelement);
        const span=document.createElement('span');
        span.innerText='Ничего нет ¯\\_(ツ)_/¯';
        span.className='calendar-event-nothing';
        span.style['display']='none';
        ulelement.appendChild(span);
    }

    const month_block=document.createElement('div');
    month_block.className='month-block';
    mainelement.appendChild(month_block);
    addSwitchMonthButton('left',month_block);
    const span=document.createElement('span');
    span.innerText=parseMonth(month,false)+' '+year;
    span.setAttribute('onclick','makeCalendar()');
    span.setAttribute('title','Сбросить');
    span.className='month-block-header';
    month_block.appendChild(span);
    addSwitchMonthButton('right',month_block);
    let btn_month_switch=document.createElement('button');
    btn_month_switch.innerText='◄';
    const element=document.createElement('div');
    element.className='calendar-cells';
    mainelement.appendChild(element);
    fillEmptyDays(1,startDay,element);
    for (let day = 1; day <= daysInMonth; day++) {
        const el = document.createElement('div');
        el.innerText = day;
        el.className='calendar-cell';
        if (day === today.getDate()) {
            el.classList.add('today');
        }
        if(dayscounter % 7===0){
            el.style['color']='red';
        }
        el.setAttribute('onclick','showCalendarDayEvents(\''+year+'-'+month+'-'+day+'\');setCalendarActiveDay(\''+month+'_'+day+'\')');
        el.id=month+'_'+day;
        element.appendChild(el);
        if(day!==daysInMonth) {
            dayscounter = dayscounter % 7 !== 0 ? (dayscounter + 1) : 1;
        }
       // console.log(day+' '+dayscounter);
    }
    if(dayscounter % 7!==0) {
        fillEmptyDays(dayscounter,7,element);
    }
    daysOfWeek.forEach(d => {
        const el = document.createElement('div');
        el.classList.add('day-header');
        //el.classList.add('calendar-cell');
        el.innerText = d;
        if(d==='Вс'){
            el.style['color']='red';
        }
        element.appendChild(el);
    });
    makeCalendarUnderField(mainelement);
    current_date=date;
    requestCalendarUnderFieldData(month,year);
    showCalendarDayEvents()
}

function requestCalendarUnderFieldData(month,year){
    let retid=null;
    fetch('/getUserData?'+new URLSearchParams({month:month+1,year:year}),{
        method:'get'
    }).then(response => {
        if (!response.ok) {
            console.log('failed to load data');
            throw new Error("request failed");
        }
        return response.json();
    }).then(data =>{
        console.log(data);
        const datenow=new Date();
        let iterator=0;
        
        function addDataToCalendarUnderField(datatype,dataelement,iterator){
            const parent = document.getElementById('announcements_zone');
            const lielement = document.createElement('li');
            const datadate =datatype==='task' ? new Date(dataelement.dateend) : new Date(dataelement.datestart);
            console.log(datenow);
            console.log(datadate);
            console.log('-------------');
            let isEqual = datadate.getDate() === datenow.getDate() && datadate.getMonth() === datenow.getMonth() && datadate.getFullYear() === datenow.getFullYear();
            if(datatype==='task') {
                lielement.innerText = isEqual ? 'Срок сдачи задания ' + dataelement.name + ' заканчивается сегодня в ' + parseTime(datadate.getHours()) + ':' + parseTime(null,datadate.getMinutes()):
                    'Срок сдачи задания ' + dataelement.name + (datadate>datenow ?  ' заканчивается ' : ' закончился ') + datadate.getDate() + ' ' + parseMonth(datadate.getMonth(), true)
                    + ' в ' + parseTime(datadate.getHours()) + ':' + parseTime(null,datadate.getMinutes());
            }else{
                lielement.innerText = isEqual ? 'Видеоконференция ' + dataelement.name + ' пройдёт сегодня в ' + parseTime(datadate.getHours()) + ':' + parseTime(null,datadate.getMinutes()) :
                    'Видеоконференция ' + dataelement.name + (datadate>datenow ?  ' пройдёт ' : ' закончилась ') + (datadate>datenow ?  datadate.getDate():new Date(dataelement.dateend).getDate())
                    + ' ' +(datadate>datenow ?   parseMonth(datadate.getMonth(), true) : parseMonth(new Date(dataelement.dateend).getMonth(),true)) + ' в ' +
                    (datadate>datenow ?  parseTime(datadate.getHours()) : parseTime(new Date(dataelement.dateend).getHours()))+ ':' + (datadate>datenow ?  parseTime(null,datadate.getMinutes()) :
                        parseTime(null,new Date(dataelement.dateend).getMinutes()));
            }
            lielement.id='calendar_event_'+iterator.toString()+'_'+datadate.getFullYear()+'-'+datadate.getMonth()+'-'+datadate.getDate();
            lielement.className='calendar-event';
            parent.appendChild(lielement);
            lielement.style['display']=isEqual ? '' : 'none';
            iterator++;
            if(isEqual){
                retid=datadate.getMonth()+'_'+datadate.getDate();
            }
            return iterator;
        }
        
        if(data.tasks.length>0) {
            data.tasks.forEach(task => {
                iterator=addDataToCalendarUnderField('task',task,iterator);
            });
        }
        if(data.conferences.length>0){
            data.conferences.forEach(conference => addDataToCalendarUnderField('conference',conference,iterator));
        }
        const announcements=document.getElementById('announcements_zone');
        announcements.querySelectorAll('li').forEach(el=>{
            if(el.style['display']!=='none'){
                const nothing= document.querySelector('.calendar-event-nothing');
                nothing.style['display']='none';
            }
        });
    });
    showCalendarDayEvents(retid);
}

document.addEventListener('DOMContentLoaded', function (){
    makeCalendar();
});