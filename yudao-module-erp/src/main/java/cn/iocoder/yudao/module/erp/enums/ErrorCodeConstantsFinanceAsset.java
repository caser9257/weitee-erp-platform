package cn.iocoder.yudao.module.erp.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstantsFinanceAsset {

    ErrorCode ASSET_NOT_EXISTS = new ErrorCode(1_030_608_000, "固定资产不存在");
    ErrorCode ASSET_NO_EXISTS = new ErrorCode(1_030_608_001, "生成固定资产编号失败，请重新提交");
    ErrorCode ASSET_DELETE_FAIL_HAS_DEPRECIATION = new ErrorCode(1_030_608_002, "固定资产({})已存在折旧记录，无法删除");
    ErrorCode ASSET_DELETE_FAIL_STATUS = new ErrorCode(1_030_608_003, "固定资产({})当前状态不允许删除");
    ErrorCode ASSET_STATUS_UPDATE_FAIL = new ErrorCode(1_030_608_004, "固定资产状态更新失败");
    ErrorCode ASSET_CANDIDATE_NOT_EXISTS = new ErrorCode(1_030_608_005, "固定资产候选记录不存在");
    ErrorCode ASSET_CANDIDATE_CONFIRM_FAIL = new ErrorCode(1_030_608_006, "固定资产候选记录已处理，不能重复确认");
    ErrorCode ASSET_DEPRECIATION_PERIOD_DUPLICATE = new ErrorCode(1_030_608_007, "期间 {} 已生成固定资产折旧，不能重复生成");
    ErrorCode ASSET_DEPRECIATION_PERIOD_INVALID = new ErrorCode(1_030_608_008, "折旧期间格式不正确，应为 yyyy-MM");
}
