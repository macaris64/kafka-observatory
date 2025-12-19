# 007 – Frontend Structure

## Kafka Observatory

---

## 1. Purpose of This Document

This document describes the **frontend application structure** of Kafka Observatory.

Its goals are to:

* Define a clear and scalable frontend folder structure
* Establish responsibilities for pages, components, and services
* Align frontend design with backend API contracts
* Enable fast development without architectural confusion

The frontend is designed to remain **simple, predictable, and MVP-focused**.

---

## 2. Frontend Design Principles

The frontend follows these core principles:

* Backend-driven contracts (no frontend business logic)
* Clear separation between pages and reusable components
* Explicit API service layer
* Stateless UI where possible
* Minimal global state

The frontend does not contain Kafka-specific logic.

---

## 3. Technology Stack

* React
* TypeScript
* Vite
* Fetch API (or lightweight wrapper)
* Optional UI library (MVP-level)

No heavy state management libraries are required for MVP.

---

## 4. High-Level Folder Structure

```
frontend/
│
├── src/
│   ├── pages
│   ├── components
│   ├── services
│   ├── hooks
│   ├── types
│   ├── utils
│   ├── App.tsx
│   └── main.tsx
│
├── public
└── package.json
```

Each folder has a single, well-defined responsibility.

---

## 5. Pages

The `pages` directory contains top-level route components.

```
pages/
 ├── TopicsPage.tsx
 ├── TopicConsumePage.tsx
 └── ProducePage.tsx
```

Responsibilities:

* Page-level layout
* API calls via services
* State coordination for the page

Pages should not contain reusable UI logic.

---

## 6. Components

The `components` directory contains reusable UI building blocks.

```
components/
 ├── TopicTable.tsx
 ├── MessageTable.tsx
 ├── ProduceForm.tsx
 └── LoadingIndicator.tsx
```

Responsibilities:

* Rendering UI elements
* Receiving data via props
* No direct API calls

Components must remain presentational.

---

## 7. Services

The `services` directory handles all backend communication.

```
services/
 ├── clusterService.ts
 ├── topicService.ts
 ├── consumeService.ts
 └── produceService.ts
```

Responsibilities:

* Encapsulating REST API calls
* Managing WebSocket connections
* Mapping API responses to frontend types

No API calls should be made outside this layer.

---

## 8. Hooks

The `hooks` directory contains custom React hooks.

```
hooks/
 ├── useTopics.ts
 ├── useConsume.ts
 └── useClusterInfo.ts
```

Responsibilities:

* Managing component state
* Encapsulating side effects
* Reusing logic across pages

Hooks may depend on services but not vice versa.

---

## 9. Types

The `types` directory contains shared TypeScript type definitions.

```
types/
 ├── Topic.ts
 ├── Message.ts
 └── Cluster.ts
```

Types are aligned with backend API contracts.

---

## 10. Utilities

The `utils` directory contains helper functions.

Examples:

* JSON detection
* Date formatting
* Error mapping

Utilities must remain framework-agnostic.

---

## 11. Routing Strategy

Routing is handled at the application level.

Example routes:

* `/` → TopicsPage
* `/topics/:name` → TopicConsumePage
* `/produce` → ProducePage

The backend serves the frontend as a single-page application.

---

## 12. State Management Strategy

MVP-level state management strategy:

* Local component state
* Lifted state within pages
* Minimal shared state via hooks

No global state management library is required.

---

## 13. Error Handling Strategy

* API errors are surfaced at the page level
* Components receive only display-ready data
* Errors are shown using simple UI feedback

No complex retry or recovery logic is required in MVP.

---

## 14. Build and Deployment

* Frontend is built using Vite
* Output is generated in the `dist` directory
* Build artifacts are copied into backend static resources
* Backend serves frontend assets in production

No separate frontend deployment is required.

---

## 15. MVP Constraints

The following constraints apply during MVP:

* Single backend instance
* No authentication or authorization
* No client-side caching strategy
* No SSR or advanced rendering

These constraints are intentional and simplify development.

---

## 16. Evolution Guidelines

Future improvements may include:

* Improved UI styling
* Advanced filtering and search
* State management enhancements
* Authentication-aware routing

These should be added without disrupting the existing structure.

---

## 17. Summary

The frontend structure of Kafka Observatory is designed to be:

* Simple and intuitive
* Easy to extend
* Closely aligned with backend APIs
* Focused on MVP delivery

By enforcing clear boundaries and responsibilities, the frontend remains maintainable while supporting future growth.
