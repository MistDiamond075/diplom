let chat_form_processing = false;
let user_menu_processing=false;
let chat_id=null;

const ChatWebSocketManager = (function () {
    let ws = null;
    let isConnected = false;
    let currentUserId = null;
    let currentChatId = null;

    function connect(userId, chatId) {
        if (isConnected || ws !== null) {
            console.warn("Already connected to chat websocket");
            return;
        }

        const ws_addr = "wss://5.189.10.253:60600";
        ws = new WebSocket(ws_addr);
        currentUserId = userId;
        currentChatId = chatId;

        ws.onopen = function () {
            const req = {
                event: "connected",
                eventType: "chat",
                chatId: chatId,
                userId: Number(userId)
            };
            ws.send(JSON.stringify(req));
            isConnected = true;
            console.log("WebSocket connected to chat " + chatId);
        };

        ws.onmessage = function (event) {
            const jsdata = JSON.parse(event.data);
            if(jsdata.eventType === "chat") {
                if (jsdata.event === "chatmsg") {
                    addMessageToChat(jsdata.message);
                }else if(jsdata.event==="removemsg"){
                    removeMessageFromChat(jsdata.messageId);
                }
            }
        };

        ws.onclose = function () {
            console.log("WebSocket closed");
            if (chatId !== null) {
                const request = {
                    event: "leave",
                    eventType: "chat",
                    chatId: currentChatId,
                    userId: currentUserId
                };
                try {
                    ws.send(JSON.stringify(request));
                } catch (e) {

                }
            }
            isConnected = false;
            ws = null;
        };
    }

    return {
        connectIfNeeded: function (userId, chatId) {
            if (!isConnected) {
                connect(userId, chatId);
            }
        },
        send: function (message) {
            if (ws && isConnected) {
                ws.send(message);
            }
        }
    };
})();

function showFilters(id){
    const element=document.getElementById(id);
    element.style['display']=element.style['display']==='none' ? '':'none';
}

function redirectToChatUpdatePage(id){
    window.location.href='chat/'+id+'/update';
}

function addTextareaResizeEvent(){
    const textarea = document.getElementById('message_input');

    textarea.addEventListener('input', () => {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px';
    });
}

function loadChatData(chatId,page=0){
    fetch('getMessages/'+chatId+'?'+new URLSearchParams({page:page}),{
        method:'get'
    }).then(response =>{
        chat_form_processing=false;
        let js=response.json();
        if(!response.ok){
            js.then(msg=> {
                showInfoMessage(msg.message!==undefined ? msg.message : 'Неопознанная ошибка');
            });
            return;
        }
        return js;
    }).then(data => {
        const msgContainer = document.querySelector('[id^="chat_messages_"]');
        console.log(data);
        msgContainer.id = 'chat_messages_' + chatId;
        msgContainer.querySelector('.messages-list').innerText = '';
        chat_id = chatId;
        const containerId = document.getElementById('chat_' + chatId);
        const header = document.querySelector('.chat-header');
        header.innerText = containerId.querySelector('.chat-name').innerText;
        const userId = document.getElementById('user_id').value;
        ChatWebSocketManager.connectIfNeeded(userId, chatId);
        const req = {
            event: "switch",
            eventType: "chat",
            chatId: chatId,
            userId: userId
        }
        ChatWebSocketManager.send(JSON.stringify(req));
        document.getElementById('message_input_chat_id').value = chatId;
        data.forEach(msg => {
            addMessageToChat(msg);
        });
        if(!user_menu_processing) {
            user_menu_processing = true;
            fetch("/chat/" + chatId + "/getUsers", {
                method: 'get'
            }).then(response => {
                let js = response.json();
                if (!response.ok) {
                    js.then(msg => {
                        showInfoMessage(msg.message !== undefined ? msg.message : 'Неопознанная ошибка');
                    });
                    return null;
                }
                user_menu_processing = false;
                return js;
            }).then(data => {
                if (data === null) {
                    return;
                }
                document.getElementById('users_menu').innerHTML='';
                data.membersList.forEach(user => {
                    addUserToUsersMenu(user);
                });
            });
        }
    });

    function addUserToUsersMenu(user){
        const container=document.getElementById('users_menu');
        if(container){
            const div=document.createElement('div');
            div.className='users-menu-element';
            container.appendChild(div);
            const img=document.createElement('img');
            img.src='/useravatar/'+user.id;
            img.className='users-menu-avatar';
            div.appendChild(img);
            const span=document.createElement('span');
            span.innerText=user.login;
            span.className='chat-user';
            div.appendChild(span);
        }
    }
}

