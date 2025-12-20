import { render, screen, fireEvent } from "@testing-library/react";
import { ProducerPanel } from "./ProducerPanel";
import { ProduceStore } from "../stores/ProduceStore";
import { apiClient } from "../services/apiClient";
import "@testing-library/jest-dom";

jest.mock("../services/apiClient");

describe("ProducerPanel", () => {
    let produceStore: ProduceStore;

    beforeEach(() => {
        produceStore = new ProduceStore();
        jest.clearAllMocks();
    });

    it("renders the form fields", () => {
        render(<ProducerPanel produceStore={produceStore} />);

        expect(screen.getByLabelText(/Topic/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Key/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Partition/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/Value/i)).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Produce Message/i })).toBeInTheDocument();
    });

    it("shows validation errors on submit with empty fields", async () => {
        render(<ProducerPanel produceStore={produceStore} />);

        fireEvent.click(screen.getByRole("button", { name: /Produce Message/i }));

        expect(await screen.findByText(/Topic is required/i)).toBeInTheDocument();
        expect(await screen.findByText(/Value is required/i)).toBeInTheDocument();
    });

    it("submits the form and shows success message", async () => {
        (apiClient.post as jest.Mock).mockResolvedValue({
            topic: "test-topic",
            partition: 0,
            offset: 42
        });

        render(<ProducerPanel produceStore={produceStore} />);

        fireEvent.change(screen.getByLabelText(/Topic/i), { target: { value: "test-topic" } });
        fireEvent.change(screen.getByLabelText(/Value/i), { target: { value: "hello kafka" } });

        fireEvent.click(screen.getByRole("button", { name: /Produce Message/i }));

        expect(await screen.findByText(/Success!/i)).toBeInTheDocument();
        expect(screen.getAllByText(/partition/i).length).toBeGreaterThan(0);
        expect(screen.getByText("0")).toBeInTheDocument();
        expect(screen.getAllByText(/offset/i).length).toBeGreaterThan(0);
        expect(screen.getByText("42")).toBeInTheDocument();
    });

    it("manages headers", () => {
        render(<ProducerPanel produceStore={produceStore} />);

        fireEvent.click(screen.getByText(/\+ Add Header/i));

        // Exact placeholder match "Key" vs "Message Key"
        const headerKeyInputs = screen.getAllByPlaceholderText("Key");
        expect(headerKeyInputs.length).toBe(1);

        fireEvent.change(headerKeyInputs[0], { target: { value: "h-key" } });
        expect(produceStore.headers[0].key).toBe("h-key");
    });
});
