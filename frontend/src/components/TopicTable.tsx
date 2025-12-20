import React, { useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { TopicStore } from '../stores/TopicStore';

interface TopicTableProps {
    topicStore: TopicStore;
}

export const TopicTable: React.FC<TopicTableProps> = observer(({ topicStore }) => {
    useEffect(() => {
        topicStore.fetchTopics();
    }, [topicStore]);

    if (topicStore.loading) return <div>Loading topics...</div>;
    if (topicStore.error) return <div style={{ color: 'red' }}>{topicStore.error}</div>;

    return (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
            <div style={{ padding: '1.5rem 1.5rem 1rem 1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3 style={{ margin: 0, fontSize: '1.1rem' }}>Available Topics</h3>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>{topicStore.topics.length} Total</span>
            </div>

            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                    <tr>
                        <th>Topic Name</th>
                        <th>Partitions</th>
                        <th>Replication</th>
                        <th style={{ textAlign: 'right' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {topicStore.topics.map(topic => (
                        <tr
                            key={topic.name}
                            style={{
                                background: topicStore.selectedTopicName === topic.name ? 'rgba(88, 166, 255, 0.05)' : 'transparent',
                                transition: 'background 0.2s ease'
                            }}
                        >
                            <td style={{ fontWeight: 500 }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                    <div style={{ width: '6px', height: '6px', background: 'var(--accent-color)', borderRadius: '50%' }} />
                                    {topic.name}
                                </div>
                            </td>
                            <td>
                                <span style={{
                                    padding: '2px 8px',
                                    background: '#21262d',
                                    borderRadius: '4px',
                                    fontSize: '0.85rem',
                                    border: '1px solid var(--panel-border)'
                                }}>{topic.partitionCount}</span>
                            </td>
                            <td>{topic.replicationFactor}x</td>
                            <td style={{ textAlign: 'right' }}>
                                <button
                                    onClick={() => topicStore.selectTopic(topic.name)}
                                    className={topicStore.selectedTopicName === topic.name ? "primary" : ""}
                                    style={{ padding: '4px 12px', fontSize: '0.8rem' }}
                                >
                                    {topicStore.selectedTopicName === topic.name ? "Selected" : "Select Topic"}
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
});
