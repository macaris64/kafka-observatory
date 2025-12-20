import { ConsumeStore, ConsumeMessage } from './ConsumeStore';
import { apiClient } from '../services/apiClient';
import { webSocketClient } from '../services/WebSocketClient';
import { runInAction } from 'mobx';

jest.mock('../services/apiClient');
jest.mock('../services/WebSocketClient');

describe('ConsumeStore', () => {
    let store: ConsumeStore;

    beforeEach(() => {
        store = new ConsumeStore();
        jest.clearAllMocks();
    });

    it('should start session and connect websocket', async () => {
        const mockSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };
        (apiClient.post as jest.Mock).mockResolvedValue(mockSession);

        await store.startSession('test-topic', 'LATEST');

        expect(store.activeSession).toEqual(mockSession);
        expect(webSocketClient.connect).toHaveBeenCalledWith(
            '/ws/consume-sessions/session-1',
            expect.any(Function),
            expect.any(Function),
            expect.any(Function)
        );
    });

    it('should handle incoming messages via websocket callback', async () => {
        const mockSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };
        (apiClient.post as jest.Mock).mockResolvedValue(mockSession);

        await store.startSession('test-topic', 'LATEST');

        const onMessage = (webSocketClient.connect as jest.Mock).mock.calls[0][1];
        const mockMsg: ConsumeMessage = {
            topic: 'test-topic', partition: 0, offset: 1, timestamp: Date.now(),
            key: null, value: 'hello'
        };

        runInAction(() => onMessage(mockMsg));
        expect(store.messages.length).toBe(1);
        expect(store.messages[0]).toEqual(mockMsg);
    });

    it('should handle websocket error', async () => {
        const mockSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };
        (apiClient.post as jest.Mock).mockResolvedValue(mockSession);
        await store.startSession('test-topic', 'LATEST');

        const onError = (webSocketClient.connect as jest.Mock).mock.calls[0][2];
        runInAction(() => onError({ message: 'WS Error' }));

        expect(store.error).toBe('WebSocket error: WS Error');
        expect(store.activeSession?.state).toBe('ERROR');
    });

    it('should handle websocket close', async () => {
        const mockSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };
        (apiClient.post as jest.Mock).mockResolvedValue(mockSession);
        await store.startSession('test-topic', 'LATEST');

        const onClose = (webSocketClient.connect as jest.Mock).mock.calls[0][3];
        runInAction(() => onClose());

        expect(store.activeSession?.state).toBe('STOPPED');
    });

    it('should pause and resume session', async () => {
        store.activeSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };

        (apiClient.post as jest.Mock).mockResolvedValue({ id: 'session-1', topic: 'test-topic', state: 'PAUSED' });
        await store.pauseSession();
        expect(store.activeSession?.state).toBe('PAUSED');

        (apiClient.post as jest.Mock).mockResolvedValue({ id: 'session-1', topic: 'test-topic', state: 'RUNNING' });
        await store.resumeSession();
        expect(store.activeSession?.state).toBe('RUNNING');
    });

    it('should stop session and disconnect websocket', async () => {
        store.activeSession = { id: 'session-1', topic: 'test-topic', state: 'RUNNING' };
        (apiClient.delete as jest.Mock).mockResolvedValue({});

        await store.stopSession();

        expect(store.activeSession).toBeNull();
        expect(webSocketClient.disconnect).toHaveBeenCalled();
    });

    it('should toggle auto-scroll and clear messages', () => {
        store.messages = [{ topic: 't', partition: 0, offset: 1, timestamp: 1, key: null, value: 'v' }];
        expect(store.autoScroll).toBe(true);

        store.toggleAutoScroll();
        expect(store.autoScroll).toBe(false);

        store.clearMessages();
        expect(store.messages.length).toBe(0);
    });

    it('should do nothing if pause/resume/stop called without active session', async () => {
        store.activeSession = null;
        await store.pauseSession();
        await store.resumeSession();
        await store.stopSession();
        expect(apiClient.post).not.toHaveBeenCalled();
        expect(apiClient.delete).not.toHaveBeenCalled();
    });

    it('should cap messages at 1000', async () => {
        const mockSession = { id: 's1', topic: 't', state: 'RUNNING' };
        (apiClient.post as jest.Mock).mockResolvedValue(mockSession);
        await store.startSession('t', 'LATEST');

        const onMsg = (webSocketClient.connect as jest.Mock).mock.calls[0][1];

        runInAction(() => {
            for (let i = 0; i < 1100; i++) {
                onMsg({ topic: 't', partition: 0, offset: i, timestamp: i, key: null, value: 'v' });
            }
        });
        expect(store.messages.length).toBe(1000);
        expect(store.messages[0].offset).toBe(100);
    });

    it('should handle start session failure', async () => {
        (apiClient.post as jest.Mock).mockRejectedValue(new Error('Start failed'));
        await store.startSession('t', 'LATEST');
        expect(store.error).toBe('Start failed');
    });
});
