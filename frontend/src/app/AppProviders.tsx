import React, { createContext, useContext } from "react";
import { AppStore, appStore } from "../stores/AppStore";

const StoreContext = createContext<AppStore | null>(null);

export const useStores = () => {
    const store = useContext(StoreContext);
    if (!store) {
        throw new Error("useStores must be used within a StoreProvider");
    }
    return store;
};

export const AppProviders: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    return (
        <StoreContext.Provider value={appStore}>
            {children}
        </StoreContext.Provider>
    );
};
