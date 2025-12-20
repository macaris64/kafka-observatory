import { render, screen } from '@testing-library/react';
import { AppLayout } from './AppLayout';

describe('AppLayout', () => {
    it('renders children and title', () => {
        render(
            <AppLayout>
                <div data-testid="child">Child Content</div>
            </AppLayout>
        );
        expect(screen.getByRole('heading', { name: /Kafka Observatory/i })).toBeInTheDocument();
        expect(screen.getByTestId('child')).toBeInTheDocument();
    });
});
