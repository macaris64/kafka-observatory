import { render, screen } from '@testing-library/react';
import { ClusterStatus } from './ClusterStatus';

describe('ClusterStatus', () => {
    let mockStore: any;

    beforeEach(() => {
        mockStore = {
            health: 'UP',
            clusterInfo: { clusterId: 'test-id', brokerCount: 3 },
            error: null,
            fetchHealth: jest.fn(),
            fetchClusterInfo: jest.fn()
        };
    });

    it('renders health and cluster info', () => {
        render(<ClusterStatus appStore={mockStore} />);
        expect(screen.getByText(/Connected/i)).toBeInTheDocument();
        expect(screen.getByText('test-id')).toBeInTheDocument();
        expect(screen.getByText(/3 Nodes/i)).toBeInTheDocument();
    });

    it('calls fetch methods on mount', () => {
        render(<ClusterStatus appStore={mockStore} />);
        expect(mockStore.fetchHealth).toHaveBeenCalled();
        expect(mockStore.fetchClusterInfo).toHaveBeenCalled();
    });
});
