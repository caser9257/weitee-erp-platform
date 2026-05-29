/*
 ERP MRP 全链路测试数据 - 验证脚本
 作用：
 - 快速确认基础数据、销售单、计划、建议、短缺、任务是否齐全
 - 只读查询，不修改数据
*/

SET NAMES utf8mb4;

USE `ruoyi-vue-pro`;

SET @project_id := 993701;
SET @sale_order_id := 993901;
SET @plan_id := 993601;

SELECT 'sale_order' AS scope, so.id, so.no, so.status, so.business_type, so.project_id, so.delivery_date
FROM erp_sale_order so
WHERE so.id = @sale_order_id;

SELECT 'audit_log' AS scope, al.id, al.action_type, al.before_status, al.after_status, al.reason
FROM erp_sale_order_audit_log al
WHERE al.order_id = @sale_order_id
ORDER BY al.id;

SELECT 'project_task' AS scope, t.id, t.role_code, t.task_type, t.task_status, t.source_type, t.source_id, t.summary, t.due_time
FROM erp_project_role_task t
WHERE t.project_id = @project_id
ORDER BY t.role_code, t.id;

SELECT 'plan' AS scope, p.id, p.plan_no, p.plan_name, p.plan_start_date, p.plan_end_date, p.status, p.run_time
FROM erp_mrp_plan p
WHERE p.id = @plan_id;

SELECT 'demand' AS scope, d.id, d.source_type, d.source_id, d.source_item_id, d.product_id, d.demand_qty, d.demand_date
FROM erp_mrp_demand d
WHERE d.plan_id = @plan_id
ORDER BY d.id;

SELECT 'trace_node' AS scope, n.id, n.trace_level, n.material_id, n.parent_material_id, n.suggest_type, n.trace_path_key,
       n.gross_demand_qty, n.theoretical_net_demand_qty, n.execution_net_demand_qty, n.skip_reason
FROM erp_mrp_trace_node n
WHERE n.plan_id = @plan_id
ORDER BY n.trace_path_key, n.id;

SELECT 'result' AS scope, r.id, r.trace_level, r.material_id, r.parent_material_id, r.suggest_type, r.trace_path_key,
       r.gross_demand_qty, r.net_demand_qty, r.skip_reason
FROM erp_mrp_result r
WHERE r.plan_id = @plan_id
ORDER BY r.trace_path_key, r.id;

SELECT 'result_component' AS scope, c.result_id, c.component_code, c.component_name, c.component_role, c.sequence_no,
       c.base_qty, c.consumed_qty, c.remaining_qty
FROM erp_mrp_result_component c
WHERE c.plan_id = @plan_id
ORDER BY c.result_id, c.sequence_no, c.id;

SELECT 'purchase_suggest' AS scope, ps.id, ps.material_id, ps.trace_level, ps.suggest_qty, ps.status, ps.trace_path_key
FROM erp_purchase_suggest ps
WHERE ps.plan_id = @plan_id
ORDER BY ps.id;

SELECT 'production_suggest' AS scope, pr.id, pr.product_id, pr.trace_level, pr.suggest_qty, pr.status, pr.trace_path_key
FROM erp_production_suggest pr
WHERE pr.plan_id = @plan_id
ORDER BY pr.id;

SELECT 'shortage' AS scope, s.id, s.material_id, s.trace_level, s.shortage_qty, s.trace_path_key
FROM erp_mrp_shortage s
WHERE s.plan_id = @plan_id
ORDER BY s.id;

SELECT 'summary' AS scope,
       (SELECT COUNT(*) FROM erp_mrp_demand WHERE plan_id = @plan_id) AS demand_cnt,
       (SELECT COUNT(*) FROM erp_mrp_trace_node WHERE plan_id = @plan_id) AS trace_cnt,
       (SELECT COUNT(*) FROM erp_mrp_result WHERE plan_id = @plan_id) AS result_cnt,
       (SELECT COUNT(*) FROM erp_mrp_result_component WHERE plan_id = @plan_id) AS component_cnt,
       (SELECT COUNT(*) FROM erp_purchase_suggest WHERE plan_id = @plan_id) AS purchase_cnt,
       (SELECT COUNT(*) FROM erp_production_suggest WHERE plan_id = @plan_id) AS production_cnt,
       (SELECT COUNT(*) FROM erp_mrp_shortage WHERE plan_id = @plan_id) AS shortage_cnt,
       (SELECT COUNT(*) FROM erp_project_role_task WHERE project_id = @project_id) AS task_cnt;
