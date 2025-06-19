const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
let form_processing=false;

function goToRegPage(){
    document.location.href='registrationpage';
}

function goToLoginPage(){
    document.location.href='loginpage';
}

function Registration(){
    if(!form_processing) {
        form_processing=true;
        const group = document.getElementById('reg_form_group').value;
        const lname = document.getElementById('reg_form_lname').value;
        const fname = document.getElementById('reg_form_fname').value;
        const sname = document.getElementById('reg_form_sname').value;
        const bday = document.getElementById('reg_form_birthday').value;
        const login = document.getElementById('reg_form_login').value;
        const pw = document.getElementById('reg_form_password').value;
        const email = document.getElementById('reg_form_mail').value;
        const qwestion = document.getElementById('qwestion_selector').value;
        const qwestion_answ = document.getElementById('reg_form_qwestion_answ').value;
        const studentcard = document.getElementById('reg_form_stud_bilet').value;
        if (group === '' || lname === '' || fname === '' || bday === '' || login === '' || pw === '' || email === '' || qwestion === '' || qwestion_answ === '') {
            return;
        }
        const senddata = {
            id: null,
            login: login,
            password: pw,
            firstname: fname,
            lastname: lname,
            surname: sname,
            dateofbirth: bday,
            email: email,
            qwestion: qwestion,
            qwestionanswer: qwestion_answ,
            studentcard: studentcard
        }
        fetch('registrationpage/regUser?' + new URLSearchParams({groupname: group}), {
            method: 'post',
            headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
            body: JSON.stringify(senddata),
        }).then(response => {
            if (!response.ok) {
                console.log('error');
            }
            form_processing=false;
            return response.json();
        }).then(data => {
            console.log(data);
            document.location.href = "/";
        });
    }
}

function sendPWRestoreRequest(){
    if(!form_processing) {
        form_processing=true;
        const mail = document.getElementById('pwr_form_mail').value;
        const cardnumber = document.getElementById('pwr_form_studentcard').value;
        const qwansw = document.getElementById('pwr_form_qwansw').value;
        if (cardnumber === '' || qwansw === '') {
            console.log("not enough data");
            return;
        }
        const senddata = {
            id: null,
            login: "",
            password: "",
            firstname: "",
            lastname: "",
            surname: "",
            dateofbirth: "",
            email: mail,
            qwestion: "PETNAME",
            qwestionanswer: qwansw,
            studentcard: cardnumber
        }
        let sendurl = 'pwrestorepage/request';
        if (mail !== '') {
            sendurl = 'pwrestorepage/request?' + new URLSearchParams({email: mail});
        }
        fetch(sendurl, {
            method: 'post',
            headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
            body: JSON.stringify(senddata),
        }).then(response => {
            if (!response.ok) {
                console.log('error');
            }
            form_processing=false;
            return response.json();
        }).then(data => {
            console.log(data);
            document.location.href = "/";
        });
    }
}

function PWRestore(){
    if(!form_processing) {
        form_processing=true;
        const uuid = document.getElementById('pwr_form_uuid').value;
        const user_id = document.getElementById('pwr_form_user_id').value;
        const newpw = document.getElementById('pwr_form_newpassword').value;
        const newpwrepeated = document.getElementById('pwr_form_newpassword_repeat').value;
        if (uuid === '' || user_id === '') {
            console.log("not enough data");
            return;
        }
        if (newpw !== newpwrepeated) {
            console.log("password's doesn't match")
        }
        const senddata = {
            id: null,
            login: "",
            password: newpw,
            firstname: "",
            lastname: "",
            surname: "",
            dateofbirth: "",
            email: "",
            qwestion: "PETNAME",
            qwestionanswer: "",
            studentcard: ""
        }
        fetch('/pwrestorepage/pwupdate?' + new URLSearchParams({uuid: uuid, user_id: user_id}), {
            method: 'post',
            headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
            body: JSON.stringify(senddata),
        }).then(response => {
            if (!response.ok) {
                console.log('error');
                document.getElementById('form_plug').innerText = "Ошибка сервера";
            }
            form_processing=false;
            return response.json();
        }).then(data => {
            console.log(data);
            document.getElementById('form_plug').innerText = "Письмо для восстановления отправлено на почту";
            document.location.href = "/";
        });
    }
}