import { ProduceStore } from "./ProduceStore";
import { apiClient } from "../services/apiClient";

jest.mock("../services/apiClient");

describe("ProduceStore", () => {
    let store: ProduceStore;

    beforeEach(() => {
        store = new ProduceStore();
        jest.clearAllMocks();
    });

    it("should initialize with default values", () => {
        expect(store.topic).toBe("");
        expect(store.key).toBe("");
        expect(store.value).toBe("");
        expect(store.partition).toBeUndefined();
        expect(store.headers).toEqual([]);
        expect(store.state).toBe("IDLE");
    });

    it("should update form fields", () => {
        store.setTopic("test-topic");
        store.setKey("test-key");
        store.setValue("test-value");
        store.setPartition(1);

        expect(store.topic).toBe("test-topic");
        expect(store.key).toBe("test-key");
        expect(store.value).toBe("test-value");
        expect(store.partition).toBe(1);
    });

    it("should manage headers", () => {
        store.addHeader();
        expect(store.headers.length).toBe(1);

        store.updateHeader(0, "key", "h1");
        store.updateHeader(0, "value", "v1");
        expect(store.headers[0]).toEqual({ key: "h1", value: "v1" });

        store.removeHeader(0);
        expect(store.headers.length).toBe(0);
    });

    it("should validate required fields", () => {
        expect(store.validate()).toBe(false);
        expect(store.validationErrors.topic).toBeDefined();
        expect(store.validationErrors.value).toBeDefined();

        store.setTopic("topic");
        store.setValue("value");
        expect(store.validate()).toBe(true);
        expect(Object.keys(store.validationErrors).length).toBe(0);
    });

    it("should handle successful production", async () => {
        const mockResponse = { topic: "t1", partition: 0, offset: 100 };
        (apiClient.post as jest.Mock).mockResolvedValue(mockResponse);

        store.setTopic("t1");
        store.setValue("val");

        const promise = store.produce();
        expect(store.state).toBe("LOADING");

        await promise;

        expect(store.state).toBe("SUCCESS");
        expect(store.result).toEqual(mockResponse);
        expect(store.value).toBe(""); // Cleared
        expect(apiClient.post).toHaveBeenCalledWith("/produce", expect.objectContaining({
            topic: "t1",
            value: "val"
        }));
    });

    it("should handle production error", async () => {
        (apiClient.post as jest.Mock).mockRejectedValue(new Error("Network Error"));

        store.setTopic("t1");
        store.setValue("val");

        await store.produce();

        expect(store.state).toBe("ERROR");
        expect(store.error).toBe("Network Error");
    });

    it("should reset state", () => {
        store.state = "SUCCESS";
        store.error = "err";
        store.reset();
        expect(store.state).toBe("IDLE");
        expect(store.error).toBeNull();
    });
});
