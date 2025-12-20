import { render, screen, fireEvent, act } from '@testing-library/react';
import { ConsumeControls } from './ConsumeControls';
import { runInAction, makeAutoObservable } from 'mobx';

describe('ConsumeControls', () => {
    let mockStore: any;

    beforeEach(() => {
        mockStore = makeAutoObservable({
            activeSession: null,
            sessionState: 'IDLE',
            error: null,
            autoScroll: true,
            startSession: jest.fn(),
            pauseSession: jest.fn(),
            resumeSession: jest.fn(),
            stopSession: jest.fn(),
            toggleAutoScroll: jest.fn(),
            clearMessages: jest.fn()
        }, {
            startSession: false,
            pauseSession: false,
            resumeSession: false,
            stopSession: false,
            toggleAutoScroll: false,
            clearMessages: false
        });
    });

    it('renders start session controls when no active session', () => {
        render(<ConsumeControls consumeStore={mockStore} topicName="test-topic" />);
        expect(screen.getByText(/Start Consumption/i)).toBeInTheDocument();
    });

    it('calls startSession on button click', () => {
        render(<ConsumeControls consumeStore={mockStore} topicName="test-topic" />);
        fireEvent.click(screen.getByText(/Start Consumption/i));
        expect(mockStore.startSession).toHaveBeenCalledWith('test-topic', 'LATEST');
    });

    it('renders runtime controls when session is active', () => {
        mockStore.activeSession = { id: 's1' };
        mockStore.sessionState = 'RUNNING';
        render(<ConsumeControls consumeStore={mockStore} topicName="test-topic" />);
        expect(screen.getByText(/Stop Session/i)).toBeInTheDocument();
        expect(screen.getByText(/Pause/i)).toBeInTheDocument();
    });

    it('calls pause/resume/stop methods', () => {
        mockStore.activeSession = { id: 's1' };
        mockStore.sessionState = 'RUNNING';
        const { rerender } = render(<ConsumeControls consumeStore={mockStore} topicName="test-topic" />);

        fireEvent.click(screen.getByText(/Pause/i));
        expect(mockStore.pauseSession).toHaveBeenCalled();

        act(() => {
            runInAction(() => {
                mockStore.sessionState = 'PAUSED';
            });
        });
        rerender(<ConsumeControls consumeStore={mockStore} topicName="test-topic" />);
        fireEvent.click(screen.getByText(/Resume/i));
        expect(mockStore.resumeSession).toHaveBeenCalled();

        fireEvent.click(screen.getByText(/Stop Session/i));
        expect(mockStore.stopSession).toHaveBeenCalled();
    });
});
