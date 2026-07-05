## ADDED Requirements

### Requirement: MVP scope shall be explicit across documents
The documentation set SHALL define the first implementation phase as a runnable MVP contract rather than a complete shopping platform. The MVP contract MUST include `/api/chat`, `/api/ready`, Main Agent orchestration, Knowledge retrieval, Shopping through MCP mock/demo tools, and a basic frontend chat experience.

#### Scenario: Reader identifies first-phase scope
- **WHEN** a developer reads the revised architecture and interface documents
- **THEN** they can distinguish MVP-required capabilities from later enhancements without relying on unstated assumptions

#### Scenario: Enhancement is not treated as MVP blocker
- **WHEN** a capability such as external rerank, HNSW tuning, automatic long-term memory distillation, Tika multi-format ingestion, or complex frontend animation is described
- **THEN** the documentation marks it as an enhancement or later phase unless it is required for the MVP chat loop

### Requirement: Chat API shall expose a structured response contract
The `/api/chat` documentation SHALL define `ChatReply` as the canonical response contract. `ChatReply` MUST include a human-readable `answer` and structured metadata fields for intent, clarification, retrieval quality, retry count, child calls, trace ID, and typed business data.

#### Scenario: Shopping result is returned
- **WHEN** the user triggers a shopping action such as add-to-cart or place-order
- **THEN** the documented response includes user-readable text in `answer` and machine-readable shopping result data in `data`

#### Scenario: Knowledge result is returned
- **WHEN** the user asks a knowledge question and retrieval succeeds
- **THEN** the documented response includes `intent` as `KNOWLEDGE`, a quality level, retry count, and optional references or evidence in structured data

#### Scenario: Clarification is required
- **WHEN** the user request lacks required slots or contains ambiguous product choices
- **THEN** the documented response sets `needClarify` and provides a clarification question without pretending the action has completed

### Requirement: Cross-layer naming and enum values shall be unified
The documentation set SHALL use one canonical naming scheme for cross-layer concepts. Intent values MUST be `KNOWLEDGE`, `SHOPPING`, and `SYSTEM`; quality failure MUST be `FAILED`; shopping actions MUST use lower snake case tool names; externally visible SKU references MUST use `skuCode` unless explicitly documented otherwise.

#### Scenario: Intent is documented in API and database
- **WHEN** the interface document and database design both describe top-level intent
- **THEN** they use the same intent vocabulary or explicitly document a necessary mapping

#### Scenario: Quality failure is stored and returned
- **WHEN** retrieval fails completely
- **THEN** the documented API response and persistence fields use `FAILED` rather than a competing spelling such as `FAILURE`

#### Scenario: Shopping tool action is routed
- **WHEN** an intent node maps to a shopping tool
- **THEN** the documented action value matches the MCP tool name format such as `add_to_cart`

### Requirement: Agent responsibility boundaries shall be single-owner
The documentation SHALL define Main Agent as the owner of user-facing decisions and child nodes as capability providers. Main Agent MUST own intent routing, clarification, retry decisions, final quality verdicts, and final answer composition. Knowledge MUST return evidence and retrieval signals. Shopping MUST return tool execution results.

#### Scenario: Knowledge retrieval is insufficient
- **WHEN** Knowledge returns low recall or missing evidence signals
- **THEN** Main Agent decides whether to retry, clarify, or degrade rather than Knowledge producing the final user-facing decision independently

#### Scenario: Entity extraction is needed
- **WHEN** entity or slot extraction is required for quality judgment or tool routing
- **THEN** the documentation identifies a single owner for the canonical extracted values consumed by downstream nodes

### Requirement: Shopping tools shall go through the documented MCP boundary
The MVP documentation SHALL require Shopping actions to be invoked through MCP Client/Server. The MCP Server MAY use mock or demo data in the first phase, but the documentation MUST NOT define an alternate first-phase path that bypasses MCP while also claiming MCP is mandatory.

#### Scenario: Add-to-cart is executed in MVP
- **WHEN** the user asks to add a product to the cart
- **THEN** the documented flow routes from Main Agent or Shopping Node through MCP Client to an MCP tool

#### Scenario: MCP uses demo data
- **WHEN** the MCP Server uses local mock data or shared demo database tables
- **THEN** the documentation states that this simulates an external shopping system and is not the final production boundary

### Requirement: Database design shall be staged and internally consistent
The database documentation SHALL classify tables and constraints by MVP priority and SHALL resolve contradictions between conceptual design and SQL scripts. At minimum, the documentation MUST address trace identity, foreign-key strategy, order details, cart lifecycle, enum values, update timestamps, seed idempotency, and Chinese retrieval support.

#### Scenario: Trace stores multiple nodes
- **WHEN** one chat request invokes multiple internal nodes
- **THEN** the documented trace model allows multiple node/span records under one trace ID

#### Scenario: Foreign keys are described
- **WHEN** a field is labeled as a foreign key in the database design
- **THEN** the SQL either defines a physical foreign key or the documentation explicitly states that the relationship is a logical foreign key enforced by the application

#### Scenario: Seed scripts are rerun
- **WHEN** a developer reruns initialization scripts
- **THEN** the documentation states whether the script is idempotent or only valid for an empty database

### Requirement: Frontend shall render stable contracts and label demo data
The frontend documentation SHALL require the UI to render stable API fields rather than relying on natural-language parsing for core state. Shopping status cards MUST use structured `data` when available. Mock or static product cards MUST be visually or textually identified as demo content.

#### Scenario: Chat answer contains shopping text only
- **WHEN** the backend response lacks structured shopping `data`
- **THEN** the frontend documentation permits only a weak hint or plain answer display, not a guaranteed cart/order state transition

#### Scenario: Mock product card is shown
- **WHEN** the frontend displays a product card not produced by the current `/api/chat` response
- **THEN** the UI documentation requires it to be labeled as sample, demo, or quick-entry content

### Requirement: Ready and health endpoints shall have distinct roles
The documentation SHALL distinguish `/api/chat` as the business entrypoint, `/api/ready` as the integration readiness endpoint, and `/api/health` as an optional liveness endpoint. `/api/ready` MUST document whether it checks infrastructure only or also business data readiness.

#### Scenario: Frontend status panel is implemented
- **WHEN** the frontend shows backend status
- **THEN** it uses `/api/ready` as the primary integration signal and treats `/api/health` as optional liveness information

#### Scenario: Database connection is up but knowledge index is empty
- **WHEN** `/api/ready` reports infrastructure status
- **THEN** the documentation makes clear whether empty knowledge vectors still count as business-ready or are reported separately

### Requirement: Responsive frontend layout shall avoid known overflow
The frontend UIUX and technical architecture documents SHALL define breakpoints and column minimums that avoid horizontal overflow at supported viewport widths.

#### Scenario: Viewport is around 1024 pixels wide
- **WHEN** the application is rendered on a 1024px-wide screen
- **THEN** the documented layout does not require three fixed/minimum-width columns whose combined width exceeds the viewport

#### Scenario: Mobile layout shows auxiliary panels
- **WHEN** the application is rendered on a mobile viewport
- **THEN** the documentation distinguishes chat-list internal scrolling from optional page, sheet, drawer, or tab scrolling for auxiliary content
