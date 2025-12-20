import { AppStore } from './AppStore';
import { apiClient } from '../services/apiClient';

jest.mock('../services/apiClient');

describe('AppStore', () => {
    let store: AppStore;

    beforeEach(() => {
        store = new AppStore();
        jest.clearAllMocks();
    });

    it('should initialize with default values', () => {
        expect(store.topicStore).toBeDefined();
        expect(store.consumeStore).toBeDefined();
        expect(store.health).toBe('UNKNOWN');
        expect(store.clusterInfo).toBeNull();
    });

    it('should fetch health status', async () => {
        (apiClient.get as jest.Mock).mockResolvedValue({ status: 'UP' });
        await store.fetchHealth();
        expect(store.health).toBe('UP');
    });

    it('should fetch cluster info', async () => {
        const mockCluster = { clusterId: 'test-cluster', brokerCount: 3 };
        (apiClient.get as jest.Mock).mockResolvedValue(mockCluster);
        await store.fetchClusterInfo();
        expect(store.clusterInfo).toEqual(mockCluster);
    });

    it('should set error on health fetch failure', async () => {
        (apiClient.get as jest.Mock).mockRejectedValue(new Error('Fetch failed'));
        await store.fetchHealth();
        expect(store.health).toBe('DOWN');
        expect(store.error).toBe('Failed to fetch health');
    });

    it('should clear error', () => {
        store.error = 'Some error';
        store.clearError();
        expect(store.error).toBeNull();
    });

    it('should fallback to UP if health status missing', async () => {
        (apiClient.get as jest.Mock).mockResolvedValue({});
        await store.fetchHealth();
        expect(store.health).toBe('UP');
    });
});
