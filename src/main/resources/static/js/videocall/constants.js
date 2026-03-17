export const sounds = {
    DEMOSTART: new Audio('/files/sound/videocall/demo_start.wav'),
    DEMOEND: new Audio('/files/sound/videocall/demo_end.wav'),
    VOICESTART: new Audio('/files/sound/videocall/voice_start.wav'),
    VOICEEND: new Audio('/files/sound/videocall/voice_end.wav'),
    JOIN: new Audio('/files/sound/videocall/join.wav'),
    LEAVE: new Audio('/files/sound/videocall/leave.wav')
};
sounds.JOIN.playbackRate = 1.3;

export const defaultStates = {
    OFF: 'OFF',
    ON: 'ON',
    MUTED_BY_ADMIN: 'MUTED_BY_ADMIN'
};

export const Actions = {
    MICROPHONE: 'AUDIO',
    CAMERA: 'VIDEO',
    BAN: 'BAN',
    SOUND: 'SOUND',
    DEMONSTRATION: 'DEMONSTRATION'
};

export const iconsVideocallUrl = '/files/icons/videocall';