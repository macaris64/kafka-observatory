import { render, screen, act } from '@testing-library/react';
import { HomePage } from './HomePage';

describe('HomePage', () => {
    it('renders cluster status', async () => {
        await act(async () => {
            render(<HomePage />);
        });
        expect(screen.getByText(/System Overview/i)).toBeInTheDocument();
    });
});