function addMessageToChat(msg,replyTo){
    const container=document.querySelector('.messages-list');
    const userId=Number(document.getElementById('user_id').value);
    if(!document.getElementById('message_'+msg.id)){
        const date=new Date(msg.date);
        let dateDiv=document.getElementById('date_separator_'+DateTimeToFormat(date.getDate())+'_'+DateTimeToFormat(date.getMonth())+'_'+DateTimeToFormat(date.getFullYear()));
        if(!dateDiv){
            dateDiv=document.createElement('div');
            dateDiv.className='date-separator-container';
            dateDiv.id='date_separator_'+DateTimeToFormat(date.getDate())+'_'+DateTimeToFormat(date.getMonth())+'_'+DateTimeToFormat(date.getFullYear());
            container.appendChild(dateDiv);
            const span=document.createElement('span');
            span.className='date-separator';
            span.innerText=DateTimeToFormat(date.getDate())+'.'+DateTimeToFormat(date.getMonth())+'.'+DateTimeToFormat(date.getFullYear());
            dateDiv.appendChild(span);
        }
        const div=document.createElement('div');
        div.id='message_'+msg.id;
        div.className='chat-message';
        div.dataset.username=msg.userId.login;
        div.dataset.user_id=msg.userId.id;
        dateDiv.appendChild(div);
        const div2=document.createElement('div');
        div2.style.width='100%';
        div.appendChild(div2);
        const span_time=document.createElement('span');
        span_time.className='chat-message-time';
        span_time.innerText=DateTimeToFormat(date.getHours())+':'+DateTimeToFormat(date.getMinutes())+' ';
        const span_name=document.createElement('span');
        span_name.className='chat-message-username';
        span_name.style['color']=generateNameColor('#2e2e2e');
        span_name.innerText=msg.userId.login+': ';
        div2.appendChild(span_time);
        div2.appendChild(span_name);
        const span_text=document.createElement('span');
        span_text.className='chat-message-text';
        let messageText=msg.text;
       /* if(msg.replyTo!==null) {
            console.log(msg.replyTo.id === replyTo);
            if (msg.replyTo.id === replyTo) {
                const userName=msg.replyTo.login;
                const bgcolor= '#16b919';
                const textcolor= 'black';
                console.log(messageText);
                const pattern=new RegExp(`@${userName}\\b`, 'gi');
                messageText = messageText.toString().replace(pattern,`<span style="background:${bgcolor};color:${textcolor};padding: 1px;font-weight: bold;border-radius: 3px">@${userName}</span>`);
                console.log(messageText);
            }
        }*/
        span_text.innerHTML = messageText;
        div2.appendChild(span_text);
        if(userId===msg.userId.id) {
            const deleteButton = document.createElement('span');
            deleteButton.innerText = '❌';
            deleteButton.className='chat-message-deletebutton';
            deleteButton.setAttribute('onclick','deleteMessageFromChat(\'' + div.id + '\')');
            div.appendChild(deleteButton);
        }
    }
}

function removeMessageFromChat(id){
    if(!id){
        showInfoMessage("Идентификатор сообщения не валиден");
    }
    const msg=document.getElementById('message_'+id);
    if(msg){
        msg.remove();
    }else {
        showInfoMessage("Сообщение не найдено");
    }
}

function sendMessageToChat(chatId=null){
    if(!chat_form_processing) {
        chat_form_processing=true;
        if (chatId === null) {
            chatId = document.getElementById('message_input_chat_id').value;
        }
        console.log(chatId);
        if (!chatId instanceof Number || chatId < 1) {
            showInfoMessage("Не удалось получить идентификатор чата");
            return;
        }
        const text = document.getElementById('message_input').value;
        const senddata = {
            text: text
        };
        fetch('addMessage/' + chatId, {
            method: 'post',
            headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
            body: JSON.stringify(senddata)
        }).then(response => {
            chat_form_processing = false;
            let js = response.json();
            if (!response.ok) {
                js.then(msg => {
                    showInfoMessage(msg.message !== undefined ? msg.message : 'Неопознанная ошибка');
                });
                return;
            }
            return js;
        }).then(data => {
            console.log(data);
            document.getElementById('message_input').value = '';
            document.getElementById('message_input').style.height='100%';
        });
    }else{
        showInfoMessage("Предыдущее сообщение ещё не отправлено");
    }
}

function deleteMessageFromChat(messageElementId){
    if(!chat_form_processing) {
        chat_form_processing=true;
        const message = document.getElementById(messageElementId);
        if (!message) {
            showInfoMessage("Сообщение не найдено");
            return;
        }
        const messageId = message.id.substring(message.id.indexOf('_') + 1);
        fetch('deleteMessage/' + messageId, {
            method: 'delete',
            headers: {[csrfHeader]: csrfToken}
        }).then(response => {
            chat_form_processing = false;
            let js = response.json();
            if (!response.ok) {
                js.then(msg => {
                    showInfoMessage(msg.message !== undefined ? msg.message : 'Неопознанная ошибка');
                });
                return;
            }
            return js;
        }).then(data => {
            console.log(data);
            message.remove();
        });
    }
}

