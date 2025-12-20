import { AppStore } from './AppStore';

describe('AppStore', () => {
    it('should initialize with topicStore and consumeStore', () => {
        const store = new AppStore();
        expect(store.topicStore).toBeDefined();
        expect(store.consumeStore).toBeDefined();
    });
});
