import { WebSocketClient } from './WebSocketClient';

describe('WebSocketClient', () => {
    let client: WebSocketClient;
    let mockWs: any;

    beforeEach(() => {
        client = new WebSocketClient();
        mockWs = {
            close: jest.fn(),
            send: jest.fn(),
        };
        (window as any).WebSocket = jest.fn(() => mockWs);
    });

    it('should connect to websocket', () => {
        const onMsg = jest.fn();
        client.connect('/ws/test', onMsg, jest.fn(), jest.fn());
        expect((window as any).WebSocket).toHaveBeenCalledWith(expect.stringContaining('/ws/test'));
    });

    it('should handle incoming messages', () => {
        const onMsg = jest.fn();
        client.connect('/ws/test', onMsg, jest.fn(), jest.fn());

        mockWs.onmessage({ data: JSON.stringify({ foo: 'bar' }) });
        expect(onMsg).toHaveBeenCalledWith({ foo: 'bar' });
    });

    it('should disconnect and close websocket', () => {
        client.connect('/ws/test', jest.fn(), jest.fn(), jest.fn());
        client.disconnect();
        expect(mockWs.close).toHaveBeenCalled();
    });

    it('should handle errors', () => {
        const onError = jest.fn();
        client.connect('/ws/test', jest.fn(), onError, jest.fn());

        mockWs.onerror(new Error('WS Error'));
        expect(onError).toHaveBeenCalled();
    });

    it('should not reconnect if already connected', () => {
        client.connect('/ws/1', jest.fn(), jest.fn(), jest.fn());
        client.connect('/ws/2', jest.fn(), jest.fn(), jest.fn());
        expect((window as any).WebSocket).toHaveBeenCalledTimes(1);
    });

    it('should not disconnect if not connected', () => {
        client.disconnect();
        expect(mockWs.close).not.toHaveBeenCalled();
    });
});
