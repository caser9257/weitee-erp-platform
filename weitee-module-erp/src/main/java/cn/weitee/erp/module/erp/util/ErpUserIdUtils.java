package cn.weitee.erp.module.erp.util;

import cn.hutool.core.util.StrUtil;

/**
 * ERP 用户编号解析工具，兼容脏历史数据。
 */
public final class ErpUserIdUtils {

    private ErpUserIdUtils() {
    }

    public static Long parseUserId(String value) {
        return StrUtil.isNumeric(value) ? Long.valueOf(value) : null;
    }

}
