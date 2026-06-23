-- 采购订单付款状态字段
-- 日期：2026-06-23

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET FOREIGN_KEY_CHECKS = 0;
USE `ruoyi-vue-pro`;

-- 添加付款状态和付款金额字段
ALTER TABLE `erp_purchase_order` 
ADD COLUMN `payment_status` TINYINT DEFAULT 0 COMMENT '付款状态 0=未付款 1=部分付款 2=全额付款',
ADD COLUMN `payment_price` DECIMAL(19,4) DEFAULT 0 COMMENT '已付款金额';

-- 初始化现有数据：汇总已审批入库单的付款金额
UPDATE `erp_purchase_order` o SET 
  `payment_price` = (
    SELECT COALESCE(SUM(pi.payment_price), 0) 
    FROM `erp_purchase_in` pi 
    WHERE pi.order_id = o.id 
      AND pi.deleted = 0
      AND pi.status = 20  -- 已审批状态
  );

-- 根据金额计算付款状态
UPDATE `erp_purchase_order` SET 
  `payment_status` = CASE 
    WHEN `payment_price` <= 0 THEN 0
    WHEN `payment_price` >= `total_price` THEN 2
    ELSE 1
  END;

SET FOREIGN_KEY_CHECKS = 1;
