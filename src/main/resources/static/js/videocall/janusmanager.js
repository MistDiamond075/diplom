import {CONFIG} from "./config.js";
import {UiManager} from "./uimanager.js";
import {VideoCallUtils} from "./utils.js";
import {defaultStates} from "./constants";

export class JanusManager {
    constructor(feedManager,pushToTalkManager) {
        this.feedManager = feedManager;
        this.pushToTalkManager = pushToTalkManager;
        this.janus = null;
        this.opaqueId = undefined;
        this.roomId = undefined;
        this.debugHandle = undefined;
        this.videoroomHandle = null;
        this.subscribeHandle = new Map();
    }

    #generateTurnCredentials(secret) {
        const unixTimeStamp = Math.floor(Date.now() / 1000) + 3600;
        const username = `${unixTimeStamp}`;
        const password = CryptoJS.HmacSHA1(username, secret).toString(CryptoJS.enc.Base64);
        return {username, credential: password};
    }

    async start(roomId, username, opaqueId, microstate = defaultStates.OFF, camerastate = defaultStates.OFF, user_id) {
        const {username: turnUsername, credential: turnCredential} = this.#generateTurnCredentials(CONFIG.turn.secret);
        this.opaqueId = opaqueId;
        this.roomId = roomId;
        this.janus = new Janus({
            server:  CONFIG.janusServerWs,
            iceServers: [
                {urls: CONFIG.stun},
                {
                    urls: CONFIG.turn.urls,
                    username: turnUsername,
                    credential: turnCredential
                }
            ],
            success: function () {
                this.janus.attach({
                    plugin: "janus.plugin.videoroom",
                    opaqueId: opaqueId,
                    success: function (pluginHandle) {
                        this.videoroomHandle = pluginHandle;
                        this.debugHandle = pluginHandle;
                        const register = {
                            request: "join",
                            room: roomId,
                            ptype: "publisher",
                            display: username
                        };
                        this.videoroomHandle.send({message: register});
                    },
                    onmessage: async function (msg, jsep) {
                        console.log("Received message:", msg);
                        if (msg.videoroom === "joined") {
                            connectToVideocallWs(roomId, user_id, this.videoroomHandle);
                            this.feedManager.ownFeed = msg.id;
                            const publishers = msg.publishers || [];

                            if (publishers.length === 0) {
                                await publishOwnFeed(this.videoroomHandle, user_id);
                            } else {
                                for (let i = 0; i < publishers.length; i++) {
                                    const publisher = publishers[i];
                                    const display = publisher.display;
                                    if (publisher.id !== this.feedManager.ownFeed) {
                                        console.log("👤 Новый участник:", display + ' ' + publisher.id);
                                        subscribe(publisher);
                                    }
                                }
                                await publishOwnFeed(this.videoroomHandle);
                            }
                        }

                        if (msg.videoroom === "talking") {
                            const talkingFeedId = msg.id;
                            if (talkingFeedId === this.feedManager.ownFeed) {
                                return;
                            }
                            if (this.feedManager.checkActiveMax('gte')) {
                                let oldest = this.feedManager.getOldest();
                                if (oldest) {
                                    await toggleVideo(talkingFeedId, false);
                                    this.feedManager.removeActive(talkingFeedId);
                                }
                            }
                            this.feedManager.addActive(talkingFeedId);
                            const userId = this.feedManager.get(feedManager.MapKey.FEED,talkingFeedId);
                            const container=document.querySelector(`#user_${userId}`);
                            if (VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(container, 'cam')) === defaultStates.ON) {
                                await toggleVideo(talkingFeedId, true);
                            }
                            this.feedManager.removeTimeout(talkingFeedId);
                            UiManager.lightUser(userId, true);
                        }

                        if (msg.videoroom === "stopped-talking") {
                            const feedId = msg.id;
                            if (feedId === this.feedManager.ownFeed) {
                                return;
                            }

                            if (subscriberHandle.has(feedId) && this.feedManager.isActive(feedId)) {
                                const userId = this.feedManager.get(this.feedManager.MapKey.FEED,feedId);
                                if (this.feedManager.checkActiveMax('gt')) {
                                    console.log('UNSUBBED');
                                    const timeout = setTimeout(() => {
                                        console.log('TIMEOUT');
                                        const container=document.querySelector(`#user_${userId}`);
                                        if (VideoCallUtils.parseDefaultState(VideoCallUtils.getParticipantSettingState(container, 'cam')) === defaultStates.ON) {
                                            toggleVideo(feedId, false);
                                        }
                                        this.feedManager.removeTimeout(feedId);
                                    }, 5000);

                                    this.feedManager.addTimeout(feedId,timeout);
                                }
                                UiManager.lightUser(userId, false);
                            }
                        }

                        if (msg.videoroom === "event") {
                            if (msg.leaving || msg.unpublished) {
                                const leavingFeed = msg.leaving || msg.unpublished;
                                if (leavingFeed === this.feedManager.ownFeed) {
                                    return;
                                }
                                unsubscribeFromPublisher(leavingFeed);
                                const userId = this.feedManager.get(this.feedManager.MapKey.FEED,leavingFeed);
                                this.feedManager.remove(leavingFeed,userId);
                                const users = document.querySelectorAll('[class*="user-participant"]');
                                users.forEach(user => {
                                    if (this.feedManager.checkActiveMax('gte')) {
                                        return;
                                    }
                                    const state = VideoCallUtils.getParticipantSettingState(user, 'cam');
                                    console.log(state);
                                    if (state !== null) {
                                        if (VideoCallUtils.parseDefaultState(state) === defaultStates.ON) {
                                            const userId = Number(user.id.substring(user.id.indexOf('_') + 1));
                                            const feedId = this.feedManager.get(this.feedManager.MapKey.USER,userId);
                                            if (feedId) {
                                                toggleVideo(feedId, true);
                                                this.feedManager.addActive(feedId);
                                            }
                                        }
                                    }
                                });
                            }
                            if (msg.publishers) {
                                const publishers = msg.publishers;
                                for (let i = 0; i < publishers.length; i++) {
                                    const publisher = publishers[i];
                                    console.log("📡 Новый опубликованный поток:", publisher.display, publisher.id);
                                    if (publisher.id === this.feedManager.ownFeed) {
                                        return;
                                    }
                                    if (!subscriberHandle.has(publisher.id)) {
                                        subscribe(publisher);
                                    }
                                }
                            }
                            if (msg.configured === "ok" && !devices_start_state_updated) {
                                devices_start_state_updated = true;
                                await updateDeviceWithTracks(false, microstate);
                                await updateDeviceWithTracks(true, camerastate);
                            }
                        }

                        if (jsep) {
                            this.videoroomHandle.handleRemoteJsep({jsep: jsep});
                        }
                    }
                });
            }
        });
    }
}