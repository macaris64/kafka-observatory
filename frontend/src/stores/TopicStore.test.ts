import { TopicStore } from './TopicStore';
import { apiClient } from '../services/apiClient';

jest.mock('../services/apiClient');

describe('TopicStore', () => {
    let store: TopicStore;

    beforeEach(() => {
        store = new TopicStore();
        jest.clearAllMocks();
    });

    it('should fetch topics successfully', async () => {
        const mockTopics = [{ name: 'topic-1', partitionCount: 3, replicationFactor: 1 }];
        (apiClient.get as jest.Mock).mockResolvedValue(mockTopics);

        await store.fetchTopics();

        expect(store.topics).toEqual(mockTopics);
        expect(store.loading).toBe(false);
    });

    it('should handle fetch topics error', async () => {
        (apiClient.get as jest.Mock).mockRejectedValue(new Error('Network error'));

        await store.fetchTopics();

        expect(store.error).toBe('Failed to fetch topics');
        expect(store.loading).toBe(false);
    });

    it('should select a topic', () => {
        const topicName = 'test-topic';
        store.selectTopic(topicName);
        expect(store.selectedTopicName).toBe(topicName);
    });

    it('should return null for non-existent selected topic', () => {
        store.topics = [{ name: 't1', partitionCount: 1, replicationFactor: 1 }];
        store.selectTopic('t2');
        expect(store.selectedTopic).toBeNull();
    });
});
