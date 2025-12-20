import { getEnvVar } from "./env";

export class WebSocketClient {
    private ws: WebSocket | null = null;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    connect(url: string, onMessage: (data: any) => void, onError: (err: any) => void, onClose: () => void) {
        if (this.ws) {
            return;
        }

        const apiBase = getEnvVar("VITE_API_BASE_URL", "");
        let fullUrl: string;

        if (apiBase) {
            const wsProtocol = apiBase.startsWith("https") ? "wss:" : "ws:";
            const wsHost = apiBase.replace(/^https?:\/\//, "");
            fullUrl = `${wsProtocol}//${wsHost}${url}`;
        } else {
            const isSecure = window.location.protocol === 'https:';
            const wsProtocol = isSecure ? 'wss:' : 'ws:';
            const wsHost = window.location.host;
            fullUrl = `${wsProtocol}//${wsHost}${url}`;
        }
        this.ws = new WebSocket(fullUrl);

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                onMessage(data);
            } catch (e) {
                console.error("Failed to parse WS message", e);
            }
        };

        this.ws.onerror = (error) => {
            onError(error);
        };

        this.ws.onclose = () => {
            onClose();
            this.ws = null;
        };
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
}

export const webSocketClient = new WebSocketClient();
