-- ============================================================
-- 商城（mall）与公众号（mp）模块数据清理脚本
-- 生成日期：2026-05-07
-- 适用数据库：MySQL
-- ⚠️ 执行前请先备份数据库！
-- ============================================================

-- ==================== 1. 清理菜单数据 ====================

-- 1.1 删除商城相关菜单（component 包含 mall/ 或 path 以 /mall 开头）
DELETE FROM system_menu WHERE component LIKE '%mall/%'
   OR path LIKE '/mall%';

-- 1.2 删除商城权限菜单（permission 以 product: trade: promotion: statistics: 开头）
DELETE FROM system_menu WHERE permission LIKE 'product:%'
   OR permission LIKE 'trade:%'
   OR permission LIKE 'promotion:%'
   OR permission LIKE 'statistics:%';

-- 1.3 删除公众号相关菜单
DELETE FROM system_menu WHERE component LIKE '%mp/%'
   OR permission LIKE 'mp:%'
   OR name LIKE '公众号%';

-- ==================== 2. 清理字典数据 ====================

-- 2.1 删除公众号字典类型
DELETE FROM system_dict_type WHERE type IN (
  'mp_auto_reply_request_match',
  'mp_message_type'
);

-- 2.2 删除公众号字典数据
DELETE FROM system_dict_data WHERE dict_type IN (
  'mp_auto_reply_request_match',
  'mp_message_type'
);

-- 2.3 删除商城交易字典
DELETE FROM system_dict_type WHERE type = 'trade_order_item_after_sale_status';
DELETE FROM system_dict_data WHERE dict_type = 'trade_order_item_after_sale_status';

-- ==================== 3. 清理业务数据表（可选） ====================
-- ⚠️ 以下操作会删除业务数据表，请确认不再需要后执行

-- 3.1 商品相关表
-- DROP TABLE IF EXISTS product_spu;
-- DROP TABLE IF EXISTS product_sku;
-- DROP TABLE IF EXISTS product_category;
-- DROP TABLE IF EXISTS product_brand;
-- DROP TABLE IF EXISTS product_property;
-- DROP TABLE IF EXISTS product_property_value;
-- DROP TABLE IF EXISTS product_comment;
-- DROP TABLE IF EXISTS product_favorite;
-- DROP TABLE IF EXISTS product_browse_history;

-- 3.2 交易相关表
-- DROP TABLE IF EXISTS trade_order;
-- DROP TABLE IF EXISTS trade_order_item;
-- DROP TABLE IF EXISTS trade_after_sale;
-- DROP TABLE IF EXISTS trade_delivery_express;
-- DROP TABLE IF EXISTS trade_delivery_express_template;
-- DROP TABLE IF EXISTS trade_delivery_pick_up_store;
-- DROP TABLE IF EXISTS trade_delivery_pick_up_order;
-- DROP TABLE IF EXISTS trade_brokerage_user;
-- DROP TABLE IF EXISTS trade_brokerage_record;
-- DROP TABLE IF EXISTS trade_brokerage_withdraw;
-- DROP TABLE IF EXISTS trade_config;

-- 3.3 营销相关表
-- DROP TABLE IF EXISTS promotion_coupon;
-- DROP TABLE IF EXISTS promotion_coupon_template;
-- DROP TABLE IF EXISTS promotion_coupon_take;
-- DROP TABLE IF EXISTS promotion_reward_activity;
-- DROP TABLE IF EXISTS promotion_discount_activity;
-- DROP TABLE IF EXISTS promotion_discount_product;
-- DROP TABLE IF EXISTS promotion_seckill_activity;
-- DROP TABLE IF EXISTS promotion_seckill_config;
-- DROP TABLE IF EXISTS promotion_combination_activity;
-- DROP TABLE IF EXISTS promotion_combination_product;
-- DROP TABLE IF EXISTS promotion_bargain_activity;
-- DROP TABLE IF EXISTS promotion_bargain_record;
-- DROP TABLE IF EXISTS promotion_bargain_help;
-- DROP TABLE IF EXISTS promotion_point_activity;
-- DROP TABLE IF EXISTS promotion_diy_template;
-- DROP TABLE IF EXISTS promotion_diy_page;
-- DROP TABLE IF EXISTS promotion_banner;
-- DROP TABLE IF EXISTS promotion_article;
-- DROP TABLE IF EXISTS promotion_article_category;
-- DROP TABLE IF EXISTS promotion_kefu_conversation;
-- DROP TABLE IF EXISTS promotion_kefu_message;

-- 3.4 公众号相关表
-- DROP TABLE IF EXISTS mp_account;
-- DROP TABLE IF EXISTS mp_menu;
-- DROP TABLE IF EXISTS mp_message;
-- DROP TABLE IF EXISTS mp_auto_reply;
-- DROP TABLE IF EXISTS mp_material;
-- DROP TABLE IF EXISTS mp_tag;
-- DROP TABLE IF EXISTS mp_follower;
-- DROP TABLE IF EXISTS mp_follower_tag;
-- DROP TABLE IF EXISTS mp_message_template;
-- DROP TABLE IF EXISTS mp_statistics;

-- 3.5 统计相关表
-- DROP TABLE IF EXISTS statistics_trade;
-- DROP TABLE IF EXISTS statistics_member;
-- DROP TABLE IF EXISTS statistics_product;

-- ==================== 完成 ====================
SELECT '商城与公众号数据清理完成' AS result;