-- 销售订单审批规则旧流程 Key 迁移
-- 仅迁移生效规则，正式流程定义 Key 为 erp_sale_order。

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `weitee-erp`;

UPDATE bpm_approval_rule r
JOIN bpm_approval_scheme_version v
  ON v.id = r.scheme_version_id
 AND v.status = 30
 AND v.deleted = b'0'
SET r.process_json = 'erp_sale_order',
    r.updater = 'admin',
    r.update_time = NOW()
WHERE r.process_json = 'sale_order_approval'
  AND r.deleted = b'0'
  AND EXISTS (
      SELECT 1
      FROM ACT_RE_PROCDEF p
      WHERE p.KEY_ = 'erp_sale_order'
        AND p.SUSPENSION_STATE_ = 1
  );
