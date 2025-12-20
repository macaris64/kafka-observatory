import React from "react";
import { observer } from "mobx-react-lite";

export const HomePage: React.FC = observer(() => {
    return (
        <div className="home-page">
            <p>Welcome to the Kafka Observatory UI</p>
        </div>
    );
});
