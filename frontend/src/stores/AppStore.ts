import { makeAutoObservable, runInAction, reaction } from "mobx";
import { TopicStore } from "./TopicStore";
import { ConsumeStore } from "./ConsumeStore";
import { ProduceStore } from "./ProduceStore";
import { apiClient } from "../services/apiClient";

export interface ClusterInfo {
    clusterId: string;
    controllerId: number;
    brokerCount: number;
}

export class AppStore {
    topicStore: TopicStore;
    consumeStore: ConsumeStore;
    produceStore: ProduceStore;
    health: string = "UNKNOWN";
    clusterInfo: ClusterInfo | null = null;
    error: string | null = null;

    constructor() {
        this.topicStore = new TopicStore();
        this.consumeStore = new ConsumeStore();
        this.produceStore = new ProduceStore();
        makeAutoObservable(this);

        // Sync selected topic to produce store
        reaction(
            () => this.topicStore.selectedTopicName,
            (name) => {
                if (name) this.produceStore.setTopic(name);
            }
        );
    }

    async fetchHealth() {
        try {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const data = await apiClient.get<any>("/health");
            runInAction(() => {
                this.health = data.status || "UP";
            });
        } catch (e) {
            runInAction(() => {
                console.log('Failed to fetch health: ', e)
                this.health = "DOWN";
                this.error = "Failed to fetch health";
            });
        }
    }

    async fetchClusterInfo() {
        try {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const data = await apiClient.get<any>("/cluster");
            runInAction(() => {
                this.clusterInfo = data;
            });
        } catch (e) {
            runInAction(() => {
                console.log('Failed to fetch cluster info: ', e)
                this.error = "Failed to fetch cluster info";
            });
        }
    }

    clearError() {
        this.error = null;
    }
}

export const appStore = new AppStore();
