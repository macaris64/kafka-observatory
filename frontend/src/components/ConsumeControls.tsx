import React, {type SetStateAction, useState} from 'react';
import { observer } from 'mobx-react-lite';
import { ConsumeStore } from '../stores/ConsumeStore';

interface ConsumeControlsProps {
    consumeStore: ConsumeStore;
    topicName: string | null;
}

export const ConsumeControls: React.FC<ConsumeControlsProps> = observer(({ consumeStore, topicName }) => {
    const [from, setFrom] = useState<'EARLIEST' | 'LATEST'>('LATEST');

    const handleStart = () => {
        if (topicName) {
            consumeStore.startSession(topicName, from);
        }
    };

    const { activeSession, sessionState } = consumeStore;

    return (
        <div className="card" style={{ marginBottom: '2rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h3 style={{ margin: 0, fontSize: '1.1rem' }}>
                    Control Center {activeSession && <span style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', fontWeight: 400 }}>— Session ID: {activeSession.id}</span>}
                </h3>
                {activeSession && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                        <button onClick={() => consumeStore.toggleAutoScroll()} style={{ padding: '4px 12px', fontSize: '0.75rem' }}>
                            {consumeStore.autoScroll ? '● Auto-scroll ON' : '○ Auto-scroll OFF'}
                        </button>
                        <button onClick={() => consumeStore.clearMessages()} style={{ padding: '4px 12px', fontSize: '0.75rem' }}>
                            Clear
                        </button>
                    </div>
                )}
            </div>

            {!activeSession ? (
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
                        <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Consumer Offset</label>
                        <select
                            value={from}
                            onChange={(e) => setFrom(e.target.value as SetStateAction<"EARLIEST" | "LATEST">)}
                            style={{ width: '100%' }}
                        >
                            <option value="LATEST">Latest (Live Stream)</option>
                            <option value="EARLIEST">Earliest (From Beginning)</option>
                        </select>
                    </div>
                    <button
                        onClick={handleStart}
                        disabled={!topicName}
                        className="primary"
                        style={{ height: '42px', padding: '0 2rem' }}
                    >
                        Start Consumption
                    </button>
                </div>
            ) : (
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1 }}>
                        <div style={{
                            padding: '6px 12px',
                            borderRadius: '6px',
                            background: '#21262d',
                            border: '1px solid var(--panel-border)',
                            fontSize: '0.9rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px'
                        }}>
                            <span style={{
                                width: '6px',
                                height: '6px',
                                background: sessionState === 'RUNNING' ? 'var(--success-color)' : 'var(--warning-color)',
                                borderRadius: '50%',
                                boxShadow: `0 0 8px ${sessionState === 'RUNNING' ? 'var(--success-color)' : 'var(--warning-color)'}`
                            }} />
                            {sessionState}
                        </div>
                        <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Streaming from <strong>{topicName}</strong></span>
                    </div>

                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {sessionState === 'RUNNING' && (
                            <button onClick={() => consumeStore.pauseSession()}>Pause Session</button>
                        )}
                        {sessionState === 'PAUSED' && (
                            <button onClick={() => consumeStore.resumeSession()} className="primary">Resume Session</button>
                        )}
                        <button onClick={() => consumeStore.stopSession()} className="danger">
                            Stop Session
                        </button>
                    </div>
                </div>
            )}

            {consumeStore.error && (
                <div style={{
                    marginTop: '1rem',
                    padding: '0.75rem',
                    background: 'rgba(248, 81, 73, 0.1)',
                    border: '1px solid var(--error-color)',
                    borderRadius: '6px',
                    color: 'var(--error-color)',
                    fontSize: '0.85rem'
                }}>
                    {consumeStore.error}
                </div>
            )}
        </div>
    );
});
