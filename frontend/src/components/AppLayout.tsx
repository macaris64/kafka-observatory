import React from "react";

export const AppLayout: React.FC<{ children: React.ReactNode; title?: string }> = ({ children, title }) => {
    return (
        <div className="app-container" style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <header style={{
                padding: '1.5rem 2rem',
                borderBottom: '1px solid var(--panel-border)',
                background: 'rgba(13, 17, 23, 0.8)',
                backdropFilter: 'blur(10px)',
                position: 'sticky',
                top: 0,
                zIndex: 100,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <div style={{
                        width: '32px',
                        height: '32px',
                        background: 'linear-gradient(135deg, #58a6ff, #1f6feb)',
                        borderRadius: '8px',
                        boxShadow: '0 0 15px var(--accent-glow)'
                    }} />
                    <h1 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 600 }}>{title || "Kafka Observatory"}</h1>
                </div>
                <nav style={{ display: 'flex', gap: '1.5rem', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                    <span style={{ cursor: 'pointer', color: 'var(--text-primary)' }}>Dashboard</span>
                    <span style={{ cursor: 'pointer' }}>Settings</span>
                </nav>
            </header>
            <main style={{ flex: 1, padding: '2rem', maxWidth: '1400px', margin: '0 auto', width: '100%', boxSizing: 'border-box' }}>
                {children}
            </main>
            <footer style={{ padding: '1rem 2rem', borderTop: '1px solid var(--panel-border)', textAlign: 'center', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                © 2025 Kafka Observatory • Built with Precision
            </footer>
        </div>
    );
};
