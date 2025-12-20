import { makeAutoObservable, runInAction } from "mobx";
import { apiClient } from "../services/apiClient";

export interface TopicInfo {
    name: string;
    partitionCount: number;
    replicationFactor: number;
}

export class TopicStore {
    topics: TopicInfo[] = [];
    selectedTopicName: string | null = null;
    loading: boolean = false;
    error: string | null = null;

    constructor() {
        makeAutoObservable(this);
    }

    async fetchTopics() {
        this.loading = true;
        this.error = null;
        try {
            const data = await apiClient.get<TopicInfo[]>("/topics");
            runInAction(() => {
                this.topics = data;
                this.loading = false;
            });
        } catch (e) {
            runInAction(() => {
                console.log('Failed to fetch topics: ', e)
                this.error = "Failed to fetch topics";
                this.loading = false;
            });
        }
    }

    selectTopic(name: string) {
        this.selectedTopicName = name;
    }

    get selectedTopic() {
        return this.topics.find(t => t.name === this.selectedTopicName) || null;
    }
}
