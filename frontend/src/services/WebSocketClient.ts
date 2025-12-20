export class WebSocketClient {
    connect(url: string) {
        console.log(`Connecting to ${url}... (Placeholder)`);
    }

    disconnect() {
        console.log("Disconnecting... (Placeholder)");
    }
}

export const webSocketClient = new WebSocketClient();
