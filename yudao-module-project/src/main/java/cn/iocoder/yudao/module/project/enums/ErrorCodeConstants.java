package cn.iocoder.yudao.module.project.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface ErrorCodeConstants {
    // 项目管理
    ErrorCode PROJECT_NOT_FOUND = new ErrorCode(1002001001, "项目不存在");
    ErrorCode PROJECT_NAME_LENGTH_ERROR = new ErrorCode(1002001002, "项目名称长度为 2-32 个字符");
    ErrorCode PROJECT_PERSONAL_ALREADY_EXISTS = new ErrorCode(1002001003, "个人项目已存在");
    ErrorCode PROJECT_MEMBER_ALREADY_EXISTS = new ErrorCode(1002001004, "该用户已是项目成员");
    ErrorCode PROJECT_MEMBER_NOT_FOUND = new ErrorCode(1002001005, "项目成员不存在");

    // 列表管理
    ErrorCode COLUMN_NOT_FOUND = new ErrorCode(1002002001, "列表不存在");
    ErrorCode COLUMN_MAX_COUNT_ERROR = new ErrorCode(1002002002, "项目列表最多不能超过 30 个");

    // 任务管理
    ErrorCode TASK_NOT_FOUND = new ErrorCode(1002003001, "任务不存在");
    ErrorCode TASK_PROJECT_MAX_COUNT_ERROR = new ErrorCode(1002003002, "项目内未完成任务最多不能超过 2000 个");
    ErrorCode TASK_COLUMN_MAX_COUNT_ERROR = new ErrorCode(1002003003, "单个列表未完成任务最多不能超过 500 个");
    ErrorCode TASK_SUBTASK_MAX_COUNT_ERROR = new ErrorCode(1002003004, "每个任务的子任务最多不能超过 50 个");
    ErrorCode TASK_ALREADY_COMPLETED = new ErrorCode(1002003005, "任务已完成");
    ErrorCode TASK_NOT_COMPLETED = new ErrorCode(1002003006, "任务未完成");

    // 工作流管理
    ErrorCode FLOW_ITEM_NOT_FOUND = new ErrorCode(1002005001, "工作流状态不存在");
    ErrorCode FLOW_MUST_HAVE_START = new ErrorCode(1002005002, "至少需要 1 个开始状态");
    ErrorCode FLOW_MUST_HAVE_END = new ErrorCode(1002005003, "至少需要 1 个结束状态");

    // 文件管理
    ErrorCode FILE_NOT_FOUND = new ErrorCode(1002006001, "文件不存在");

    // 日报管理
    ErrorCode REPORT_NOT_FOUND = new ErrorCode(1002007001, "日报不存在");

    // 会议管理
    ErrorCode MEETING_NOT_FOUND = new ErrorCode(1002008001, "会议不存在");

    // 审批管理
    ErrorCode APPROVE_NOT_FOUND = new ErrorCode(1002009001, "审批不存在");

    // 操作日志
    ErrorCode LOG_NOT_FOUND = new ErrorCode(1002010001, "操作日志不存在");

    // 评论管理
    ErrorCode COMMENT_NOT_FOUND = new ErrorCode(1002011001, "评论不存在");
}
