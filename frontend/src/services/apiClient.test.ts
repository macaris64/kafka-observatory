import { apiClient } from './apiClient';

describe('apiClient', () => {
    beforeEach(() => {
        (window as any).fetch = jest.fn();
    });

    it('get should fetch and return data', async () => {
        const mockData = { data: 'test' };
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: true,
            json: async () => mockData,
        });

        const result = await apiClient.get('/test');
        expect(result).toBe('test');
        expect((window as any).fetch).toHaveBeenCalledWith('/api/test');
    });

    it('post should send body and return data', async () => {
        const mockData = { id: 1 };
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: true,
            json: async () => ({ data: mockData }),
        });

        const result = await apiClient.post('/test', { foo: 'bar' });
        expect(result).toEqual(mockData);
        expect((window as any).fetch).toHaveBeenCalledWith('/api/test', expect.objectContaining({
            method: 'POST',
            body: JSON.stringify({ foo: 'bar' })
        }));
    });

    it('delete should call delete and return data', async () => {
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: true,
            json: async () => ({ data: 'ok' }),
        });

        const result = await apiClient.delete('/test/1');
        expect(result).toBe('ok');
        expect((window as any).fetch).toHaveBeenCalledWith('/api/test/1', expect.objectContaining({
            method: 'DELETE'
        }));
    });

    it('should throw error on non-ok response', async () => {
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: false,
            statusText: 'Not Found',
        });

        await expect(apiClient.get('/error')).rejects.toThrow('API error: Not Found');
    });

    it('should throw error on post non-ok response', async () => {
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: false,
            statusText: 'Bad Request',
        });

        await expect(apiClient.post('/error', {})).rejects.toThrow('API error: Bad Request');
    });

    it('should throw error on delete non-ok response', async () => {
        ((window as any).fetch as jest.Mock).mockResolvedValue({
            ok: false,
            statusText: 'Forbidden',
        });

        await expect(apiClient.delete('/error/1')).rejects.toThrow('API error: Forbidden');
    });
});
