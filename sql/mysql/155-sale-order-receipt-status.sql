-- 销售订单收款状态字段
-- 日期：2026-06-23

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- 添加收款状态和收款金额字段
ALTER TABLE `erp_sale_order` 
ADD COLUMN `receipt_status` TINYINT DEFAULT 0 COMMENT '收款状态 0=未收款 1=部分收款 2=全额收款',
ADD COLUMN `receipt_price` DECIMAL(19,4) DEFAULT 0 COMMENT '已收款金额';

-- 初始化现有数据：汇总已审批出库单的收款金额
UPDATE `erp_sale_order` o SET 
  `receipt_price` = (
    SELECT COALESCE(SUM(so.receipt_price), 0) 
    FROM `erp_sale_out` so 
    WHERE so.order_id = o.id 
      AND so.deleted = 0
      AND so.status = 20  -- 已审批状态
  );

-- 根据金额计算收款状态
UPDATE `erp_sale_order` SET 
  `receipt_status` = CASE 
    WHEN `receipt_price` <= 0 THEN 0
    WHEN `receipt_price` >= `total_price` THEN 2
    ELSE 1
  END;

SET FOREIGN_KEY_CHECKS = 1;
