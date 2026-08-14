package cn.weitee.erp.module.mes.enums;

import cn.weitee.erp.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {

    ErrorCode MES_WORK_TASK_NOT_EXISTS = new ErrorCode(1_040_000_001, "工序任务不存在");
    ErrorCode MES_WORK_TASK_STATUS_INVALID = new ErrorCode(1_040_000_002, "当前任务状态不允许执行该操作");
    ErrorCode MES_WORK_TASK_PLAN_TIME_INVALID = new ErrorCode(1_040_000_003, "计划结束时间不能早于计划开始时间");
    ErrorCode MES_WORK_CALENDAR_NOT_EXISTS = new ErrorCode(1_040_000_004, "工作日历不存在");
    ErrorCode MES_WORK_CALENDAR_DATE_INVALID = new ErrorCode(1_040_000_005, "日历失效日期不能早于生效日期");
    ErrorCode MES_WORK_CALENDAR_WEEK_MASK_INVALID = new ErrorCode(1_040_000_006, "周几开工格式不正确（应为 7 位 0/1）");
    ErrorCode MES_SOP_NOT_EXISTS = new ErrorCode(1_040_000_007, "SOP 不存在");
    ErrorCode MES_SOP_STATUS_INVALID = new ErrorCode(1_040_000_008, "当前 SOP 状态不允许执行该操作");
    ErrorCode MES_SOP_DATE_INVALID = new ErrorCode(1_040_000_009, "SOP 失效日期不能早于生效日期");
    ErrorCode MES_SOP_IMPORT_NOT_EXISTS = new ErrorCode(1_040_000_010, "SOP 导入记录不存在");
    ErrorCode MES_SOP_IMPORT_STATUS_INVALID = new ErrorCode(1_040_000_011, "当前导入记录状态不允许执行该操作");
    ErrorCode MES_SOP_OCR_FAILED = new ErrorCode(1_040_000_012, "OCR 识别失败，请稍后重试");

}
