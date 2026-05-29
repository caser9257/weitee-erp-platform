# Task Plan: SCM Inventory Query & Basic Data Page Refresh

## Goal
Refresh the six SCM pages under `库存查询与基础资料` with a consistent enterprise ERP visual system and stable interaction behavior, using StitchMCP as the design reference and keeping API contracts unchanged.

## Current Phase
Phase 3

## Phases

### Phase 1: Discovery
- [x] Confirm the six scoped pages
- [x] Inspect current page structure, states, and actions
- [x] Record findings in `findings.md`
- **Status:** complete

### Phase 2: Design Proposal
- [ ] Draft the page-level optimization approach
- [ ] Map shared layout patterns and per-page differences
- [ ] Share the design plan with the user for approval
- **Status:** in_progress

### Phase 3: Implementation
- [ ] Refactor the six page UIs
- [ ] Keep request/response contracts unchanged
- [ ] Align copy, layout, and state handling
- **Status:** pending

### Phase 4: Verification
- [ ] Verify loading / empty / error states
- [ ] Verify duplicate-click prevention and reset behavior
- [ ] Verify responsive behavior in wide, narrow, and small viewports
- **Status:** pending

### Phase 5: Delivery
- [ ] Summarize code changes
- [ ] Summarize validation results
- [ ] Note residual risks
- **Status:** pending

## Key Questions
1. Keep the six scoped pages limited to `warehouse`, `stock`, `stock-record`, `stock-analysis`, `netting-policy`, and `substitute-material`?
2. Should the two strategy pages (`netting-policy`, `substitute-material`) follow the same card-led style as the four inventory pages, or keep a lighter treatment?

## Decisions Made
| Decision | Rationale |
|----------|-----------|
| Scope is the six routes under `库存查询与基础资料` | The route map and menu-group metadata confirm the six-page boundary. |
| Do not change API contracts | The request is a frontend visual / interaction refresh, not a backend schema change. |
| Use StitchMCP / existing design system as visual reference | Keeps the refresh aligned with the current ERP design language. |

## Errors Encountered
| Error | Attempt | Resolution |
|-------|---------|------------|
| `frontend-state-guard` skill path not found under repo-local `.tester` | 1 | Resolved by loading the skill from `C:/Users/Administrator/.tester/skills`. |
| Initial route search hit the wrong SCM group (`仓储作业`) | 1 | Re-read `projectDrivenFlat.ts` and `projectDrivenFlat.spec.ts` to confirm the six-page scope. |

## Notes
- Inventory query pages inspected: `warehouse`, `stock`, `stock-record`, `stock-analysis`.
- Basic data pages inspected: `netting-policy`, `substitute-material`.
- StitchMCP project used: `projects/15546762320028241086` (`Manufacturing ERP Design`).

---

## Warehouse Batch Edit Follow-up

### Goal
Add and stabilize batch editing for ERP `仓库` master data so the same container pattern can be reused on other low-risk master-data pages.

### Constraints / assumptions
- Keep API contract limited to warehouse batch update.
- Keep UI copy in Simplified Chinese.
- Avoid `defaultStatus` and price fields for batch editing.
- Reuse the shared batch-edit container and field editor.

### Task Breakdown
1. Backend contract
   - Add warehouse batch-update request / response VO.
   - Add `PUT /erp/warehouse/batch-update`.
   - Add warehouse-specific batch field validation and updates.
2. Frontend integration
   - Add warehouse API wrapper.
   - Add warehouse batch-edit adapter and drawer.
   - Wire the list page selection, toolbar action, and success refresh.
3. Verification
   - Compile backend.
   - Type-check changed frontend files.
   - Run browser/manual validation for wide, narrow, and small viewports.

### Validation Plan
- Confirm warehouse selection clears after refresh.
- Confirm batch-edit open/close resets stale state.
- Confirm duplicate submit clicks are blocked.
- Confirm layout does not collapse at 1280px and 375px widths.

### Risks / blockers
- Browser validation remains pending.
- `status` and `sort` updates are safe by design, but they still need permission coverage to stay consistent with the rest of the module.
