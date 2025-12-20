import { render, screen } from '@testing-library/react';
import App from './App';

describe('App', () => {
    it('renders without crashing and shows home page', () => {
        render(<App />);
        // Check for the header specifically to avoid ambiguity with the welcome text
        expect(screen.getByRole('heading', { name: /Kafka Observatory UI/i })).toBeInTheDocument();
        expect(screen.getByText(/Welcome to the Kafka Observatory UI/i)).toBeInTheDocument();
    });
});
