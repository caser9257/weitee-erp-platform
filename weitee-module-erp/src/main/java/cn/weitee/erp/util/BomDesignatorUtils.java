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
 *   <li>区间段支持两种形态（均展开为前缀大写 + start..end，保留数字宽度零填充）：
 *       {@code R101-105}（后半段省略前缀）、{@code R101-R105}（两侧同前缀，方案汇报的标准形态）</li>
 *   <li>段匹配 {@code 前缀+数字（如 C12）} → 原样（大写）保留</li>
 *   <li>不匹配上述形态（含两侧前缀不一致的区间、逆序区间、展开超限）→ 原串大写保留，不丢失信息</li>
 * </ul>
 */
public class BomDesignatorUtils {

    /** 区间形态一：后半段省略前缀，如 R101-105 */
    private static final Pattern RANGE_PATTERN = Pattern.compile("^([A-Za-z]+)(\\d+)-(\\d+)$");
    /** 区间形态二：两侧同前缀，如 R101-R105（方案汇报的标准写法） */
    private static final Pattern RANGE_WITH_PREFIX_PATTERN = Pattern.compile("^([A-Za-z]+)(\\d+)-([A-Za-z]+)(\\d+)$");
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

    /**
     * 判断文本是否为位号形态：全部逗号分段均匹配「前缀+数字」或区间形态。
     * 用于数量一致性比对的前置过滤——位置描述等自由文本（如「安装于侧板上下各一块」、
     * 「外壳-通-连接-框架组件PCBA（网络1 J5）」）解析不出位号模式，跳过比对避免误报。
     */
    public static boolean isDesignatorLike(String raw) {
        if (StrUtil.isBlank(raw)) {
            return false;
        }
        boolean hasSegment = false;
        for (String segment : raw.split(",")) {
            String normalized = segment.trim().replaceAll("\\s+", "").toUpperCase();
            if (normalized.isEmpty()) {
                continue;
            }
            hasSegment = true;
            if (!SINGLE_PATTERN.matcher(normalized).matches()
                    && !RANGE_PATTERN.matcher(normalized).matches()
                    && !RANGE_WITH_PREFIX_PATTERN.matcher(normalized).matches()) {
                return false;
            }
        }
        return hasSegment;
    }

    /**
     * 单个区间展开数量上限：超过视为非法区间（保留原串），
     * 防御 "R1-R9999999" 类输入在保存/导入事务内生成千万级字符串导致 OOM。
     */
    private static final int MAX_RANGE_EXPAND = 10_000;

    private static String[] expandSegment(String segment) {
        Matcher rangeMatcher = RANGE_PATTERN.matcher(segment);
        if (rangeMatcher.matches()) {
            return expandRange(rangeMatcher.group(1), rangeMatcher.group(2), rangeMatcher.group(3), segment);
        }
        Matcher rangeWithPrefixMatcher = RANGE_WITH_PREFIX_PATTERN.matcher(segment);
        if (rangeWithPrefixMatcher.matches()) {
            String startPrefix = rangeWithPrefixMatcher.group(1);
            String endPrefix = rangeWithPrefixMatcher.group(3);
            // 两侧前缀不一致（如 R101-C105）不构成区间，保留原串
            if (!startPrefix.equals(endPrefix)) {
                return new String[]{segment};
            }
            return expandRange(startPrefix, rangeWithPrefixMatcher.group(2), rangeWithPrefixMatcher.group(4), segment);
        }
        Matcher singleMatcher = SINGLE_PATTERN.matcher(segment);
        if (singleMatcher.matches()) {
            return new String[]{segment};
        }
        // 不匹配区间/单值形态，原串保留
        return new String[]{segment};
    }

    private static String[] expandRange(String prefix, String startStr, String endStr, String segment) {
        long start;
        long end;
        try {
            start = Long.parseLong(startStr);
            end = Long.parseLong(endStr);
        } catch (NumberFormatException e) {
            // 数值超出 long 范围，视为非法区间，保留原串
            return new String[]{segment};
        }
        if (end < start || end - start + 1 > MAX_RANGE_EXPAND) {
            // 非法区间（起止颠倒）或展开规模超限，保留原串，不丢失信息
            return new String[]{segment};
        }
        int width = startStr.length();
        List<String> expanded = new ArrayList<>();
        for (long i = start; i <= end; i++) {
            expanded.add(prefix + String.format("%0" + width + "d", i));
        }
        return expanded.toArray(new String[0]);
    }

}
