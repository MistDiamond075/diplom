let production_mode;
const response = await fetch('/admin/getMode');
if(response.ok){
    production_mode = await response.text();
}

export const CONFIG = {
    ws: production_mode==='true' ? "wss://5.189.10.253:60600" : "wss://192.168.0.102:60600",

    janusServerWs: "wss://5.189.10.253:60859",

    turn:{
        secret: "mn0dye2k54",
        urls: "turn:5.189.10.253:60868?transport=udp"
    },

    stun: "stun:5.189.10.253:60868",

    maxActiveFeeds: 1
}