# Findings & Decisions

## Requirements
- Optimize the six SCM pages under `库存查询与基础资料`.
- Use StitchMCP for design alignment.
- Follow AGENTS rules for interactive frontend work.
- Keep API contracts unchanged unless explicitly required.
- Preserve Chinese UI copy.

## Research Findings
- The scoped routes are:
  - `warehouse` -> `src/views/erp/stock/warehouse/index.vue`
  - `stock` -> `src/views/erp/stock/stock/index.vue`
  - `stock-record` -> `src/views/erp/stock/record/index.vue`
  - `stock-analysis` -> `src/views/erp/stock/analysis/index.vue`
  - `netting-policy` -> `src/views/erp/mrp/netting-policy/index.vue`
  - `substitute-material` -> `src/views/erp/mrp/substitute/index.vue`
- `stock-in`, `stock-out`, `stock-move`, and `stock-check` belong to the separate `仓储作业` group, so they are out of scope for this task.
- The current ERP design system already exists in StitchMCP project `15546762320028241086`.
- `warehouse`, `stock`, `stock-record`, and `stock-analysis` already use a more modern card / drawer style.
- `netting-policy` and `substitute-material` are visually flatter and are the likeliest targets for structural polish.

## Technical Decisions
| Decision | Rationale |
|----------|-----------|
| Reuse existing API endpoints and data contracts | The requested work is visual and interaction polish, not backend reshaping. |
| Treat the six pages as one coherent module | This enables shared layout language, spacing, and state handling. |
| Keep Chinese visible text | Matches the repo and the ERP audience. |
| Use StitchMCP as a style reference, not as a source of business logic | Prevents accidental divergence from the actual Vue implementation. |
| Use compact linear attachments when attachment count is small | Prevents single small attachments from occupying oversized card blocks in print previews. |

## Issues Encountered
| Issue | Resolution |
|-------|------------|
| Initial menu search matched the wrong SCM subgroup | Rechecked route metadata and test fixtures to find the correct six-page boundary. |

## Resources
- `yudao-ui/yudao-ui-admin-vue3/src/router/modules/projectDrivenFlat.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/router/modules/projectDrivenFlat.spec.ts`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/stock/warehouse/index.vue`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/stock/stock/index.vue`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/stock/record/index.vue`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/stock/analysis/index.vue`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/mrp/netting-policy/index.vue`
- `yudao-ui/yudao-ui-admin-vue3/src/views/erp/mrp/substitute/index.vue`
- Stitch project `projects/15546762320028241086`

## Visual/Browser Findings
- Stitch project title: `Manufacturing ERP Design`.
- The project already contains a reusable ERP design system with blue primary color, light surfaces, and IBM Plex Sans / Inter typography.
- The project also contains a comparable warehouse-style screen (`物料仓储中心`) that can inform table density and card hierarchy.

## Warehouse Batch Edit Addendum

### Decision
- Chosen next batch-edit target: `仓库`
- Rejected targets for this round:
  - `销售单据` and `采购单据` because they are workflow-heavy and already have state-sensitive actions.
  - `采购来源批次` because it is a specialized traceability object, not a stable master-data page.
- Batch-edit field set for `仓库`:
  - `address`
  - `principal`
  - `remark`
  - `status`
  - `sort`
- Explicitly excluded:
  - `defaultStatus`
  - `warehousePrice`
  - `truckagePrice`
  - `name`

### API / UI Notes
- Backend contract added:
  - `PUT /erp/warehouse/batch-update`
  - request mode fixed to `overwrite`
- Frontend entry added:
  - warehouse table multi-select
  - batch-edit toolbar action
  - shared batch-edit drawer reuse
- Result state:
  - compile passed
  - changed-file TS check passed
  - browser validation still pending

### Carry-Forward Risk
- Batch-editing `status` and `sort` is still a master-data mutation and should stay permission-gated.
- No browser-level confirmation yet for drawer reopen/reset and narrow-screen wrapping.