function addUserToUsersList(){
    console.log('added');
    document.getElementById('chatuser_selector').addEventListener('input', function (e) {
        const input = e.target;
        const value = input.value;
        console.log(value);
        console.log(document.getElementById('chatuser_list').innerText)
        if(document.getElementById('chatuser_list').innerText.includes(value)){
            return;
        }
        const datalist = document.getElementById('chatuser_selector_list');
        const options = Array.from(datalist.options);
        const match = options.find(opt => opt.value.toLowerCase() === value);
        if (match) {
            const element=document.createElement('span');
            const id_element='chatuser_'+document.getElementById('chatuser_list').children.length;
            element.innerText=value+'\t';
            element.dataset.group=match.textContent || match.label || value;
            element.className='chatuser-from-list';
            element.id=id_element;
            element.setAttribute('onclick','deleteFromChatUserList(\''+id_element+'\')');
            document.getElementById('chatuser_list').appendChild(element);
            input.value = '';
        }
    });
}

function deleteOldGroupUsers(groupName){
    if(!groupName || groupName===''){
        return;
    }
    document.getElementById('group_selector').addEventListener('input',function (e){
        const input = e.target;
        const value = input.value;
        const datalist = document.getElementById('group_selector_list');
        const options = Array.from(datalist.options);
        const match = options.find(opt => opt.value.toLowerCase() === value);
        if (match) {
            if(confirm("Удалить участников старой группы?")){
                const oldMemebers=document.querySelectorAll('.chatuser-from-list');
                oldMemebers.forEach(member =>{
                    if(member.dataset.group.includes(groupName) && !member.dataset.group.includes(value)){
                        member.remove();
                    }
                });
            }
        }
    });
}

function sendChatToSv(id){
    if(!chat_form_processing){
        chat_form_processing=true;
        const name=document.getElementById('chat_name').value;
        const users=Array.from(document.getElementById('chatuser_list').innerText.split(' '));
        const subjectname=document.getElementById('subject_selector').value;
        const group=document.getElementById('group_selector').value;
        const senddata={
            id:id,
            name:name,
            subjectName:subjectname,
            groupName:group,
            members:users
        };
        const url=window.location.href.includes('/create') ? window.location.href+'/addChat':window.location.href+'/updateChat';
        const method=window.location.href.includes('/create') ? 'post':'PATCH';
        fetch(url,{
            method:method,
            headers: {
                'Accept': 'application/json',
                'Content-Type':'application/json',
                [csrfHeader]: csrfToken
            },
            body:JSON.stringify(senddata)
        }).then(response =>{
            chat_form_processing=false;
            let js=response.json();
            if(!response.ok){
                js.then(msg=> {
                    showInfoMessage(msg.message!==undefined ? msg.message : 'Неопознанная ошибка');
                });
                return null;
            }
            return js;
        }).then(data => {
            console.log(data);
            if(data===null){
                return;
            }
            window.location.href='/chats';
        });
    }
}

function deleteChatFromSv(id){
    fetch(window.location.href+'/deleteChat/'+id,{
        method:'delete',
        headers:{[csrfHeader]: csrfToken}
    }).then(response =>{
        let js=response.json();
        if(!response.ok){
            js.then(msg=> {
                showInfoMessage(msg.message!==undefined ? msg.message : 'Неопознанная ошибка');
            });
            return null;
        }
        return js;
    }).then(data => {
        if(data===null){
            return;
        }
        console.log(data);
        const chat=document.getElementById('chat_'+id);
        if(chat){
            chat.remove();
        }
        const msgContainer=document.querySelector('[id^="chat_messages_"]');
        msgContainer.id='chat_messages_';
        msgContainer.querySelector('.messages-list').innerText='';
        chat_id=null;
        const header=document.querySelector('.chat-header');
        header.innerText='';
    });
}

function deleteFromChatUserList(id_element){
    document.getElementById(id_element).remove();
    document.getElementById('chatuser_selector').value='';
}

function menuChatUsersListener(){
    const btn=document.getElementById('users_menu_button');
    const container=document.getElementById('users_menu');
    if(btn!==undefined && container!==undefined){
        btn.addEventListener('click',function (event){
            console.log('23232323232');
            container.style.display=container.style.display==='none' ? '' : 'none';
        });
        document.addEventListener('click',function (event){
            console.log((event.target===btn || event.target===container));
            if(!(event.target===btn || event.target===container)){
                console.log(event.target);
                container.style.display='none';
            }
        });
    }
}

document.addEventListener('DOMContentLoaded',function (){
    const msgInput=document.getElementById('message_input');
    if(msgInput) {
    msgInput.addEventListener('keydown', function (event) {
            if (event.key === 'Enter') {
                if(!event.shiftKey) {
                    sendMessageToChat();
                }
            }
        });
    }
    /*if(typeof setSort!=='undefined') {

    }*/
    if(!window.location.href.includes('chats')){
        addUserToUsersList();
        if(window.location.href.includes('/update')) {
            deleteOldGroupUsers(document.getElementById('group_selector').value);
        }
    }else if(window.location.href.includes('chats')){
        addSettingsMenuListener('settings-block');
        menuChatUsersListener();
        addTextareaResizeEvent();
    }
},false);