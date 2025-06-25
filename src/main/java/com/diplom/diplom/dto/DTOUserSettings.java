package com.diplom.diplom.dto;

public class DTOUserSettings {
    public enum voiceModes{VOICE,PUSH_TO_TALK}
    public enum userDisplayModes{USERNAME,FULLNAME}
    public String[] keysPushToTalk=new String[2];
    public String portPushToTalk;
    public voiceModes voiceMode;
    public userDisplayModes userDisplay;

    public DTOUserSettings() {
    }

    public DTOUserSettings(String[] keysPushToTalk, voiceModes voiceMode, userDisplayModes userDisplay, String portPushToTalk) {
        this.keysPushToTalk = keysPushToTalk;
        this.voiceMode = voiceMode;
        this.userDisplay = userDisplay;
        this.portPushToTalk = portPushToTalk;
    }

    public String[] getKeysPushToTalk() {
        return keysPushToTalk;
    }

    public void setKeysPushToTalk(String[] keysPushToTalk) {
        this.keysPushToTalk = keysPushToTalk;
    }

    public String getPortPushToTalk() {
        return portPushToTalk;
    }

    public void setPortPushToTalk(String portPushToTalk) {
        this.portPushToTalk = portPushToTalk;
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
