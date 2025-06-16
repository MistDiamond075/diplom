let defaultlogin;
let defaultroles;
let defaultgroup;
let defaultstudentcard;
let defaultfname;
let defaultlname;
let defaultsname;
let defaultbirthdate;
let defaultemail;
let defaultqw;
let defaultqwansw;
let defaultpassword;

function switchProfileForm(canceled){
    const elements=Array.from(document.querySelectorAll('input'));
    const savebtn=document.getElementById('form_save_btn');
    const switchbtn=document.getElementById('form_switch_btn');
    const cancelbtn=document.getElementById('form_cancel_btn');
    const avatar_input=document.getElementById('attached_file');
    const avatar_overlay=document.getElementById('attached_file_overlay');
    avatar_overlay.style['display']=avatar_overlay.style['display']==='none'? '':'none';
    avatar_input.style['display']=avatar_input.style['display']==='none'? '':'none';
    document.getElementById('user_password').value='';
    document.getElementById('attached_file').style["display"]='';
    savebtn.style["display"]==='none' ? savebtn.style["display"]='':savebtn.style["display"]='none';
    cancelbtn.style["display"]==='none' ? cancelbtn.style["display"]='':cancelbtn.style["display"]='none';
    switchbtn.style["display"]==='none' ? switchbtn.style["display"]='':switchbtn.style["display"]='none';
    elements.forEach(element=>{
        element.style['pointer-events']==='none' ? element.style['pointer-events']='':element.style['pointer-events']='none';
    });
    document.getElementById('attached_file').style["pointer-events"]='';
    const rolesfield=elements.find(element => element.id==='user_role');
    if(rolesfield!==undefined || rolesfield!==null){
        rolesfield.style['pointer-events']='';
    }
    if(canceled){
        document.getElementById('attached_file').style["display"]='none';
        loadDefaultValues();
    }
}

function loadDefaultValues(){
    document.getElementById('user_login').value=defaultlogin;
    document.getElementById('user_role').value=defaultroles;
    document.getElementById('user_group').value=defaultgroup;
    document.getElementById('user_studentcard').value=defaultstudentcard;
    document.getElementById('user_fname').value=defaultfname;
   document.getElementById('user_lname').value=defaultlname;
    document.getElementById('user_sname').value=defaultsname;
    document.getElementById('user_birthdate').value=defaultbirthdate;
    document.getElementById('user_email').value=defaultemail;
    document.getElementById('user_qwestion').value=defaultqw;
    document.getElementById('user_qwestion_answer').value=defaultqwansw;
    document.getElementById('user_password').value=defaultpassword;
    console.log(defaultpassword);
}

function updateDefaultValues(){
    defaultlogin=document.getElementById('user_login').value;
    defaultroles=document.getElementById('user_role').value;
    defaultgroup=document.getElementById('user_group').value;
    defaultstudentcard=document.getElementById('user_studentcard').value;
    defaultfname=document.getElementById('user_fname').value;
    defaultlname=document.getElementById('user_lname').value;
    defaultsname=document.getElementById('user_sname').value;
    defaultbirthdate=document.getElementById('user_birthdate').value;
    defaultemail=document.getElementById('user_email').value;
    defaultqw=document.getElementById('user_qwestion').value;
    defaultqwansw=document.getElementById('user_qwestion_answer').value;
    defaultpassword=document.getElementById('user_password').value;
    console.log('pwold: '+defaultpassword);
}

function sendUpdateProfileToSv(){
    const form_data = new FormData();
    let attached_files = document.getElementById('attached_file').files;
    const uid=document.getElementById('user_id').value;
    const ulogin=document.getElementById('user_login').value;
    let urole=document.getElementById('user_role').value;
    const ugroup=document.getElementById('user_group').value;
    const ustudcard=document.getElementById('user_studentcard').value;
    const ufname=document.getElementById('user_fname').value;
    const ulname=document.getElementById('user_lname').value;
    const usname=document.getElementById('user_sname').value;
    const ubirthdate=document.getElementById('user_birthdate').value;
    const uemail=document.getElementById('user_email').value;
    const uqw=document.getElementById('user_qwestion').value;
    let upassword=document.getElementById('user_password').value;
    const uqwansw=document.getElementById('user_qwestion_answer').value;
    if(upassword===''){
        upassword=defaultpassword;
    }
    if(urole===''){
        urole=defaultroles;
    }
    if(ulogin==='' || ufname==='' || ulname==='' || ubirthdate==='' || uemail==='' || uqw==='' || uqwansw==='' || upassword==='' || uid===''){
        console.log('not enough data');
        return;
    }
    const senddata={
        id: null,
        login: ulogin,
        password:upassword,
        firstname: ufname,
        lastname: ulname,
        surname: usname,
        dateofbirth: ubirthdate,
        email: uemail,
        qwestion:uqw,
        qwestionanswer: uqwansw,
        studentcard:parseInt(ustudcard,10)
    }
    console.log(senddata);
    if(attached_files.length>0) {
        if (attached_files[0].size <= 0 && attached_files[0].name === "") {
            attached_files = null;
        } else {
            form_data.append('imgfile', attached_files[0]);
            form_data.append('userId', uid);
            fetch('/addUserAvatar', {
                method: 'post',
                headers: {
                    [csrfHeader]: csrfToken,
                },
                body: form_data,
            }).then(response => {
                if (!response.ok) {
                    console.log('error occured');
                    return;
                }
                return response.json();
            }).then(data => {
                console.log(JSON.stringify(data));
                updateDefaultValues();
            });
        }
    }
    fetch('/updUser/'+uid+'?'+new URLSearchParams({group_name:ugroup}),{
        method: 'PATCH',
        headers:{'Content-Type':'application/json',[csrfHeader]:csrfToken},
        body:JSON.stringify(senddata),
    }).then(response =>{
        if(!response.ok){
            console.log('error occured');
            return;
        }
        return response.json();
    }).then(data => {
        console.log(JSON.stringify(data));
        updateDefaultValues();
        location.reload();
    });
}

document.addEventListener('DOMContentLoaded', function(){
   updateDefaultValues();
   defaultpassword=null;
   document.getElementById('user_avatar').onload=() =>{
   }
},false);