import React, { useEffect, useRef } from 'react';
import { observer } from 'mobx-react-lite';
import { ConsumeStore } from '../stores/ConsumeStore';

interface MessageViewerProps {
    consumeStore: ConsumeStore;
}

export const MessageViewer: React.FC<MessageViewerProps> = observer(({ consumeStore }) => {
    const scrollRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (consumeStore.autoScroll && scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [consumeStore.messages.length, consumeStore.autoScroll]);

    return (
        <div className="card" style={{ display: 'flex', flexDirection: 'column', height: '600px', padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '1rem 1.5rem', borderBottom: '1px solid var(--panel-border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255,255,255,0.02)' }}>
                <h3 style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Live Message Stream</h3>
                <div style={{ fontSize: '0.8rem', color: 'var(--accent-color)', fontWeight: 600 }}>
                    {consumeStore.messages.length} Messages
                </div>
            </div>

            <div
                ref={scrollRef}
                className="code-block"
                style={{
                    flex: 1,
                    margin: '1rem',
                    overflowY: 'auto',
                    border: 'none',
                    background: '#010409',
                    borderRadius: '8px'
                }}
            >
                {consumeStore.messages.length === 0 && (
                    <div style={{ color: 'var(--text-secondary)', textAlign: 'center', marginTop: '4rem', fontSize: '0.9rem' }}>
                        <div style={{ fontSize: '2rem', marginBottom: '1rem', opacity: 0.2 }}>📡</div>
                        Waiting for Kafka stream to initialize...
                    </div>
                )}
                {consumeStore.messages.map((msg, idx) => (
                    <div
                        key={`${msg.offset}-${idx}`}
                        style={{
                            padding: '4px 0',
                            borderBottom: '1px solid rgba(48, 54, 61, 0.3)',
                            whiteSpace: 'pre-wrap',
                            wordBreak: 'break-all',
                            display: 'flex',
                            gap: '1rem',
                            fontSize: '0.85rem'
                        }}
                    >
                        <span style={{ color: 'var(--text-secondary)', minWidth: '160px', userSelect: 'none', fontSize: '0.8rem' }}>
                            <span style={{ color: 'var(--accent-color)' }}>{new Date(msg.timestamp).toLocaleTimeString()}</span>
                            <span style={{ opacity: 0.5 }}> | </span>
                            P:{msg.partition} O:{msg.offset}
                        </span>
                        <span style={{ color: '#d2a8ff' }}>{msg.value}</span>
                    </div>
                ))}
            </div>
        </div>
    );
});
