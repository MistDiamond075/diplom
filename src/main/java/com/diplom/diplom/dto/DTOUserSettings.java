package com.diplom.diplom.dto;

public class DTOUserSettings {
    public enum voiceModes{VOICE,PUSH_TO_TALK}
    public enum userDisplayModes{USERNAME,FULLNAME}
    public char[] keysPushToTalk=new char[2];
    public voiceModes voiceMode;
    public userDisplayModes userDisplay;

    public DTOUserSettings() {
    }

    public DTOUserSettings(char[] keysPushToTalk, voiceModes voiceMode, userDisplayModes userDisplay) {
        this.keysPushToTalk = keysPushToTalk;
        this.voiceMode = voiceMode;
        this.userDisplay = userDisplay;
    }

    public char[] getKeysPushToTalk() {
        return keysPushToTalk;
    }

    public void setKeysPushToTalk(char[] keysPushToTalk) {
        this.keysPushToTalk = keysPushToTalk;
    }

    public voiceModes getVoiceMode() {
        return voiceMode;
    }

    public void setVoiceMode(voiceModes voiceMode) {
        this.voiceMode = voiceMode;
    }

    public userDisplayModes getUserDisplay() {
        return userDisplay;
    }

    public void setUserDisplay(userDisplayModes userDisplay) {
        this.userDisplay = userDisplay;
    }
}
