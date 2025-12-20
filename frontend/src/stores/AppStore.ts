import { makeAutoObservable } from "mobx";
import { TopicStore } from "./TopicStore";
import { ConsumeStore } from "./ConsumeStore";

export class AppStore {
    topicStore: TopicStore;
    consumeStore: ConsumeStore;

    constructor() {
        this.topicStore = new TopicStore();
        this.consumeStore = new ConsumeStore();
        makeAutoObservable(this);
    }
}

export const appStore = new AppStore();
