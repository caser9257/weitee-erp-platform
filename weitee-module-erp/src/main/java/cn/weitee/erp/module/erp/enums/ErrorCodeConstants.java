package cn.weitee.erp.module.erp.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

/**
 * ERP 错误码枚举类
 * <p>
 * erp 系统，使用 1-030-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== ERP 供应商（1-030-100-000） ==========
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_030_100_000, "供应商不存在");
    ErrorCode SUPPLIER_NOT_ENABLE = new ErrorCode(1_030_100_001, "供应商({})未启用");
    ErrorCode SUPPLIER_BATCH_UPDATE_FIELD_NOT_SUPPORT = new ErrorCode(1_030_100_002, "批量修改字段【{}】不支持");
    ErrorCode SUPPLIER_BATCH_UPDATE_FIELD_VALUE_INVALID = new ErrorCode(1_030_100_003, "批量修改字段【{}】的值【{}】不合法");

    // ========== ERP 采购订单（1-030-101-000） ==========
    ErrorCode PURCHASE_ORDER_NOT_EXISTS = new ErrorCode(1_030_101_000, "采购订单不存在");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_030_101_001, "采购订单({})已审核，无法删除");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL = new ErrorCode(1_030_101_002, "反审核失败，只有已审核的采购订单才能反审核");
    ErrorCode PURCHASE_ORDER_APPROVE_FAIL = new ErrorCode(1_030_101_003, "审核失败，只有未审核的采购订单才能审核");
    ErrorCode PURCHASE_ORDER_NO_EXISTS = new ErrorCode(1_030_101_004, "生成采购单号失败，请重新提交");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_101_005, "采购订单({})已审核，无法修改");
    ErrorCode PURCHASE_ORDER_NOT_APPROVE = new ErrorCode(1_030_101_006, "采购订单未审核，无法操作");
    ErrorCode PURCHASE_ORDER_ITEM_IN_FAIL_PRODUCT_EXCEED = new ErrorCode(1_030_101_007, "采购订单项({})超过最大允许入库数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_IN = new ErrorCode(1_030_101_008, "反审核失败，已存在对应的采购入库单");
    ErrorCode PURCHASE_ORDER_ITEM_RETURN_FAIL_IN_EXCEED = new ErrorCode(1_030_101_009, "采购订单项({})超过最大允许退货数量({})");
    ErrorCode PURCHASE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_030_101_010, "反审核失败，已存在对应的采购退货单");
    ErrorCode PURCHASE_ORDER_BPM_SUBMIT_FAIL = new ErrorCode(1_030_101_011, "当前采购订单不允许提交审批");
    ErrorCode PURCHASE_ORDER_BPM_CANCEL_FAIL = new ErrorCode(1_030_101_012, "当前采购订单不存在可撤回的审批流程");
    ErrorCode PURCHASE_ORDER_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_101_013, "采购订单({})审批中，无法修改");
    ErrorCode PURCHASE_ORDER_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_101_014, "采购订单({})审批中，无法删除");
    ErrorCode PURCHASE_ORDER_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_101_017, "当前采购订单状态不允许执行该操作");
    ErrorCode PURCHASE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT = new ErrorCode(1_030_101_015, "批量修改字段【{}】不支持");
    ErrorCode PURCHASE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID = new ErrorCode(1_030_101_016, "批量修改字段【{}】的值【{}】不合法");

    // ========== ERP 采购入库（1-030-102-000） ==========
    ErrorCode PURCHASE_IN_NOT_EXISTS = new ErrorCode(1_030_102_000, "采购入库单不存在");
    ErrorCode PURCHASE_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_102_001, "采购入库单({})已审核，无法删除");
    ErrorCode PURCHASE_IN_PROCESS_FAIL = new ErrorCode(1_030_102_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode PURCHASE_IN_APPROVE_FAIL = new ErrorCode(1_030_102_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode PURCHASE_IN_NO_EXISTS = new ErrorCode(1_030_102_004, "生成入库单失败，请重新提交");
    ErrorCode PURCHASE_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_102_005, "采购入库单({})已审核，无法修改");
    ErrorCode PURCHASE_IN_NOT_APPROVE = new ErrorCode(1_030_102_006, "采购入库单未审核，无法操作");
    ErrorCode PURCHASE_IN_FAIL_PAYMENT_PRICE_EXCEED = new ErrorCode(1_030_102_007, "付款金额({})超过采购入库单总金额({})");
    ErrorCode PURCHASE_IN_PROCESS_FAIL_EXISTS_PAYMENT = new ErrorCode(1_030_102_008, "反审核失败，已存在对应的付款单");
    ErrorCode PURCHASE_IN_BPM_SUBMIT_FAIL = new ErrorCode(1_030_102_009, "当前采购入库单不允许提交审批");
    ErrorCode PURCHASE_IN_BPM_CANCEL_FAIL = new ErrorCode(1_030_102_010, "当前采购入库单不存在可撤回的审批流程");
    ErrorCode PURCHASE_IN_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_102_011, "采购入库单({})审批中，无法修改");
    ErrorCode PURCHASE_IN_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_102_012, "采购入库单({})审批中，无法删除");
    ErrorCode PURCHASE_IN_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_102_038, "当前采购入库单状态不允许执行该操作");
    ErrorCode PURCHASE_IN_QUALITY_CHECK_FAIL_STATUS = new ErrorCode(1_030_102_013, "当前采购入库单未审批通过，无法执行质检");
    ErrorCode PURCHASE_IN_QUALITY_CHECK_FAIL_QA_STATUS = new ErrorCode(1_030_102_014, "当前采购入库单已完成质检，无法重复质检");
    ErrorCode PURCHASE_IN_QUALITY_CHECK_FAIL_ITEMS = new ErrorCode(1_030_102_015, "采购入库单质检明细不匹配");
    ErrorCode PURCHASE_IN_QUALITY_CHECK_FAIL_COUNT = new ErrorCode(1_030_102_016, "采购入库明细({})质检数量必须等于到货数量({})");
    ErrorCode PURCHASE_IN_QUALITY_CHECK_FAIL_NEGATIVE = new ErrorCode(1_030_102_017, "采购入库明细({})质检数量不能小于 0");
    ErrorCode PURCHASE_IN_ITEM_COUNT_EXCEED_REMAINING = new ErrorCode(1_030_102_018, "采购入库明细({})超过订单剩余可入库数量({})");
    ErrorCode PURCHASE_IN_ITEM_ORDER_MISMATCH = new ErrorCode(1_030_102_019, "采购入库明细({})不属于当前采购订单");
    ErrorCode PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STATUS = new ErrorCode(1_030_102_020, "当前采购入库单未审批通过，无法执行入库确认");
    ErrorCode PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_QA_STATUS = new ErrorCode(1_030_102_021, "当前采购入库单质检结果不允许执行入库确认");
    ErrorCode PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_STOCK_STATUS = new ErrorCode(1_030_102_022, "当前采购入库单不处于待入库状态，无法重复确认入库");
    ErrorCode PURCHASE_IN_QUALITY_ORDER_NOT_EXISTS = new ErrorCode(1_030_102_023, "采购入库质检单不存在");
    ErrorCode PURCHASE_IN_QUALITY_ORDER_SUBMIT_FAIL_STATUS = new ErrorCode(1_030_102_024, "当前采购入库质检单状态不允许提交");
    ErrorCode PURCHASE_IN_QUALITY_ASSIGN_CHECKER_FAIL_STATUS = new ErrorCode(1_030_102_032, "当前采购入库质检单状态不允许指派质检人");
    ErrorCode PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_REQUIRED = new ErrorCode(1_030_102_033, "当前采购入库质检单尚未指派质检人");
    ErrorCode PURCHASE_IN_QUALITY_ASSIGNED_CHECKER_FORBIDDEN = new ErrorCode(1_030_102_034, "当前用户不是该质检单的被指派质检人");
    ErrorCode PURCHASE_IN_QUALITY_NO_REJECT_ITEMS = new ErrorCode(1_030_102_037, "质检单中没有不合格品，无法创建退货单");
    ErrorCode PURCHASE_IN_BATCH_UPDATE_FIELD_NOT_SUPPORT = new ErrorCode(1_030_102_035, "批量修改字段【{}】不支持");
    ErrorCode PURCHASE_IN_BATCH_UPDATE_FIELD_VALUE_INVALID = new ErrorCode(1_030_102_036, "批量修改字段【{}】的值【{}】不合法");

    // ========== ERP 采购退货（1-030-103-000） ==========
    ErrorCode PURCHASE_RETURN_NOT_EXISTS = new ErrorCode(1_030_103_000, "采购退货单不存在");
    ErrorCode PURCHASE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_103_001, "采购退货单({})已审核，无法删除");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL = new ErrorCode(1_030_103_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode PURCHASE_RETURN_APPROVE_FAIL = new ErrorCode(1_030_103_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode PURCHASE_RETURN_NO_EXISTS = new ErrorCode(1_030_103_004, "生成退货单失败，请重新提交");
    ErrorCode PURCHASE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_103_005, "采购退货单({})已审核，无法修改");
    ErrorCode PURCHASE_RETURN_NOT_APPROVE = new ErrorCode(1_030_103_006, "采购退货单未审核，无法操作");
    ErrorCode PURCHASE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_030_103_007, "退款金额({})超过采购退货单总金额({})");
    ErrorCode PURCHASE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_030_103_008, "反审核失败，已存在对应的退款单");
    ErrorCode PURCHASE_RETURN_BPM_SUBMIT_FAIL = new ErrorCode(1_030_103_009, "当前采购退货单不允许提交审批");
    ErrorCode PURCHASE_RETURN_BPM_CANCEL_FAIL = new ErrorCode(1_030_103_010, "当前采购退货单不存在可撤回的审批流程");
    ErrorCode PURCHASE_RETURN_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_103_011, "采购退货单({})审批中，无法修改");
    ErrorCode PURCHASE_RETURN_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_103_012, "采购退货单({})审批中，无法删除");
    ErrorCode PURCHASE_RETURN_MANUAL_STATUS_UPDATE_FORBIDDEN = new ErrorCode(1_030_103_013, "采购退货单({})已接入 BPM 审批，不允许手工更新状态");
    ErrorCode PURCHASE_RETURN_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_103_014, "当前采购退货单状态不允许执行该操作");

    // ========== ERP 采购来源批次（1-030-104-000） ==========
    ErrorCode PURCHASE_SOURCE_BATCH_NOT_EXISTS = new ErrorCode(1_030_104_000, "采购来源批次不存在");
    ErrorCode PURCHASE_SOURCE_BATCH_NO_EXISTS = new ErrorCode(1_030_104_001, "采购来源批次号已存在");
    ErrorCode PURCHASE_SOURCE_BATCH_ORDER_ITEM_NOT_EXISTS = new ErrorCode(1_030_104_002, "采购订单明细不存在");
    ErrorCode PURCHASE_SOURCE_BATCH_UPDATE_FAIL_CLOSED = new ErrorCode(1_030_104_003, "采购来源批次已关闭，无法修改");
    ErrorCode PURCHASE_SOURCE_BATCH_ORDER_ITEM_MISMATCH = new ErrorCode(1_030_104_004, "采购来源批次与采购订单明细不匹配");
    ErrorCode PURCHASE_SOURCE_BATCH_PRODUCT_MISMATCH = new ErrorCode(1_030_104_005, "采购来源批次与产品不匹配");

    // ========== ERP 客户（1-030-200-000）==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_020_200_000, "客户不存在");
    ErrorCode CUSTOMER_NOT_ENABLE = new ErrorCode(1_020_200_001, "客户({})未启用");

    // ========== ERP 销售订单（1-030-201-000） ==========
    ErrorCode SALE_ORDER_NOT_EXISTS = new ErrorCode(1_020_201_000, "销售订单不存在");
    ErrorCode SALE_ORDER_DELETE_FAIL_APPROVE = new ErrorCode(1_020_201_001, "销售订单({})已审核，无法删除");
    ErrorCode SALE_ORDER_PROCESS_FAIL = new ErrorCode(1_020_201_002, "反审核失败，只有已审核的销售订单才能反审核");
    ErrorCode SALE_ORDER_APPROVE_FAIL = new ErrorCode(1_020_201_003, "审核失败，只有未审核的销售订单才能审核");
    ErrorCode SALE_ORDER_NO_EXISTS = new ErrorCode(1_020_201_004, "生成销售单号失败，请重新提交");
    ErrorCode SALE_ORDER_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_201_005, "销售订单({})已审核，无法修改");
    ErrorCode SALE_ORDER_NOT_APPROVE = new ErrorCode(1_020_201_006, "销售订单未审核，无法操作");
    ErrorCode SALE_ORDER_ITEM_OUT_FAIL_PRODUCT_EXCEED = new ErrorCode(1_020_201_007, "销售订单项({})超过最大允许出库数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_OUT = new ErrorCode(1_020_201_008, "反审核失败，已存在对应的销售出库单");
    ErrorCode SALE_ORDER_ITEM_RETURN_FAIL_OUT_EXCEED = new ErrorCode(1_020_201_009, "销售订单项({})超过最大允许退货数量({})");
    ErrorCode SALE_ORDER_PROCESS_FAIL_EXISTS_RETURN = new ErrorCode(1_020_201_010, "反审核失败，已存在对应的销售退货单");
    ErrorCode SALE_ORDER_BATCH_UPDATE_FIELD_NOT_SUPPORT = new ErrorCode(1_020_201_011, "批量修改字段【{}】不支持");
    ErrorCode SALE_ORDER_BATCH_UPDATE_FIELD_VALUE_INVALID = new ErrorCode(1_020_201_012, "批量修改字段【{}】的值【{}】不合法");

    // ========== ERP 发货放行（1-020-201-100） ==========
    ErrorCode SHIPMENT_RELEASE_ORDER_NOT_EXISTS = new ErrorCode(1_020_201_100, "发货放行订单不存在");
    ErrorCode SHIPMENT_RELEASE_STATUS_INVALID = new ErrorCode(1_020_201_101, "当前发货放行状态不允许执行该操作");

    // ========== ERP 销售出库（1-030-202-000） ==========
    ErrorCode SALE_OUT_NOT_EXISTS = new ErrorCode(1_020_202_000, "销售出库单不存在");
    ErrorCode SALE_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_020_202_001, "销售出库单({})已审核，无法删除");
    ErrorCode SALE_OUT_PROCESS_FAIL = new ErrorCode(1_020_202_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode SALE_OUT_APPROVE_FAIL = new ErrorCode(1_020_202_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode SALE_OUT_NO_EXISTS = new ErrorCode(1_020_202_004, "生成出库单失败，请重新提交");
    ErrorCode SALE_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_202_005, "销售出库单({})已审核，无法修改");
    ErrorCode SALE_OUT_NOT_APPROVE = new ErrorCode(1_020_202_006, "销售出库单未审核，无法操作");
    ErrorCode SALE_OUT_FAIL_RECEIPT_PRICE_EXCEED = new ErrorCode(1_020_202_007, "收款金额({})超过销售出库单总金额({})");
    ErrorCode SALE_OUT_PROCESS_FAIL_EXISTS_RECEIPT = new ErrorCode(1_020_202_008, "反审核失败，已存在对应的收款单");
    ErrorCode SALE_OUT_ALREADY_EXISTS = new ErrorCode(1_020_202_009, "该订单已存在出库单，无法重复创建");
    ErrorCode SHIPMENT_RELEASE_NOT_RELEASED = new ErrorCode(1_020_202_010, "该订单尚未放行，无法创建出库单");
    ErrorCode SALE_ORDER_ITEM_ALL_OUTED = new ErrorCode(1_020_202_011, "该订单所有产品已全部出库");
    ErrorCode SALE_ORDER_ITEM_NOT_EXISTS = new ErrorCode(1_020_202_012, "销售订单项不存在");

    // ========== ERP 销售退货（1-030-203-000） ==========
    ErrorCode SALE_RETURN_NOT_EXISTS = new ErrorCode(1_020_203_000, "销售退货单不存在");
    ErrorCode SALE_RETURN_DELETE_FAIL_APPROVE = new ErrorCode(1_020_203_001, "销售退货单({})已审核，无法删除");
    ErrorCode SALE_RETURN_PROCESS_FAIL = new ErrorCode(1_020_203_002, "反审核失败，只有已审核的退货单才能反审核");
    ErrorCode SALE_RETURN_APPROVE_FAIL = new ErrorCode(1_020_203_003, "审核失败，只有未审核的退货单才能审核");
    ErrorCode SALE_RETURN_NO_EXISTS = new ErrorCode(1_020_203_004, "生成退货单失败，请重新提交");
    ErrorCode SALE_RETURN_UPDATE_FAIL_APPROVE = new ErrorCode(1_020_203_005, "销售退货单({})已审核，无法修改");
    ErrorCode SALE_RETURN_NOT_APPROVE = new ErrorCode(1_020_203_006, "销售退货单未审核，无法操作");
    ErrorCode SALE_RETURN_FAIL_REFUND_PRICE_EXCEED = new ErrorCode(1_020_203_007, "退款金额({})超过销售退货单总金额({})");
    ErrorCode SALE_RETURN_PROCESS_FAIL_EXISTS_REFUND = new ErrorCode(1_020_203_008, "反审核失败，已存在对应的退款单");

    // ========== ERP 项目（1-020-204-000）==========
    ErrorCode PROJECT_NOT_EXISTS = new ErrorCode(1_020_204_000, "项目不存在");
    ErrorCode PROJECT_NOT_ENABLE = new ErrorCode(1_020_204_001, "项目({})未启用");
    ErrorCode PROJECT_NO_EXISTS = new ErrorCode(1_020_204_002, "项目编号已存在");
    ErrorCode PROJECT_DELETE_FAIL_EXISTS_SALE_ORDER = new ErrorCode(1_020_204_003, "项目({})已被销售订单引用，无法删除");

    // ========== ERP 仓库 1-030-400-000 ==========
    ErrorCode WAREHOUSE_NOT_EXISTS = new ErrorCode(1_030_400_000, "仓库不存在");
    ErrorCode WAREHOUSE_NOT_ENABLE = new ErrorCode(1_030_400_001, "仓库({})未启用");
    ErrorCode WAREHOUSE_BATCH_UPDATE_FIELD_NOT_SUPPORT = new ErrorCode(1_030_400_002, "批量修改字段【{}】不支持");
    ErrorCode WAREHOUSE_BATCH_UPDATE_FIELD_VALUE_INVALID = new ErrorCode(1_030_400_003, "批量修改字段【{}】的值【{}】不合法");
    ErrorCode WAREHOUSE_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_400_004, "仓库分类不存在");
    ErrorCode WAREHOUSE_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_030_400_005, "已存在该分类名称的仓库分类");
    ErrorCode WAREHOUSE_CATEGORY_EXITS_WAREHOUSE = new ErrorCode(1_030_400_006, "存在仓库使用该分类，无法删除");
    ErrorCode WAREHOUSE_CATEGORY_EXITS_CHILDREN = new ErrorCode(1_030_400_007, "存在子仓库分类，无法删除");
    ErrorCode WAREHOUSE_CATEGORY_PARENT_NOT_EXISTS = new ErrorCode(1_030_400_008, "父级仓库分类不存在");
    ErrorCode WAREHOUSE_CATEGORY_PARENT_ERROR = new ErrorCode(1_030_400_009, "不能设置自己为父仓库分类");
    ErrorCode WAREHOUSE_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_030_400_010, "不能设置自己的子分类为父分类");
    ErrorCode WAREHOUSE_CATEGORY_CODE_DUPLICATE = new ErrorCode(1_030_400_011, "已存在该分类编码的仓库分类");

    // ========== ERP 其它入库单 1-030-401-000 ==========
    ErrorCode STOCK_IN_NOT_EXISTS = new ErrorCode(1_030_401_000, "其它入库单不存在");
    ErrorCode STOCK_IN_DELETE_FAIL_APPROVE = new ErrorCode(1_030_401_001, "其它入库单({})已审核，无法删除");
    ErrorCode STOCK_IN_PROCESS_FAIL = new ErrorCode(1_030_401_002, "反审核失败，只有已审核的入库单才能反审核");
    ErrorCode STOCK_IN_APPROVE_FAIL = new ErrorCode(1_030_401_003, "审核失败，只有未审核的入库单才能审核");
    ErrorCode STOCK_IN_NO_EXISTS = new ErrorCode(1_030_401_004, "生成入库单失败，请重新提交");
    ErrorCode STOCK_IN_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_401_005, "其它入库单({})已审核，无法修改");
    ErrorCode STOCK_IN_BPM_SUBMIT_FAIL = new ErrorCode(1_030_401_006, "当前其它入库单不允许提交审批");
    ErrorCode STOCK_IN_BPM_CANCEL_FAIL = new ErrorCode(1_030_401_007, "当前其它入库单不存在可撤回的审批流程");
    ErrorCode STOCK_IN_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_401_008, "其它入库单({})审批中，无法修改");
    ErrorCode STOCK_IN_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_401_009, "其它入库单({})审批中，无法删除");
    ErrorCode STOCK_IN_MANUAL_STATUS_UPDATE_FORBIDDEN = new ErrorCode(1_030_401_010, "其它入库单({})已接入 BPM 审批，不允许手工更新状态");
    ErrorCode STOCK_IN_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_401_011, "当前其它入库单状态不允许执行该操作");

    // ========== ERP 其它出库单 1-030-402-000 ==========
    ErrorCode STOCK_OUT_NOT_EXISTS = new ErrorCode(1_030_402_000, "其它出库单不存在");
    ErrorCode STOCK_OUT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_402_001, "其它出库单({})已审核，无法删除");
    ErrorCode STOCK_OUT_PROCESS_FAIL = new ErrorCode(1_030_402_002, "反审核失败，只有已审核的出库单才能反审核");
    ErrorCode STOCK_OUT_APPROVE_FAIL = new ErrorCode(1_030_402_003, "审核失败，只有未审核的出库单才能审核");
    ErrorCode STOCK_OUT_NO_EXISTS = new ErrorCode(1_030_402_004, "生成出库单失败，请重新提交");
    ErrorCode STOCK_OUT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_402_005, "其它出库单({})已审核，无法修改");
    ErrorCode STOCK_OUT_BPM_SUBMIT_FAIL = new ErrorCode(1_030_402_006, "当前其它出库单不允许提交审批");
    ErrorCode STOCK_OUT_BPM_CANCEL_FAIL = new ErrorCode(1_030_402_007, "当前其它出库单不存在可撤回的审批流程");
    ErrorCode STOCK_OUT_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_402_008, "其它出库单({})审批中，无法修改");
    ErrorCode STOCK_OUT_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_402_009, "其它出库单({})审批中，无法删除");
    ErrorCode STOCK_OUT_MANUAL_STATUS_UPDATE_FORBIDDEN = new ErrorCode(1_030_402_010, "其它出库单({})已接入 BPM 审批，不允许手工更新状态");
    ErrorCode STOCK_OUT_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_402_011, "当前其它出库单状态不允许执行该操作");

    // ========== ERP 组装拆卸单 1-030-403-000 ==========
    ErrorCode STOCK_ASSEMBLE_NOT_EXISTS = new ErrorCode(1_030_403_000, "组装拆卸单不存在");
    ErrorCode STOCK_ASSEMBLE_NO_EXISTS = new ErrorCode(1_030_403_001, "生成组装拆卸单失败，请重新提交");
    ErrorCode STOCK_ASSEMBLE_ACTION_TYPE_INVALID = new ErrorCode(1_030_403_002, "组装拆卸类型不合法");
    ErrorCode STOCK_ASSEMBLE_BOM_NOT_EXISTS = new ErrorCode(1_030_403_003, "当前产品没有启用的 BOM");
    ErrorCode STOCK_ASSEMBLE_BOM_ITEM_INVALID = new ErrorCode(1_030_403_004, "BOM 明细不合法，无法生成组装拆卸单");
    ErrorCode STOCK_ASSEMBLE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_403_005, "组装拆卸单({})已审核，无法修改");
    ErrorCode STOCK_ASSEMBLE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_403_006, "组装拆卸单({})已审核，无法删除");
    ErrorCode STOCK_ASSEMBLE_APPROVE_FAIL = new ErrorCode(1_030_403_007, "组装拆卸单当前状态不允许审核");
    ErrorCode STOCK_ASSEMBLE_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_403_008, "组装拆卸单状态不允许执行该操作");

    // ========== ERP 库存调拨单 1-030-403-000 ==========
    ErrorCode STOCK_MOVE_NOT_EXISTS = new ErrorCode(1_030_403_000, "库存调拨单不存在");
    ErrorCode STOCK_MOVE_DELETE_FAIL_APPROVE = new ErrorCode(1_030_403_001, "库存调拨单({})已审核，无法删除");
    ErrorCode STOCK_MOVE_PROCESS_FAIL = new ErrorCode(1_030_403_002, "反审核失败，只有已审核的调拨单才能反审核");
    ErrorCode STOCK_MOVE_APPROVE_FAIL = new ErrorCode(1_030_403_003, "审核失败，只有未审核的调拨单才能审核");
    ErrorCode STOCK_MOVE_NO_EXISTS = new ErrorCode(1_030_403_004, "生成调拨号失败，请重新提交");
    ErrorCode STOCK_MOVE_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_403_005, "库存调拨单({})已审核，无法修改");

    // ========== ERP 库存盘点单 1-030-405-000 ==========
    ErrorCode STOCK_CHECK_NOT_EXISTS = new ErrorCode(1_030_405_000, "库存盘点单不存在");
    ErrorCode STOCK_CHECK_DELETE_FAIL_APPROVE = new ErrorCode(1_030_405_001, "库存盘点单({})已审核，无法删除");
    ErrorCode STOCK_CHECK_PROCESS_FAIL = new ErrorCode(1_030_405_002, "反审核失败，只有已审核的盘点单才能反审核");
    ErrorCode STOCK_CHECK_APPROVE_FAIL = new ErrorCode(1_030_405_003, "审核失败，只有未审核的盘点单才能审核");
    ErrorCode STOCK_CHECK_NO_EXISTS = new ErrorCode(1_030_405_004, "生成盘点号失败，请重新提交");
    ErrorCode STOCK_CHECK_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_405_005, "库存盘点单({})已审核，无法修改");
    ErrorCode STOCK_CHECK_STATUS_TRANSITION_FAIL = new ErrorCode(1_030_405_006, "盘点单状态流转失败，当前状态({})不允许流转到目标状态({})");
    ErrorCode STOCK_CHECK_SNAPSHOT_FAIL = new ErrorCode(1_030_405_007, "盘点单快照生成失败");
    ErrorCode STOCK_CHECK_VOUCHER_GENERATE_FAIL = new ErrorCode(1_030_405_008, "盘点凭证生成失败");
    ErrorCode STOCK_CHECK_VOUCHER_VOID_FAIL = new ErrorCode(1_030_405_009, "盘点凭证作废失败");
    ErrorCode STOCK_CHECK_CLOSE_FAIL_VOUCHER_MISSING = new ErrorCode(1_030_405_010, "盘点单关闭失败，凭证未生成");
    ErrorCode STOCK_CHECK_YEAR_END_NOT_CLOSED = new ErrorCode(1_030_405_011, "年末盘点单({})未完成，无法进行年结");

    // ========== ERP 产品库存 1-030-404-000 ==========
    ErrorCode STOCK_COUNT_NEGATIVE = new ErrorCode(1_030_404_000, "操作失败，产品({})所在仓库({})的库存：{}，小于变更数量：{}");
    ErrorCode STOCK_COUNT_NEGATIVE2 = new ErrorCode(1_030_404_001, "操作失败，产品({})所在仓库({})的库存不足");
    ErrorCode STOCK_BATCH_NOT_EXISTS = new ErrorCode(1_030_404_002, "批次库存不存在");
    ErrorCode STOCK_BATCH_INSUFFICIENT = new ErrorCode(1_030_404_003, "批次({})库存不足，当前可用量：{}");
    ErrorCode STOCK_BATCH_ALLOCATION_INSUFFICIENT = new ErrorCode(1_030_404_004, "业务单据({})批次库存不足，需出库：{}，当前可用：{}");
    ErrorCode STOCK_BATCH_ALLOCATION_EXISTS = new ErrorCode(1_030_404_005, "业务单据({})已存在批次分配明细");
    ErrorCode STOCK_BATCH_ADJUSTMENT_NO_EXISTS = new ErrorCode(1_030_404_006, "批次调整单号({})已存在");
    ErrorCode STOCK_BATCH_REBUILD_UNSUPPORTED_BIZ_TYPE = new ErrorCode(1_030_404_007, "批次重建暂不支持该业务类型");
    ErrorCode STOCK_BATCH_REBUILD_CONFIRM_REQUIRED = new ErrorCode(1_030_404_008, "执行批次重建必须显式确认");
    ErrorCode STOCK_BATCH_REBUILD_HAS_FAILED_DETAIL = new ErrorCode(1_030_404_009, "存在不可重建明细，请先执行预览并处理失败项");
    ErrorCode STOCK_BATCH_RESERVATION_EXISTS = new ErrorCode(1_030_404_010, "业务单据({})已存在批次预占明细");
    ErrorCode STOCK_BATCH_RESERVATION_INSUFFICIENT = new ErrorCode(1_030_404_011, "业务单据({})批次可预占库存不足，需预占：{}，当前可用：{}");
    ErrorCode STOCK_BATCH_LOCK_INSUFFICIENT = new ErrorCode(1_030_404_012, "批次({})可锁定库存不足，当前可用量：{}");
    ErrorCode STOCK_BATCH_LOCKED_INSUFFICIENT = new ErrorCode(1_030_404_013, "批次({})已锁定库存不足，当前锁定量：{}");

    // ========== ERP 产品 1-030-500-000 ==========
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_030_500_000, "产品不存在");
    ErrorCode PRODUCT_NOT_ENABLE = new ErrorCode(1_030_500_001, "产品({})未启用");

    // ========== ERP 产品分类 1-030-501-000 ==========
    ErrorCode PRODUCT_CATEGORY_NOT_EXISTS = new ErrorCode(1_030_501_000, "产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_EXITS_CHILDREN = new ErrorCode(1_030_501_001, "存在存在子产品分类，无法删除");
    ErrorCode PRODUCT_CATEGORY_PARENT_NOT_EXITS = new ErrorCode(1_030_501_002,"父级产品分类不存在");
    ErrorCode PRODUCT_CATEGORY_PARENT_ERROR = new ErrorCode(1_030_501_003, "不能设置自己为父产品分类");
    ErrorCode PRODUCT_CATEGORY_NAME_DUPLICATE = new ErrorCode(1_030_501_004, "已经存在该分类名称的产品分类");
    ErrorCode PRODUCT_CATEGORY_PARENT_IS_CHILD = new ErrorCode(1_030_501_005, "不能设置自己的子分类为父分类");
    ErrorCode PRODUCT_CATEGORY_EXITS_PRODUCT = new ErrorCode(1_030_501_006, "存在产品使用该分类，无法删除");

    // ========== ERP 产品单位 1-030-502-000 ==========
    ErrorCode PRODUCT_UNIT_NOT_EXISTS = new ErrorCode(1_030_502_000, "产品单位不存在");
    ErrorCode PRODUCT_UNIT_NAME_DUPLICATE = new ErrorCode(1_030_502_001, "已存在该名字的产品单位");
    ErrorCode PRODUCT_UNIT_EXITS_PRODUCT = new ErrorCode(1_030_502_002, "存在产品使用该单位，无法删除");

    // ========== ERP 结算账户 1-030-600-000 ==========
    ErrorCode ACCOUNT_NOT_EXISTS = new ErrorCode(1_030_600_000, "结算账户不存在");
    ErrorCode ACCOUNT_NOT_ENABLE = new ErrorCode(1_030_600_001, "结算账户({})未启用");

    // ========== ERP 付款单 1-030-601-000 ==========
    ErrorCode FINANCE_PAYMENT_NOT_EXISTS = new ErrorCode(1_030_601_000, "付款单不存在");
    ErrorCode FINANCE_PAYMENT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_601_001, "付款单({})已审核，无法删除");
    ErrorCode FINANCE_PAYMENT_PROCESS_FAIL = new ErrorCode(1_030_601_002, "反审核失败，只有已审核的付款单才能反审核");
    ErrorCode FINANCE_PAYMENT_APPROVE_FAIL = new ErrorCode(1_030_601_003, "审核失败，只有未审核的付款单才能审核");
    ErrorCode FINANCE_PAYMENT_NO_EXISTS = new ErrorCode(1_030_601_004, "生成付款单号失败，请重新提交");
    ErrorCode FINANCE_PAYMENT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_601_005, "付款单({})已审核，无法修改");
    ErrorCode FINANCE_PAYMENT_UPDATE_FAIL_PROCESSING = new ErrorCode(1_030_601_006, "付款单({})审批中，无法修改");
    ErrorCode FINANCE_PAYMENT_DELETE_FAIL_PROCESSING = new ErrorCode(1_030_601_007, "付款单({})审批中，无法删除");
    ErrorCode FINANCE_PAYMENT_BPM_SUBMIT_FAIL = new ErrorCode(1_030_601_008, "当前付款单不允许提交审批");
    ErrorCode FINANCE_PAYMENT_BPM_CANCEL_FAIL = new ErrorCode(1_030_601_009, "当前付款单不存在可撤回的审批流程");
    ErrorCode FINANCE_PAYMENT_VOID_FAIL = new ErrorCode(1_030_601_010, "作废失败，只有已审核的付款单才能作废");
    ErrorCode FINANCE_PAYMENT_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_030_601_011, "当前付款单状态不允许执行该操作");
    ErrorCode FINANCE_PAYMENT_MANUAL_STATUS_DISABLED = new ErrorCode(1_030_601_012, "付款单已接入审批流，禁止手工变更审核状态");

    // ========== ERP 预付款单 1-030-605-000 ==========
    ErrorCode PREPAYMENT_NOT_EXISTS = new ErrorCode(1_030_605_000, "预付款单不存在");
    ErrorCode PREPAYMENT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_605_001, "预付款单({})已审核，无法删除");
    ErrorCode PREPAYMENT_PROCESS_FAIL = new ErrorCode(1_030_605_002, "反审核失败，只有已审核的预付款单才能反审核");
    ErrorCode PREPAYMENT_APPROVE_FAIL = new ErrorCode(1_030_605_003, "审核失败，只有未审核的预付款单才能审核");
    ErrorCode PREPAYMENT_NO_EXISTS = new ErrorCode(1_030_605_004, "生成预付款单号失败，请重新提交");
    ErrorCode PREPAYMENT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_605_005, "预付款单({})已审核，无法修改");
    ErrorCode PREPAYMENT_ALLOCATE_FAIL_APPROVE = new ErrorCode(1_030_605_006, "预付款单({})未审核，无法核销");
    ErrorCode PREPAYMENT_ALLOCATE_AMOUNT_EXCEED = new ErrorCode(1_030_605_007, "预付款单({})本次核销金额({})超过剩余可核销金额({})");

    // ========== ERP 采购发票三单匹配 1-030-606-000 ==========
    ErrorCode AP_INVOICE_NOT_EXISTS = new ErrorCode(1_030_606_000, "采购发票不存在");
    ErrorCode AP_INVOICE_EXISTS = new ErrorCode(1_030_606_001, "供应商下已存在相同发票号({})");
    ErrorCode AP_INVOICE_UPDATE_FAIL_MATCHED = new ErrorCode(1_030_606_002, "采购发票({})已存在匹配记录，无法直接修改，请先撤销匹配");
    ErrorCode AP_INVOICE_MATCH_ITEMS_EMPTY = new ErrorCode(1_030_606_003, "匹配明细不能为空");
    ErrorCode AP_INVOICE_MATCH_COUNT_INVALID = new ErrorCode(1_030_606_004, "采购入库明细({})本次匹配数量必须大于 0");
    ErrorCode AP_INVOICE_MATCH_AMOUNT_INVALID = new ErrorCode(1_030_606_005, "采购入库明细({})本次匹配金额必须大于 0");
    ErrorCode AP_INVOICE_MATCH_SUPPLIER_MISMATCH = new ErrorCode(1_030_606_006, "采购入库单({})与发票供应商不一致");
    ErrorCode AP_INVOICE_MATCH_STATEMENT_NOT_EXISTS = new ErrorCode(1_030_606_007, "采购入库单({})未生成应付台账，无法匹配");
    ErrorCode AP_INVOICE_MATCH_COUNT_EXCEED = new ErrorCode(1_030_606_008, "采购入库明细({})本次匹配数量({})超过剩余可匹配数量({})");
    ErrorCode AP_INVOICE_MATCH_AMOUNT_EXCEED = new ErrorCode(1_030_606_009, "采购入库明细({})本次匹配金额({})超过剩余可匹配金额({})");
    ErrorCode AP_INVOICE_MATCH_TOTAL_COUNT_EXCEED = new ErrorCode(1_030_606_010, "采购发票({})匹配数量({})超过发票总数量({})");
    ErrorCode AP_INVOICE_MATCH_TOTAL_AMOUNT_EXCEED = new ErrorCode(1_030_606_011, "采购发票({})匹配金额({})超过发票总金额({})和尾差容忍({})");
    ErrorCode AP_INVOICE_MATCH_ITEM_NOT_EXISTS = new ErrorCode(1_030_606_012, "发票匹配明细不存在");

    // ========== ERP 应付台账 1-030-603-000 ==========
    ErrorCode AP_STATEMENT_NOT_EXISTS = new ErrorCode(1_030_603_000, "应付台账不存在");
    ErrorCode AP_STATEMENT_SUPPLIER_NOT_MATCH = new ErrorCode(1_030_603_001, "应付台账({})与付款单供应商不一致");
    ErrorCode AP_STATEMENT_CLOSED = new ErrorCode(1_030_603_002, "应付台账({})已关闭，无法继续核销");
    ErrorCode AP_STATEMENT_ALLOCATE_AMOUNT_EXCEED = new ErrorCode(1_030_603_003, "应付台账({})本次核销金额({})超过剩余可核销金额({})");
    ErrorCode AP_STATEMENT_HAS_APPROVED_ALLOCATE = new ErrorCode(1_030_603_004, "应付台账({})存在已生效核销记录，无法反审核");
    ErrorCode AP_STATEMENT_ALLOCATE_AMOUNT_INVALID = new ErrorCode(1_030_603_005, "应付台账({})本次核销金额必须大于 0");

    // ========== ERP 暂估单 1-030-604-000 ==========
    ErrorCode AP_ESTIMATE_NOT_EXISTS = new ErrorCode(1_030_604_000, "暂估单不存在");
    ErrorCode AP_ESTIMATE_NO_EXISTS = new ErrorCode(1_030_604_001, "生成暂估单号失败，请重新提交");
    ErrorCode AP_ESTIMATE_CONFIRM_FAIL_STATUS = new ErrorCode(1_030_604_002, "暂估单({})已确认或已冲回，无法确认");
    ErrorCode AP_ESTIMATE_SOURCE_EXISTS = new ErrorCode(1_030_604_003, "来源入库单({})已存在暂估记录");

    // ========== ERP 收款单 1-030-602-000 ==========
    ErrorCode FINANCE_RECEIPT_NOT_EXISTS = new ErrorCode(1_030_602_000, "收款单不存在");
    ErrorCode FINANCE_RECEIPT_DELETE_FAIL_APPROVE = new ErrorCode(1_030_602_001, "收款单({})已审核，无法删除");
    ErrorCode FINANCE_RECEIPT_PROCESS_FAIL = new ErrorCode(1_030_602_002, "反审核失败，只有已审核的收款单才能反审核");
    ErrorCode FINANCE_RECEIPT_APPROVE_FAIL = new ErrorCode(1_030_602_003, "审核失败，只有未审核的收款单才能审核");
    ErrorCode FINANCE_RECEIPT_NO_EXISTS = new ErrorCode(1_030_602_004, "生成收款单号失败，请重新提交");
    ErrorCode FINANCE_RECEIPT_UPDATE_FAIL_APPROVE = new ErrorCode(1_030_602_005, "收款单({})已审核，无法修改");

    // ========== ERP 轻量 MRP 1-030-700-000 ==========
    ErrorCode BOM_NOT_EXISTS = new ErrorCode(1_030_700_000, "BOM 不存在");
    ErrorCode MATERIAL_PLAN_RULE_NOT_EXISTS = new ErrorCode(1_030_700_001, "物料计划规则不存在");
    ErrorCode MRP_PLAN_NOT_EXISTS = new ErrorCode(1_030_700_002, "MRP 计划不存在");
    ErrorCode PURCHASE_SUGGEST_NOT_EXISTS = new ErrorCode(1_030_700_003, "采购建议不存在");
    ErrorCode PRODUCTION_SUGGEST_NOT_EXISTS = new ErrorCode(1_030_700_004, "生产建议不存在");
    ErrorCode PRODUCTION_ORDER_NOT_EXISTS = new ErrorCode(1_030_700_005, "生产工单不存在");
    ErrorCode MRP_PLAN_DATE_INVALID = new ErrorCode(1_030_700_006, "MRP 计划结束日期不能早于开始日期");
    ErrorCode PRODUCTION_ORDER_STATUS_INVALID = new ErrorCode(1_030_700_007, "当前工单状态不允许执行该操作");
    ErrorCode MRP_SUGGEST_STATUS_INVALID = new ErrorCode(1_030_700_008, "当前建议单状态不允许执行该操作");
    ErrorCode MRP_BOM_CYCLE = new ErrorCode(1_030_700_009, "BOM 检测到循环引用");
    ErrorCode BOM_ITEM_EMPTY = new ErrorCode(1_030_700_010, "BOM 子件不能为空");
    ErrorCode PRODUCTION_ORDER_EFFECTIVE_BOM_NOT_EXISTS = new ErrorCode(1_030_700_011, "当前生产工单缺少有效 BOM，无法下达");
    ErrorCode BOM_STATUS_INVALID = new ErrorCode(1_030_700_012, "BOM 状态不合法");
    ErrorCode RD_BOM_NOT_EXISTS = new ErrorCode(1_030_700_013, "研发 BOM 不存在");
    ErrorCode MRP_NETTING_POLICY_NOT_EXISTS = new ErrorCode(1_030_700_014, "净需求策略不存在");
    ErrorCode MRP_NETTING_POLICY_DEFAULT_DELETE_FORBIDDEN = new ErrorCode(1_030_700_015, "默认净需求策略不允许删除");
    ErrorCode MRP_PLAN_STATUS_INVALID = new ErrorCode(1_030_700_016, "当前 MRP 计划状态不允许执行该操作");
    ErrorCode PURCHASE_IN_STOCK_BATCH_REQUIRED = new ErrorCode(1_030_700_017, "批次管理物料必须录入批次");
    ErrorCode PURCHASE_IN_STOCK_BATCH_COUNT_MISMATCH = new ErrorCode(1_030_700_018, "入库批次数量合计必须等于执行入库数量");
    ErrorCode PURCHASE_IN_CONFIRM_STOCK_IN_FAIL_BATCH_REQUIRED = new ErrorCode(1_030_700_019, "存在批次管理物料，请通过执行入库录入批次后再确认");
    ErrorCode PRODUCTION_MATERIAL_NOT_EXISTS = new ErrorCode(1_030_700_020, "生产工单物料不存在");
    ErrorCode PRODUCTION_MATERIAL_ORDER_MISMATCH = new ErrorCode(1_030_700_021, "生产工单物料与工单不匹配");
    ErrorCode PRODUCTION_MATERIAL_QTY_INVALID = new ErrorCode(1_030_700_022, "工单物料数量不合法");
    ErrorCode PRODUCTION_ISSUE_BATCH_COUNT_MISMATCH = new ErrorCode(1_030_700_023, "领料批次数量合计必须等于领料数量");
    ErrorCode PRODUCTION_RETURN_BATCH_COUNT_MISMATCH = new ErrorCode(1_030_700_024, "退料批次数量合计必须等于退料数量");
    ErrorCode PRODUCTION_RETURN_BATCH_INVALID = new ErrorCode(1_030_700_025, "退料批次无效或可退数量不足");
    ErrorCode PRODUCTION_COST_ENTRY_NOT_EXISTS = new ErrorCode(1_030_700_026, "生产成本归集明细不存在");
    ErrorCode PRODUCTION_COST_TYPE_INVALID = new ErrorCode(1_030_700_027, "生产成本类型不合法");
    ErrorCode PRODUCTION_COST_AMOUNT_INVALID = new ErrorCode(1_030_700_028, "生产成本金额必须大于 0");
    ErrorCode OUTSOURCE_ORDER_NOT_EXISTS = new ErrorCode(1_030_700_029, "委外订单不存在");
    ErrorCode OUTSOURCE_ORDER_STATUS_INVALID = new ErrorCode(1_030_700_030, "当前委外订单状态不允许执行该操作");
    ErrorCode OUTSOURCE_BATCH_COUNT_MISMATCH = new ErrorCode(1_030_700_031, "委外批次数量合计必须等于单据数量");
    ErrorCode OUTSOURCE_BATCH_INVALID = new ErrorCode(1_030_700_032, "委外批次无效或数量不足");
    ErrorCode OUTSOURCE_FEE_AMOUNT_INVALID = new ErrorCode(1_030_700_033, "委外加工费必须大于 0");
    ErrorCode OUTSOURCE_ISSUE_TYPE_INVALID = new ErrorCode(1_030_700_034, "委外发料类型不合法");
    ErrorCode OUTSOURCE_LOSS_QTY_INVALID = new ErrorCode(1_030_700_035, "委外损耗数量不合法或不足以结案");
    ErrorCode OUTSOURCE_LOSS_SOURCE_MISSING = new ErrorCode(1_030_700_036, "当前委外订单缺少可追溯的发料批次，无法登记或补录损耗");
    ErrorCode OUTSOURCE_ORDER_TYPE_BOM_INVALID = new ErrorCode(1_030_700_037, "有 BOM 委外必须选择 BOM，无 BOM 委外不能选择 BOM");
    ErrorCode OUTSOURCE_ORDER_MATERIAL_TRACKING_FORBIDDEN = new ErrorCode(1_030_700_038, "无 BOM 委外不支持材料追踪，请切换为有 BOM 委外");
    ErrorCode OUTSOURCE_ORDER_BOM_PRODUCT_MISMATCH = new ErrorCode(1_030_700_039, "BOM 成品与委外订单产品不一致");
    ErrorCode OUTSOURCE_FEE_NOT_EXISTS = new ErrorCode(1_030_700_040, "委外加工费不存在");
    ErrorCode OUTSOURCE_FEE_VOID_FAIL_ALLOCATED = new ErrorCode(1_030_700_041, "委外加工费({})已被付款核销，无法作废");

    // ========== ERP 一期项目/销售扩展 ==========
    ErrorCode SALE_ORDER_BUSINESS_TYPE_REQUIRED = new ErrorCode(1_020_201_011, "销售订单业务类型不能为空");
    ErrorCode SALE_ORDER_SOURCE_PROJECT_REQUIRED = new ErrorCode(1_020_201_012, "自研销售订单必须选择来源研发项目");
    ErrorCode SALE_ORDER_SETTLEMENT_TYPE_INVALID = new ErrorCode(1_020_201_013, "来料加工业务必须使用加工费结算类型");
    ErrorCode SALE_ORDER_REJECT_REASON_REQUIRED = new ErrorCode(1_020_201_014, "销售订单驳回时必须填写驳回原因");
    ErrorCode SALE_ORDER_STATUS_UPDATE_ILLEGAL = new ErrorCode(1_020_201_015, "当前销售订单状态不允许执行该操作");
    ErrorCode PROJECT_SOURCE_NOT_FOUND = new ErrorCode(1_020_204_004, "来源研发项目不存在");
    ErrorCode PROJECT_SOURCE_NOT_ENABLE = new ErrorCode(1_020_204_005, "来源研发项目({})未启用");
    ErrorCode PROJECT_SOURCE_REQUIRED = new ErrorCode(1_020_204_006, "来源项目不能为空");

    ErrorCode SALE_ORDER_BPM_SUBMIT_FAIL = new ErrorCode(1_020_201_016, "当前销售订单不允许提交审批");
    ErrorCode SALE_ORDER_BPM_CANCEL_FAIL = new ErrorCode(1_020_201_017, "当前销售订单不存在可撤回的审批流程");
    ErrorCode SALE_ORDER_UPDATE_FAIL_PROCESSING = new ErrorCode(1_020_201_018, "销售订单({})审批中，无法修改");
    ErrorCode SALE_ORDER_DELETE_FAIL_PROCESSING = new ErrorCode(1_020_201_019, "销售订单({})审批中，无法删除");

    ErrorCode PROJECT_PC_CONFIRM_FORBIDDEN = new ErrorCode(1_020_204_007, "当前用户不是项目 PC 负责人，无法执行确认");
    ErrorCode PROJECT_MC_CONFIRM_FORBIDDEN = new ErrorCode(1_020_204_008, "当前用户不是项目 MC 负责人，无法执行确认");
    ErrorCode PROJECT_PC_CONFIRM_STATUS_INVALID = new ErrorCode(1_020_204_009, "当前项目 PC 状态不允许重复确认");
    ErrorCode PROJECT_MC_CONFIRM_STATUS_INVALID = new ErrorCode(1_020_204_010, "当前项目 MC 状态不允许重复确认");

    // ========== ERP 租赁合同 1-030-610-000 ==========
    ErrorCode LEASE_CONTRACT_NOT_EXISTS = new ErrorCode(1_030_610_000, "租赁合同不存在");
    ErrorCode LEASE_CONTRACT_NO_EXISTS = new ErrorCode(1_030_610_001, "生成租赁合同号失败，请重新提交");
    ErrorCode LEASE_CONTRACT_STATUS_INVALID = new ErrorCode(1_030_610_002, "当前租赁合同状态不允许执行该操作");

    // ========== ERP 服务接收单 1-030-611-000 ==========
    ErrorCode SERVICE_RECEIPT_NOT_EXISTS = new ErrorCode(1_030_611_000, "服务接收单不存在");
    ErrorCode SERVICE_RECEIPT_NO_EXISTS = new ErrorCode(1_030_611_001, "生成服务接收单号失败，请重新提交");
    ErrorCode SERVICE_RECEIPT_STATUS_INVALID = new ErrorCode(1_030_611_002, "当前服务接收单状态不允许执行该操作");

    // ========== ERP 三单匹配 1-030-607-000 ==========
    ErrorCode THREE_WAY_MATCH_NOT_EXISTS = new ErrorCode(1_030_607_000, "三单匹配记录不存在");
    ErrorCode THREE_WAY_MATCH_STATUS_INVALID = new ErrorCode(1_030_607_001, "当前匹配记录状态不允许执行该操作");
    ErrorCode THREE_WAY_MATCH_INVOICE_NOT_EXISTS = new ErrorCode(1_030_607_002, "发票不存在或金额为空");
    ErrorCode THREE_WAY_MATCH_CONTRACT_NOT_EXISTS = new ErrorCode(1_030_607_003, "租赁合同不存在");
    ErrorCode THREE_WAY_MATCH_RECEIPT_NOT_EXISTS = new ErrorCode(1_030_607_004, "服务接收单不存在");

    // ========== ERP 市场预警 1-030-608-000 ==========
    ErrorCode MARKET_ALERT_NOT_EXISTS = new ErrorCode(1_030_608_000, "预警记录不存在");

    // ========== ERP 应收台账 1-030-609-000 ==========
    ErrorCode AR_STATEMENT_NOT_EXISTS = new ErrorCode(1_030_609_000, "应收台账不存在");
}
