import React from 'react';
import { observer } from 'mobx-react-lite';
import { ProduceStore } from '../stores/ProduceStore';

interface ProducerPanelProps {
    produceStore: ProduceStore;
}

export const ProducerPanel: React.FC<ProducerPanelProps> = observer(({ produceStore }) => {
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        produceStore.produce();
    };

    return (
        <div className="card">
            <h2 style={{ marginTop: 0, marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span style={{ fontSize: '1.5rem' }}>✉️</span> Produce Message
            </h2>

            {produceStore.state === "SUCCESS" && produceStore.result && (
                <div className="success-banner">
                    <strong>Success!</strong> Message produced to
                    <code>{produceStore.result.topic}</code>,
                    partition <code>{produceStore.result.partition}</code>,
                    offset <code>{produceStore.result.offset}</code>.
                    <button
                        style={{ marginLeft: '1rem', padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => produceStore.reset()}
                    >
                        Dismiss
                    </button>
                </div>
            )}

            {produceStore.state === "ERROR" && produceStore.error && (
                <div className="error-banner">
                    <strong>Error:</strong> {produceStore.error}
                    <button
                        style={{ marginLeft: '1rem', padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => produceStore.reset()}
                    >
                        Dismiss
                    </button>
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="topic">Topic</label>
                    <input
                        id="topic"
                        type="text"
                        value={produceStore.topic}
                        onChange={(e) => produceStore.setTopic(e.target.value)}
                        placeholder="Target Topic"
                        style={{ width: '100%', boxSizing: 'border-box' }}
                    />
                    {produceStore.validationErrors.topic && (
                        <div className="error-text">{produceStore.validationErrors.topic}</div>
                    )}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                    <div className="form-group">
                        <label htmlFor="key">Key (Optional)</label>
                        <input
                            id="key"
                            type="text"
                            value={produceStore.key}
                            onChange={(e) => produceStore.setKey(e.target.value)}
                            placeholder="Message Key"
                            style={{ width: '100%', boxSizing: 'border-box' }}
                        />
                    </div>
                    <div className="form-group">
                        <label htmlFor="partition">Partition (Optional)</label>
                        <input
                            id="partition"
                            type="number"
                            value={produceStore.partition ?? ''}
                            onChange={(e) => produceStore.setPartition(e.target.value ? parseInt(e.target.value) : undefined)}
                            placeholder="Auto"
                            style={{ width: '100%', boxSizing: 'border-box' }}
                        />
                    </div>
                </div>

                <div className="form-group">
                    <label htmlFor="value">Value (Required)</label>
                    <textarea
                        id="value"
                        value={produceStore.value}
                        onChange={(e) => produceStore.setValue(e.target.value)}
                        placeholder="Message content..."
                    />
                    {produceStore.validationErrors.value && (
                        <div className="error-text">{produceStore.validationErrors.value}</div>
                    )}
                </div>

                <div className="form-group">
                    <label style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        Headers (Optional)
                        <button
                            type="button"
                            onClick={() => produceStore.addHeader()}
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                        >
                            + Add Header
                        </button>
                    </label>
                    {produceStore.headers.map((header, index) => (
                        <div key={index} style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.5rem' }}>
                            <input
                                type="text"
                                value={header.key}
                                onChange={(e) => produceStore.updateHeader(index, 'key', e.target.value)}
                                placeholder="Key"
                                style={{ flex: 1 }}
                            />
                            <input
                                type="text"
                                value={header.value}
                                onChange={(e) => produceStore.updateHeader(index, 'value', e.target.value)}
                                placeholder="Value"
                                style={{ flex: 1 }}
                            />
                            <button
                                type="button"
                                className="danger"
                                onClick={() => produceStore.removeHeader(index)}
                                style={{ padding: '0.2rem 0.5rem' }}
                            >
                                ×
                            </button>
                        </div>
                    ))}
                </div>

                <button
                    type="submit"
                    className="primary"
                    disabled={produceStore.state === "LOADING"}
                    style={{ width: '100%', justifyContent: 'center', marginTop: '1rem', padding: '0.8rem' }}
                >
                    {produceStore.state === "LOADING" ? "Producing..." : "Produce Message"}
                </button>
            </form>
        </div>
    );
});
