import {CONFIG} from "./config.js";

export class FeedManager{
    constructor (maxActive = undefined) {
        this.maxActive = maxActive ? maxActive : CONFIG.maxActiveFeeds;
        this.active = new Set();
        this.timeoutFeeds = new Map();
        this.feedId_userId = new Map();
        this.userId_feedId = new Map();
        this.ownFeed= undefined;
        this.IdTypes = {USER: 'User',FEED:'FEED'};
    }

    contains(idType,target){
        switch(idType){
            case this.IdTypes.USER:{
                return this.userId_feedId.has(target);
            }
            case this.IdTypes.FEED:{
                return this.feedId_userId.has(target);
            }
        }
        console.warn(`${idType} is not supported in FeedManager`);
        return false;
    }

    get(idType,target){
        switch(idType){
            case this.IdTypes.USER:{
                return this.userId_feedId.get(target);
            }
            case this.IdTypes.FEED:{
                return this.feedId_userId.get(target);
            }
        }
        console.warn(`${idType} is not supported in FeedManager`);
        return false;
    }

    isActive(target){
        return this.active.has(target);
    }

    addTimeout(feedId,timeout){
        this.timeoutFeeds.set(feedId, new Map().set(timeout, Date.now()));
    }

    add(idType,key, value){
        switch (idType){
            case this.IdTypes.USER:{
                this.userId_feedId.set(key,value);
            break;}
            case this.IdTypes.FEED:{
                this.feedId_userId.set(key,value);
            break;}
            default:{
                console.warn(`${idType} is not supported in FeedManager`);
            }
        }
    }

    remove(feedId = undefined, userId = undefined){
        if(feedId){
            this.feedId_userId.delete(feedId);
        }
        if (userId){
            this.userId_feedId.delete(userId);
        }
    }

    removeTimeout(feedId) {
        const timeout = this.timeoutFeeds.get(feedId);
        clearTimeout(timeout?.get(feedId)?.entries()?.next()?.value[0]);
        this.timeoutFeeds.delete(feedId);
    }

    getOldest(){
        return this.active.values().next().value;
    }

    addActive(feedId){
        this.active.add(feedId);
    }

    removeActive(feedId){
        this.active.delete(feedId);
    }

    checkActiveMax(operation){
        switch (operation){
            case 'gt':{
                return this.active.size > this.maxActive;
            }
            case 'gte':{
                return this.active.size >= this.maxActive;
            }
            case 'lt':{
                return this.active.size < this.maxActive;
            }
            case 'lte':{
                return this.active.size <= this.maxActive;
            }
            case 'eq':{
                return this.active.size === this.maxActive;
            }
            default: {
                return this.active.size < this.maxActive;
            }
        }
    }

    clearAll(){
        this.userId_feedId.clear();
        this.feedId_userId.clear();
        this.active.clear();
        this.timeoutFeeds.clear();
    }
}