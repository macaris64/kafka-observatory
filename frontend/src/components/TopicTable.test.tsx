import { render, screen, fireEvent } from '@testing-library/react';
import { TopicTable } from './TopicTable';

jest.mock('../stores/TopicStore');

describe('TopicTable', () => {
    let mockStore: any;

    beforeEach(() => {
        mockStore = {
            topics: [
                { name: 'topic-1', partitionCount: 1, replicationFactor: 1 },
                { name: 'topic-2', partitionCount: 3, replicationFactor: 2 }
            ],
            loading: false,
            error: null,
            selectedTopicName: null,
            fetchTopics: jest.fn(),
            selectTopic: jest.fn()
        };
    });

    it('renders topic list', () => {
        render(<TopicTable topicStore={mockStore} />);
        expect(screen.getByText('topic-1')).toBeInTheDocument();
        expect(screen.getByText('topic-2')).toBeInTheDocument();
    });

    it('calls fetchTopics on mount', () => {
        render(<TopicTable topicStore={mockStore} />);
        expect(mockStore.fetchTopics).toHaveBeenCalled();
    });

    it('calls selectTopic when button clicked', () => {
        render(<TopicTable topicStore={mockStore} />);
        const selectButtons = screen.getAllByText('Select Topic');
        fireEvent.click(selectButtons[0]);
        expect(mockStore.selectTopic).toHaveBeenCalledWith('topic-1');
    });

    it('shows loading state', () => {
        mockStore.loading = true;
        render(<TopicTable topicStore={mockStore} />);
        expect(screen.getByText(/Loading topics.../i)).toBeInTheDocument();
    });
});
