import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { AppStore } from '../stores/AppStore';

interface ClusterStatusProps {
    appStore: AppStore;
}

export const ClusterStatus: React.FC<ClusterStatusProps> = observer(({ appStore }) => {
    useEffect(() => {
        appStore.fetchHealth();
        appStore.fetchClusterInfo();
        const interval = setInterval(() => {
            appStore.fetchHealth();
        }, 30000);
        return () => clearInterval(interval);
    }, [appStore]);

    const { health, clusterInfo, error } = appStore;

    return (
        <div className="card" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h3 style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.9rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>System Overview</h3>
                <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem',
                    fontSize: '0.8rem',
                    padding: '4px 10px',
                    borderRadius: '20px',
                    background: health === 'UP' ? 'rgba(63, 185, 80, 0.1)' : 'rgba(248, 81, 73, 0.1)',
                    color: health === 'UP' ? 'var(--success-color)' : 'var(--error-color)',
                    border: `1px solid ${health === 'UP' ? 'rgba(63, 185, 80, 0.2)' : 'rgba(248, 81, 73, 0.2)'}`
                }}>
                    <div style={{
                        width: '8px',
                        height: '8px',
                        borderRadius: '50%',
                        background: health === 'UP' ? 'var(--success-color)' : 'var(--error-color)',
                        boxShadow: `0 0 8px ${health === 'UP' ? 'var(--success-color)' : 'var(--error-color)'}`
                    }} />
                    {health === 'UP' ? 'All Systems Operational' : 'System Issues Detected'}
                </div>
            </div>

            {error && (
                <div style={{
                    padding: '1rem',
                    background: 'rgba(248, 81, 73, 0.1)',
                    border: '1px solid var(--error-color)',
                    borderRadius: '8px',
                    color: 'var(--error-color)',
                    fontSize: '0.9rem',
                    marginBottom: '1rem'
                }}>
                    {error}
                </div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '2rem' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>Kafka Cluster ID</span>
                    <span style={{ fontSize: '1.1rem', fontWeight: 500, fontFamily: 'JetBrains Mono, monospace' }}>{clusterInfo?.clusterId || '—'}</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>Active Brokers</span>
                    <span style={{ fontSize: '1.1rem', fontWeight: 500 }}>{clusterInfo?.brokerCount ?? '0'} Nodes</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem' }}>API Backend</span>
                    <span style={{ fontSize: '1.1rem', fontWeight: 500 }}>{health === 'UP' ? 'Connected' : 'Disconnected'}</span>
                </div>
            </div>
        </div>
    );
});
