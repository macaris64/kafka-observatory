import React from 'react';
import { observer } from 'mobx-react-lite';
import { AppLayout } from '../components/AppLayout';
import { ClusterStatus } from '../components/ClusterStatus';
import { TopicTable } from '../components/TopicTable';
import { ConsumeControls } from '../components/ConsumeControls';
import { MessageViewer } from '../components/MessageViewer';
import { appStore } from '../stores/AppStore';

export const HomePage: React.FC = observer(() => {
    const { topicStore, consumeStore } = appStore;

    return (
        <AppLayout title="Kafka Observatory">
            <ClusterStatus appStore={appStore} />
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '2rem' }}>
                <div>
                    <TopicTable topicStore={topicStore} />
                </div>
                <div>
                    <ConsumeControls
                        consumeStore={consumeStore}
                        topicName={topicStore.selectedTopicName}
                    />
                    <MessageViewer consumeStore={consumeStore} />
                </div>
            </div>
        </AppLayout>
    );
});
