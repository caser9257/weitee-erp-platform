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
    ErrorCode MES_SOP_NO_DUPLICATE = new ErrorCode(1_040_000_013, "SOP 编码已存在");
    ErrorCode MES_SOP_IMPORT_FILE_EMPTY = new ErrorCode(1_040_000_014, "上传文件不能为空");
    ErrorCode MES_SOP_IMPORT_FILE_TOO_LARGE = new ErrorCode(1_040_000_015, "上传文件不能超过 10MB");
    ErrorCode MES_SOP_IMPORT_FILE_TYPE_INVALID = new ErrorCode(1_040_000_016, "仅支持上传图片文件");
    ErrorCode MES_TASK_DISPATCH_NOT_EXISTS = new ErrorCode(1_040_000_017, "派工记录不存在");
    ErrorCode MES_TASK_DISPATCH_ALREADY_EXISTS = new ErrorCode(1_040_000_018, "当前工序任务已有派工记录，请使用改派");
    ErrorCode MES_TASK_DISPATCH_ASSIGNEE_REQUIRED = new ErrorCode(1_040_000_019, "班组、人员、设备至少选择一项");
    ErrorCode MES_EXECUTION_NOT_DISPATCHED = new ErrorCode(1_040_000_020, "当前任务尚未派工或派工已失效");
    ErrorCode MES_EXECUTION_ERP_STEP_NOT_EXISTS = new ErrorCode(1_040_000_021, "现场执行对应的 ERP 工序不存在");

}
