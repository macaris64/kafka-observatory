import { render, screen, act } from '@testing-library/react';
import App from './App';

describe('App', () => {
    it('renders without crashing and shows cluster status', async () => {
        await act(async () => {
            render(<App />);
        });
        // Check for the header specifically to avoid ambiguity
        expect(screen.getByRole('heading', { name: /Kafka Observatory/i })).toBeInTheDocument();
        expect(screen.getByText(/System Overview/i)).toBeInTheDocument();
    });
});
