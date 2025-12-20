import { apiClient } from './apiClient';

describe('apiClient', () => {
    it('should log GET requests (placeholder test)', async () => {
        const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
        await apiClient.get('/api/test');
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining('GET /api/test'));
        consoleSpy.mockRestore();
    });
});
