# REWE-101 Low-stock warning in the item list

| Field | Value |
|---|---|
| Type | Story |
| Status | Ready for Development |
| Priority | Medium |
| Component | Warehouse app (frontend, API) |
| Reporter | Store Operations |
| Sprint | Warehouse Sprint 24 |
| Story points | 3 |
| Labels | `inventory`, `ux` |

## Description

As store staff, I want to see at a glance which items are running low, so that I can reorder them before the
shelf is empty.

Today the item list shows only the quantity. During the morning check, staff scroll through the whole list and
regularly overlook items with 1 or 2 pieces left.

## Acceptance criteria

- An item with a quantity below 5 is marked as "low stock" in the item list.
- An item with quantity 0 is marked as "out of stock".
- The threshold 5 is defined once in the backend and returned by the API, not hard-coded in the frontend.
- The existing invariants stay: stock never negative, SKUs unique, a booking delta is never 0.
- `java backend/InventoryTest.java` covers the new rule.

## Out of scope

- Automatic reordering or e-mail notifications.
- A configurable threshold per item (follow-up ticket if needed).
