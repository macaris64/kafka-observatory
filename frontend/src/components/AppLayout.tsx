import React from "react";

export const AppLayout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return (
        <div className="app-layout">
            <header>
                <h1>Kafka Observatory UI</h1>
            </header>
            <main>{children}</main>
        </div>
    );
};
