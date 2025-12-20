import { AppProviders } from "./AppProviders";
import { AppLayout } from "../components/AppLayout";
import { HomePage } from "../pages/HomePage";

function App() {
    return (
        <AppProviders>
            <AppLayout>
                <HomePage />
            </AppLayout>
        </AppProviders>
    );
}

export default App;
