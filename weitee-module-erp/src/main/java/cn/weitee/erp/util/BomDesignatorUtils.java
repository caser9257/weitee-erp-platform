package cn.weitee.erp.util;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BOM 位号（Reference Designator）解析工具
 *
 * <p>规则（前后端保持一致）：
 * <ul>
 *   <li>空文本 → 空列表</li>
 *   <li>按逗号分隔，逐段 trim 并跳过空段</li>
 *   <li>段匹配 {@code 前缀+起止数字（如 R101-R105）} → 前缀大写 + start..end 展开，保留数字宽度（零填充）</li>
 *   <li>段匹配 {@code 前缀+数字（如 C12）} → 原样（大写）保留</li>
 *   <li>不匹配上述形态 → 原串大写保留，不丢失信息</li>
 * </ul>
 */
public class BomDesignatorUtils {

    private static final Pattern RANGE_PATTERN = Pattern.compile("^([A-Za-z]+)(\\d+)-(\\d+)$");
    private static final Pattern SINGLE_PATTERN = Pattern.compile("^([A-Za-z]+)(\\d+)$");

    /**
     * 解析位号字符串为位号列表
     *
     * @param raw 原始位号文本，如 "R101-R105, R108, C12"
     * @return 展开后的位号列表，空文本返回空列表
     */
    public static List<String> parseDesignatorList(String raw) {
        List<String> result = new ArrayList<>();
        if (StrUtil.isBlank(raw)) {
            return result;
        }
        String[] segments = raw.split(",");
        for (String segment : segments) {
            String trimmed = segment.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String normalized = trimmed.replaceAll("\\s+", "").toUpperCase();
            String[] expanded = expandSegment(normalized);
            for (String designator : expanded) {
                result.add(designator);
            }
        }
        return result;
    }

    /**
     * 统计位号数量（等于 {@link #parseDesignatorList} 的结果长度）
     */
    public static int countDesignators(String raw) {
        return parseDesignatorList(raw).size();
    }

    private static String[] expandSegment(String segment) {
        Matcher rangeMatcher = RANGE_PATTERN.matcher(segment);
        if (rangeMatcher.matches()) {
            String prefix = rangeMatcher.group(1);
            String startStr = rangeMatcher.group(2);
            String endStr = rangeMatcher.group(3);
            int start = Integer.parseInt(startStr);
            int end = Integer.parseInt(endStr);
            if (end < start) {
                // 非法区间（起止颠倒），保留原串，不丢失信息
                return new String[]{segment};
            }
            int width = startStr.length();
            List<String> expanded = new ArrayList<>();
            for (int i = start; i <= end; i++) {
                expanded.add(prefix + String.format("%0" + width + "d", i));
            }
            return expanded.toArray(new String[0]);
        }
        Matcher singleMatcher = SINGLE_PATTERN.matcher(segment);
        if (singleMatcher.matches()) {
            return new String[]{segment};
        }
        // 不匹配区间/单值形态，原串保留
        return new String[]{segment};
    }

}
