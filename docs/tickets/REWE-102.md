# REWE-102 Reject overly long item names

| Field | Value |
|---|---|
| Type | Bug |
| Status | Ready for Development |
| Priority | High |
| Component | Warehouse app (API) |
| Reporter | Store Operations |
| Sprint | Warehouse Sprint 24 |
| Story points | 2 |
| Labels | `validation`, `bug` |

## Description

An item was created with a name of more than 300 characters (a product description pasted by mistake). Since
then the item list in the frontend is broken: the table is wider than the screen and the buttons are out of
reach.

**Steps to reproduce:** create an item with a name of 300 characters, open http://localhost:8000.
**Expected:** the API rejects the name. **Actual:** the item is created and breaks the layout.

## Acceptance criteria

- Creating an item with a name longer than 80 characters fails with HTTP 400 and
  `{"error": "name must be at most 80 characters"}`.
- Leading and trailing spaces are removed before the length is checked.
- The rule lives in `Inventory.java`; `Server.java` only maps the exception.
- `java backend/InventoryTest.java` covers both the limit and the trimming.

## Out of scope

- Cleaning up items that already have long names.
