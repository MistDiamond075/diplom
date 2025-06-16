function setSort(inputdate1Id,inputdate2Id,inputnameId,inputgroupId,inputsubjectId,dataclass,inputdateclass,inputnameclass,inputgroupclass,inputsubjectclass){
let dateelement1=inputdate1Id;
let dateelement2=inputdate2Id;
let nameelement=inputnameId;
let groupelement=inputgroupId;
let subjectelement=inputsubjectId;
let elementsclass=dataclass;
let elementsdateclass=inputdateclass;
let elementsnameclass=inputnameclass;
let elementsgroupclass=inputgroupclass;
let elementssubjectclass=inputsubjectclass;
console.log(document.getElementById(subjectelement));


function sortData(event){
    event.preventDefault();
    let elements=Array.from(document.querySelectorAll(elementsclass));
    const datestart=document.getElementById(dateelement1).value;
    const dateend=document.getElementById(dateelement2).value;
    const name=document.getElementById(nameelement).value;
    const group=document.getElementById(groupelement).value;
    const subject=document.getElementById(subjectelement).value;
    elements=sortDataByDate(elements,datestart,dateend);
    elements=sortDataByTaskname(elements,name);
    elements=sortDataByGroup(elements,group);
    elements=sortDataBySubject(elements,subject);
}

function sortDataByDate(elements,datestart,dateend){
    return elements.filter(element => {
        let element_date=element.querySelector(elementsdateclass).innerText;
        element_date=element_date.substring(0,element_date.indexOf(' '));
        if ((datestart === '' || element_date >= datestart) && (dateend === '' || element_date <= dateend)) {
            element.style.display = '';
            return true;
        } else {
            element.style.display = 'none';
            return false;
        }
    });
}

function sortDataByTaskname(elements,taskname){
    return elements.filter(element => {
        let element_name=element.querySelector(elementsnameclass).innerText;
        if (taskname==='' || element_name.includes(taskname)) {
            element.style.display = '';
            return true;
        } else {
            element.style.display = 'none';
            return false;
        }
    });
}

function sortDataByGroup(elements,group){
    return elements.filter(element => {
        let element_values=Array.from(element.querySelectorAll(elementsgroupclass));
        let isVisible=false;
        if(element_values.length<1 && group!==''){
            element.style.display = 'none';
            return false;
        }
        element_values.forEach(element_value =>{
            let value= element_value.tagName.toLowerCase()==='input' ? element_value.value : element_value.innerText;
            if (group==='' || value.includes(group)) {
                element.style.display = '';
                isVisible=true;
            } else {
                element.style.display = 'none';
                isVisible=false;
            }
            if(isVisible){return isVisible;}
        });
        return isVisible;
    });
}

function sortDataBySubject(elements,subject){
    console.log(elements);
    console.log(subject);
    return elements.filter(element =>{
        console.log(element);
        let element_subject=element.querySelector(elementssubjectclass).innerText;
        if (subject==='' || element_subject.includes(subject)) {
            element.style.display = '';
            return true;
        } else {
            element.style.display = 'none';
            return false;
        }
    });
}

function addFiltersEventListeners(){
    document.getElementById(dateelement1).addEventListener('change',sortData);
    document.getElementById(dateelement2).addEventListener('change',sortData);
    document.getElementById(nameelement).addEventListener('input',sortData);
    if(document.getElementById(groupelement)) {
        document.getElementById(groupelement).addEventListener('input', sortData);
    }
    document.getElementById(subjectelement).addEventListener('input',sortData);
}

addFiltersEventListeners();

return {
    sort:sortData
};
}