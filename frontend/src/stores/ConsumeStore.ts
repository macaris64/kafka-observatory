import { makeAutoObservable, runInAction } from "mobx";
import { apiClient } from "../services/apiClient";
import { webSocketClient } from "../services/WebSocketClient";

export interface ConsumeMessage {
    topic: string;
    partition: number;
    offset: number;
    timestamp: number;
    key: string | null;
    value: string;
}

export type SessionState = "IDLE" | "RUNNING" | "PAUSED" | "STOPPED" | "ERROR";

export interface ConsumeSession {
    id: string;
    topic: string;
    state: SessionState;
}

export class ConsumeStore {
    activeSession: ConsumeSession | null = null;
    messages: ConsumeMessage[] = [];
    error: string | null = null;
    autoScroll: boolean = true;

    constructor() {
        makeAutoObservable(this);
    }

    async startSession(topic: string, from: "EARLIEST" | "LATEST") {
        this.error = null;
        this.messages = [];
        try {
            const session = await apiClient.post<ConsumeSession>("/consume-sessions", {
                topic,
                from,
                maxBufferSize: 500
            });
            runInAction(() => {
                this.activeSession = session;
                this.connectWebSocket(session.id);
            });
        } catch (e: any) {
            runInAction(() => {
                this.error = e.message || "Failed to start session";
            });
        }
    }

    private connectWebSocket(sessionId: string) {
        webSocketClient.connect(
            `/ws/consume-sessions/${sessionId}`,
            (message: ConsumeMessage) => {
                runInAction(() => {
                    this.messages.push(message);
                    if (this.messages.length > 1000) {
                        this.messages = this.messages.slice(-1000);
                    }
                });
            },
            (err) => {
                runInAction(() => {
                    this.error = "WebSocket error: " + (err.message || "Unknown error");
                    if (this.activeSession) this.activeSession.state = "ERROR";
                });
            },
            () => {
                runInAction(() => {
                    if (this.activeSession && this.activeSession.state === "RUNNING") {
                        this.activeSession.state = "STOPPED";
                    }
                });
            }
        );
    }

    async pauseSession() {
        if (!this.activeSession) return;
        try {
            const session = await apiClient.post<ConsumeSession>(`/consume-sessions/${this.activeSession.id}/pause`);
            runInAction(() => {
                this.activeSession = session;
            });
        } catch (e: any) {
            runInAction(() => {
                this.error = e.message || "Failed to pause session";
            });
        }
    }

    async resumeSession() {
        if (!this.activeSession) return;
        try {
            const session = await apiClient.post<ConsumeSession>(`/consume-sessions/${this.activeSession.id}/resume`);
            runInAction(() => {
                this.activeSession = session;
            });
        } catch (e: any) {
            runInAction(() => {
                this.error = e.message || "Failed to resume session";
            });
        }
    }

    async stopSession() {
        if (!this.activeSession) return;
        try {
            await apiClient.delete(`/consume-sessions/${this.activeSession.id}`);
            runInAction(() => {
                this.activeSession = null;
                webSocketClient.disconnect();
            });
        } catch (e: any) {
            runInAction(() => {
                this.error = e.message || "Failed to stop session";
            });
        }
    }

    toggleAutoScroll() {
        this.autoScroll = !this.autoScroll;
    }

    clearMessages() {
        this.messages = [];
    }

    get sessionState(): SessionState {
        return this.activeSession?.state || "IDLE";
    }
}
