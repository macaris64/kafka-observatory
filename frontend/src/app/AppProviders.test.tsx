import { render, screen } from '@testing-library/react';
import { AppProviders, useStores } from './AppProviders';

const TestComponent = () => {
    const store = useStores();
    return <div data-testid="store-status">{store ? 'Store Ready' : 'No Store'}</div>;
};

const ErrorComponent = () => {
    try {
        useStores();
        return null;
    } catch (e: any) {
        return <div data-testid="error-message">{e.message}</div>;
    }
};

describe('AppProviders', () => {
    it('provides the appStore to children', () => {
        render(
            <AppProviders>
                <TestComponent />
            </AppProviders>
        );
        expect(screen.getByTestId('store-status')).toHaveTextContent('Store Ready');
    });

    it('throws error when useStores is used outside of AppProviders', () => {
        render(<ErrorComponent />);
        expect(screen.getByTestId('error-message')).toHaveTextContent('useStores must be used within a StoreProvider');
    });
});
