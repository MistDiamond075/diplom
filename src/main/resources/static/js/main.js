const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

class Pair{
    constructor(first, second) {
        this.first = first;
        this.second = second;
    }
}

function redirectToUserProfile(id=null){
    window.location.href='/profile'+(id!==null ? '?'+new URLSearchParams({id:id}) : '');
}

function formCharCount(input_id,counter_id){
    const textarea = document.getElementById(input_id);
    const form_length_max=textarea.getAttribute('maxlength');
    const charCount = textarea.value.length;
    document.getElementById(counter_id).textContent = `${charCount}/`+form_length_max;
}

function getDateTime(){
    const date=new Date;
    return DateTimeToFormat(date.getFullYear())+"-"+DateTimeToFormat(date.getMonth()+1)+"-"
        +date.getDate()+" "+DateTimeToFormat(date.getHours())+":"+DateTimeToFormat(date.getMinutes());
}

function DateTimeToFormat(dateortime){
    if(dateortime.toString().length===1){
        return "0"+dateortime;
    }else{
        return dateortime;
    }
}

function showSettingsMenu(id){
    event.stopPropagation();
    const settings=document.getElementById('settings_'+id);
    if(settings.style['display']==='none'){
        const allsettings=Array.from(document.getElementsByClassName('settings-block'));
        allsettings.forEach(setting=>setting.style['display']='none');
        settings.style['display']='flex';
    }else{
        settings.style['display']='none';
    }
}

function addSettingsMenuListener(menu_classname){
    document.addEventListener('click', (event) => {
        document.querySelectorAll('.'+menu_classname).forEach(menu => {
            const btn = menu.previousElementSibling;
            if (!menu.contains(event.target) && !btn.contains(event.target)) {
                menu.style.display = 'none';
            }
        });
    });

    document.querySelectorAll('.'+menu_classname).forEach((btn, index) => {
        btn.addEventListener('click', (event) => {
            const menu = btn.nextElementSibling;
            menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
            event.stopPropagation(); // Чтобы не закрывалось моментально
        });
    });
}

function addSettingsMenuEvent(event,btn_classname,menu_classname){
    const str='.'+btn_classname+', .'+menu_classname;
    if(!event.target.closest(str)){
        document.querySelector('.' + menu_classname).style['display']='none';
    }
    console.log(event.target.closest)
}

function getRandomColor(){
    const symbols='0123456789abcdef';
    let color='#';
    for (let i = 0; i < 6; i++) {
        color += symbols[Math.floor(Math.random() * 16)];
    }
    return color;
}

function generateNameColor(bgcolor='#000000'){
    function getBright(color){
        const rgb = color.slice(1).match(/.{2}/g).map(val => parseInt(val, 16) / 255);
        const [r, g, b] = rgb.map(val => {
            return val <= 0.03928 ? val / 12.92 : Math.pow((val + 0.055) / 1.055, 2.4);
        });
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    function getContrast(color1,color2){
        const cntrst1 = getBright(color1);
        const cntrst2 = getBright(color2);
        const brightest = Math.max(cntrst1, cntrst2);
        const darkest = Math.min(cntrst1, cntrst2);
        return (brightest + 0.05) / (darkest + 0.05);
    }

    function getMatchedColorForColor(color){
        let newcolor=getRandomColor();
        if(getContrast(color,newcolor)<4) {
            while (getContrast(color, newcolor) < 4) {
                newcolor = getRandomColor();
            }
        }
        return newcolor;
    }

    return getMatchedColorForColor(bgcolor);
}

function showInfoMessage(text,isError=true){
    const messageContainer=document.querySelector('.info-messages');
    if(messageContainer) {
        messageContainer.style['display']='flex';
        const span=messageContainer.querySelector('span');
        if(span){
            span.innerText=text;
        }
        messageContainer.style.background=isError ? '#c53636d1' : '#2ba535d1';
        setTimeout(() => {
            messageContainer.setAttribute('style', 'display:none');
            span.innerText='';
        }, 7000);
    }
}

const observer = new IntersectionObserver(entries => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            const el = entry.target;
            const actualSrc = el.getAttribute('data-src');
            if(actualSrc) {
                el.setAttribute('src', actualSrc);
                if (el.parentElement.tagName.toLowerCase() === 'video' || el.parentElement.tagName.toLowerCase() === 'audio') {
                    const source = document.createElement('source');
                    source.setAttribute('src', actualSrc);
                    source.type = el.tagName.toLowerCase() + '/mp4';
                    el.appendChild(source);
                    el.parentElement.load();
                }
            }
            observer.unobserve(el);
        }
    });
}, { threshold: 0.1 });

function observeLazyElement(el) {
    observer.observe(el);
}

function logout(){
    fetch('logout',{
        method: 'post',
        headers:{
            [csrfHeader]: csrfToken,
        }
    }).then(response =>{
        if(!response.ok){
            showInfoMessage("Непредвиденная ошибка");
        }else{
            window.location.href='loginpage';
        }
    });
}

document.addEventListener('DOMContentLoaded',function () {
    const settingsBtn=document.querySelector('.allsettings-button-show');
    if(settingsBtn){
        settingsBtn.addEventListener('click',showSettingsAll);
        addSettingsContainerListener();
    }
},false);