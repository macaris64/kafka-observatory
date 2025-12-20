export const getEnvVar = (name: string, defaultValue: string): string => {
    try {
        // Access import.meta.env dynamically to avoid syntax errors in CommonJS/Jest
        const metaEnv = new Function('return import.meta.env')();
        return metaEnv[name] || defaultValue;
    } catch {
        // Fallback for Jest or other environments
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        return (globalThis as any)[name] || (globalThis as any).process?.env?.[name] || defaultValue;
    }
};
