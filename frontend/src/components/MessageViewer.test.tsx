import { render, screen } from '@testing-library/react';
import { MessageViewer } from './MessageViewer';

describe('MessageViewer', () => {
    let mockStore: any;

    beforeEach(() => {
        mockStore = {
            messages: [
                { partition: 0, offset: 1, timestamp: Date.now(), value: 'msg-1' },
                { partition: 1, offset: 2, timestamp: Date.now(), value: 'msg-2' }
            ],
            autoScroll: true
        };
    });

    it('renders message list', () => {
        render(<MessageViewer consumeStore={mockStore} />);
        expect(screen.getByText(/msg-1/i)).toBeInTheDocument();
        expect(screen.getByText(/msg-2/i)).toBeInTheDocument();
        expect(screen.getByText(/P:0 O:1/i)).toBeInTheDocument();
    });

    it('shows waiting message when empty', () => {
        mockStore.messages = [];
        render(<MessageViewer consumeStore={mockStore} />);
        expect(screen.getByText(/Waiting for Kafka stream to initialize/i)).toBeInTheDocument();
    });
});
