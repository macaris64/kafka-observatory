import { makeAutoObservable } from "mobx";

export class TopicStore {
    constructor() {
        makeAutoObservable(this);
    }
}
