package com.diplom.diplom.dto;

public class DTOUserSettings {
    public enum voiceModes{VOICE,PUSH_TO_TALK}
    public enum userDisplayModes{USERNAME,FULLNAME}
    private String[] keysPushToTalk=new String[2];
    private String portPushToTalk;
    private Integer soundsVolume;
    private voiceModes voiceMode;
    private userDisplayModes userDisplay;

    public DTOUserSettings() {
    }

    public DTOUserSettings(String[] keysPushToTalk, voiceModes voiceMode, userDisplayModes userDisplay, String portPushToTalk, Integer soundsVolume) {
        this.keysPushToTalk = keysPushToTalk;
        this.voiceMode = voiceMode;
        this.userDisplay = userDisplay;
        this.portPushToTalk = portPushToTalk;
        this.soundsVolume = soundsVolume;
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

    public Integer getSoundsVolume() {
        return soundsVolume;
    }

    public void setSoundsVolume(Integer soundsVolume) {
        this.soundsVolume = soundsVolume;
    }
}
