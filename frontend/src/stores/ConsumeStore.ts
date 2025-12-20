import { makeAutoObservable } from "mobx";

export class ConsumeStore {
    constructor() {
        makeAutoObservable(this);
    }
}
