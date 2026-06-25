package cn.weitee.erp.module.system.service.postlevel;

import cn.hutool.core.util.StrUtil;

import java.util.Set;

final class PostLevelClassifier {

    private static final String LEVEL_HIGH = "\u9ad8\u7ea7";
    private static final String LEVEL_MIDDLE = "\u4e2d\u7ea7";
    private static final String LEVEL_PRIMARY = "\u521d\u7ea7";
    private static final String LEVEL_UNCLASSIFIED = "\u672a\u5206\u7ea7";

    private static final Set<String> HIGH_POSTS = Set.of(
            normalizePostName("\u9879\u76ee\u7ba1\u7406"),
            normalizePostName("\u9500\u552e\u603b\u76d1"),
            normalizePostName("\u9500\u552e\u7ecf\u7406"),
            normalizePostName("\u5ba2\u6237\u7ecf\u7406"),
            normalizePostName("\u7efc\u5408\u8ba1\u5212\u90e8\u7ecf\u7406"),
            normalizePostName("\u8ba1\u5212\u7ecf\u7406")
    );

    private static final Set<String> MIDDLE_POSTS = Set.of(
            normalizePostName("\u4f1a\u8ba1"),
            normalizePostName("IT"),
            normalizePostName("\u884c\u653f"),
            normalizePostName("\u57f9\u8bad"),
            normalizePostName("\u62db\u8058"),
            normalizePostName("\u4fdd\u5bc6\u4e13\u5458"),
            normalizePostName("\u91c7\u8d2d\u5de5\u7a0b\u5e08"),
            normalizePostName("\u5de5\u827a\u5de5\u7a0b\u5e08"),
            normalizePostName("\u4f53\u7cfb\u5de5\u7a0b\u5e08"),
            normalizePostName("\u552e\u524d\u6280\u672f\u652f\u6301"),
            normalizePostName("\u5fae\u7ec4\u88c5\u8c03\u6d4b\u5de5\u7a0b\u5e08"),
            normalizePostName("\u5fae\u7ec4\u88c5\u8c03\u6d4b\u8bd5\u5de5\u7a0b\u5e08"),
            normalizePostName("\u4ea7\u54c1\u8c03\u8bd5\u5de5\u7a0b\u5e08"),
            normalizePostName("\u8f6f\u4ef6\u8c03\u8bd5\u5de5\u7a0b\u5e08"),
            normalizePostName("\u8f6f\u4ef6\u6d4b\u8bd5\u5de5\u7a0b\u5e08"),
            normalizePostName("\u6574\u673a\u6d4b\u8bd5\u5de5\u7a0b\u5e08"),
            normalizePostName("\u5c04\u9891\u5de5\u7a0b\u5e08"),
            normalizePostName("\u7ed3\u6784\u5de5\u7a0b\u5e08"),
            normalizePostName("C++\u7814\u53d1\u5de5\u7a0b\u5e08"),
            normalizePostName("\u5d4c\u5165\u5f0f\u5de5\u7a0b\u5e08"),
            normalizePostName("\u4fe1\u606f\u5316\u5de5\u7a0b\u5e08"),
            normalizePostName("FPGA\u5de5\u7a0b\u5e08"),
            normalizePostName("PCB\u5de5\u7a0b\u5e08"),
            normalizePostName("\u5668\u4ef6\u5de5\u7a0b\u5e08"),
            normalizePostName("\u786c\u4ef6\u5de5\u7a0b\u5e08"),
            normalizePostName("\u751f\u4ea7\u8ba1\u5212\u5458"),
            normalizePostName("\u4e3b\u8ba1\u5212\u5458"),
            normalizePostName("\u751f\u4ea7\u8ba1\u5212\u4e3b\u7ba1")
    );

    private static final Set<String> PRIMARY_POSTS = Set.of(
            normalizePostName("\u9879\u76ee\u52a9\u7406"),
            normalizePostName("\u8ba1\u5212\u4e13\u5458"),
            normalizePostName("\u8ddf\u5355\u5458"),
            normalizePostName("\u51fa\u7eb3"),
            normalizePostName("\u5e93\u623f\u4e13\u5458"),
            normalizePostName("\u9500\u552e\u52a9\u7406"),
            normalizePostName("\u8d28\u91cf\u52a9\u7406"),
            normalizePostName("\u68c0\u9a8c\u5458"),
            normalizePostName("\u6d4b\u8bd5\u6280\u670d\u5458"),
            normalizePostName("\u6d4b\u8bd5\u6280\u672f\u5458"),
            normalizePostName("\u7814\u53d1\u52a9\u7406"),
            normalizePostName("\u5d4c\u5165\u5f0f\u5b9e\u4e60\u751f"),
            normalizePostName("\u540e\u52e4\u5458"),
            normalizePostName("\u7269\u6d41\u5458"),
            normalizePostName("SMT"),
            normalizePostName("\u7535\u88c5"),
            normalizePostName("\u7c98\u7247\u64cd\u4f5c\u5458"),
            normalizePostName("\u8f85\u52a9\u64cd\u4f5c\u5458\uff08\u5305\u88c5\uff09"),
            normalizePostName("\u8f85\u52a9\u64cd\u4f5c\u5458\uff08\u6e05\u6d17\uff09"),
            normalizePostName("\u8f85\u52a9\u64cd\u4f5c\u5458\uff08\u5305\u88c5/\u6e05\u6d17\uff09"),
            normalizePostName("\u7ea4\u710a\u64cd\u4f5c\u5458"),
            normalizePostName("\u94ce\u710a\u64cd\u4f5c\u5458"),
            normalizePostName("\u948e\u710a\u64cd\u4f5c\u5458"),
            normalizePostName("\u952e\u5408\u64cd\u4f5c\u5458"),
            normalizePostName("\u6fc0\u5149\u5c01\u710a")
    );

    private PostLevelClassifier() {
    }

    static String resolveLevel(String level, String postName) {
        if (StrUtil.isNotBlank(level)) {
            String normalizedLevel = level.trim();
            if (!StrUtil.equals(normalizedLevel, LEVEL_UNCLASSIFIED)) {
                return normalizedLevel;
            }
        }
        String normalizedName = normalizePostName(postName);
        if (StrUtil.isBlank(normalizedName)) {
            return LEVEL_UNCLASSIFIED;
        }
        if (HIGH_POSTS.contains(normalizedName)) {
            return LEVEL_HIGH;
        }
        if (MIDDLE_POSTS.contains(normalizedName)) {
            return LEVEL_MIDDLE;
        }
        if (PRIMARY_POSTS.contains(normalizedName)) {
            return LEVEL_PRIMARY;
        }
        return LEVEL_UNCLASSIFIED;
    }

    private static String normalizePostName(String postName) {
        if (StrUtil.isBlank(postName)) {
            return null;
        }
        return StrUtil.trim(postName)
                .replace("(", "\uff08")
                .replace(")", "\uff09")
                .replaceAll("\\s+", "");
    }
}
