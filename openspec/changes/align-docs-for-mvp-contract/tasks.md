## 1. Establish Document Baseline

- [x] 1.1 Mark the affected `/doc` documents as part of the `align-docs-for-mvp-contract` revision baseline.
- [x] 1.2 Add or update each affected document's status to indicate whether it is MVP-required or enhancement-stage.
- [x] 1.3 Ensure the documentation states that no code changes should proceed until the revised document baseline is reviewed.

## 2. Align API and Cross-Layer Contracts

- [x] 2.1 Update `doc/接口文档.md` to define `ChatReply` as the canonical `/api/chat` response contract.
- [x] 2.2 Add structured `data` examples for Knowledge references and Shopping cart/order/stock/logistics results.
- [x] 2.3 Define a minimal unified error response shape with `code`, `message`, `traceId`, and optional `details`.
- [x] 2.4 Clarify `/api/chat`, `/api/ready`, and `/api/health` roles and requiredness.
- [x] 2.5 Normalize intent, quality, shopping action, SKU, cart, and order naming across the interface document.

## 3. Align Agent Architecture Documents

- [x] 3.1 Update `doc/技术架构.md` to describe the MVP chain and mark external rerank, HNSW, Tika multi-format ingestion, and full three-layer memory as staged enhancements.
- [x] 3.2 Update `doc/主Agent-技术架构.md` so Main Agent owns intent routing, clarification, retry decisions, final quality verdicts, and final answer composition.
- [x] 3.3 Update `doc/知识库Agent-技术架构.md` so Knowledge returns evidence, scores, hit signals, and references without owning final user-facing decisions.
- [x] 3.4 Remove or reconcile duplicate query rewrite and entity extraction ownership between Main Agent and Knowledge documents.
- [x] 3.5 Update `doc/购物Agent-技术架构.md` to require Shopping actions through MCP Client/Server in MVP.
- [x] 3.6 Unify MCP transport and dependency wording across architecture documents, or explicitly list any remaining transport question as unresolved.
- [x] 3.7 Remove stale Spring AI Alibaba / SAA dependency wording from current implementation paths unless clearly labeled as historical context.

## 4. Align Database Documentation and SQL Design

- [x] 4.1 Update `doc/database/数据库设计.md` to classify tables and constraints into P0, P1, and P2 implementation priorities.
- [x] 4.2 Resolve the foreign-key strategy by documenting either physical foreign keys or logical application-enforced relationships.
- [x] 4.3 Fix the documented trace model so one trace ID can contain multiple node/span records.
- [x] 4.4 Clarify whether order and cart models are MVP mock models or require `t_order_item` and explicit cart lifecycle design.
- [x] 4.5 Normalize database enum values for intent, quality verdict, and shopping actions.
- [x] 4.6 Document the `updated_at` maintenance strategy and soft-delete uniqueness policy.
- [x] 4.7 Clarify whether `init.sql` is idempotent or only valid for empty databases.
- [x] 4.8 Document the MVP Chinese retrieval strategy and distinguish it from later zhparser/HNSW/rerank optimizations.
- [x] 4.9 Align `schema.sql`, `init.sql`, and `create-database.sql` comments with the revised database design decisions.

## 5. Align Frontend UIUX and Frontend Technical Documents

- [x] 5.1 Update frontend documents so shopping state cards use structured `data` as the formal data source.
- [x] 5.2 Mark mock/static product cards as sample, demo, or quick-entry content rather than Agent-generated recommendations.
- [x] 5.3 Clarify that parsing `answer` text can only produce weak hints, not authoritative shopping state transitions.
- [x] 5.4 Normalize quick action behavior as fill-input-first for MVP unless a direct-send enhancement is explicitly documented.
- [x] 5.5 Revise responsive layout breakpoints to avoid three-column overflow around 1024px.
- [x] 5.6 Clarify mobile scrolling behavior by separating chat-list internal scroll from auxiliary sheet, tab, or page scrolling.
- [x] 5.7 Align the backend status panel with `/api/ready` as primary and `/api/health` as optional liveness.
- [x] 5.8 Mark complex visual effects, avatar asset replacement, online fonts, and high-coverage E2E as staged enhancements unless required by MVP acceptance.

## 6. Update Test Documentation

- [x] 6.1 Update `test/README.md` to reflect the MVP contract alignment and revised acceptance priorities.
- [x] 6.2 Update API/DTO-related test cases to validate `ChatReply`, structured `data`, unified enums, and error response behavior.
- [x] 6.3 Update Agent orchestration test cases to verify single-owner responsibilities for Main Agent, Knowledge, and Shopping.
- [x] 6.4 Update database-related test cases to reflect staged table priority, trace semantics, FK strategy, seed behavior, and enum normalization.
- [x] 6.5 Update frontend test cases to verify ready/health roles, responsive breakpoints, mock labeling, and no authoritative state from `answer` parsing.

## 7. Validate OpenSpec and Review Readiness

- [x] 7.1 Run OpenSpec validation for `align-docs-for-mvp-contract` and fix any artifact issues.
- [x] 7.2 Review the revised documents for internal consistency against `specs/mvp-contract-alignment/spec.md`.
- [x] 7.3 Summarize remaining open questions for user review before any application code or SQL implementation changes.
