import { getEnvVar } from "./env";

const BASE_URL = getEnvVar("VITE_API_BASE_URL", "");

export const apiClient = {
    get: async <T>(url: string): Promise<T> => {
        const response = await fetch(`${BASE_URL}/api${url}`);
        if (!response.ok) {
            throw new Error(`API error: ${response.statusText}`);
        }
        const json = await response.json();
        return json.data ?? json;
    },
    post: async <T>(url: string, body?: any): Promise<T> => {
        const response = await fetch(`${BASE_URL}/api${url}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: body ? JSON.stringify(body) : undefined,
        });
        if (!response.ok) {
            throw new Error(`API error: ${response.statusText}`);
        }
        const json = await response.json();
        return json.data ?? json;
    },
    delete: async <T>(url: string): Promise<T> => {
        const response = await fetch(`${BASE_URL}/api${url}`, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`API error: ${response.statusText}`);
        }
        const json = await response.json();
        return json.data ?? json;
    }
};
