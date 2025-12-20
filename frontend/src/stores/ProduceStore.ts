import { makeAutoObservable, runInAction } from "mobx";
import { apiClient } from "../services/apiClient";

export interface ProduceResult {
    topic: string;
    partition: number;
    offset: number;
}

export interface Header {
    key: string;
    value: string;
}

export type ProduceState = "IDLE" | "LOADING" | "SUCCESS" | "ERROR";

export class ProduceStore {
    topic: string = "";
    key: string = "";
    value: string = "";
    partition: number | undefined = undefined;
    headers: Header[] = [];

    state: ProduceState = "IDLE";
    result: ProduceResult | null = null;
    error: string | null = null;
    validationErrors: Record<string, string> = {};

    constructor() {
        makeAutoObservable(this);
    }

    setTopic(topic: string) {
        this.topic = topic;
    }

    setKey(key: string) {
        this.key = key;
    }

    setValue(value: string) {
        this.value = value;
    }

    setPartition(partition: number | undefined) {
        this.partition = partition;
    }

    addHeader() {
        this.headers.push({ key: "", value: "" });
    }

    updateHeader(index: number, field: "key" | "value", value: string) {
        if (this.headers[index]) {
            this.headers[index][field] = value;
        }
    }

    removeHeader(index: number) {
        this.headers.splice(index, 1);
    }

    validate(): boolean {
        const errors: Record<string, string> = {};
        if (!this.topic.trim()) {
            errors.topic = "Topic is required";
        }
        if (!this.value.trim()) {
            errors.value = "Value is required";
        }
        this.validationErrors = errors;
        return Object.keys(errors).length === 0;
    }

    async produce() {
        if (!this.validate()) return;

        this.state = "LOADING";
        this.error = null;
        this.result = null;

        try {
            const headersObj: Record<string, string> = {};
            this.headers.forEach(h => {
                if (h.key.trim()) {
                    headersObj[h.key] = h.value;
                }
            });

            const response = await apiClient.post<ProduceResult>("/produce", {
                topic: this.topic,
                key: this.key || null,
                value: this.value,
                partition: this.partition,
                headers: Object.keys(headersObj).length > 0 ? headersObj : undefined
            });

            runInAction(() => {
                this.result = response;
                this.state = "SUCCESS";
                this.value = ""; // Clear value on success
            });
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
        } catch (e: any) {
            runInAction(() => {
                this.error = e.message || "Failed to produce message";
                this.state = "ERROR";
            });
        }
    }

    reset() {
        this.state = "IDLE";
        this.result = null;
        this.error = null;
        this.validationErrors = {};
    }
}
