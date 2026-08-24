package cn.weitee.erp.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BomDesignatorUtilsTest {

    @Test
    void testParseNullAndBlank() {
        assertThat(BomDesignatorUtils.parseDesignatorList(null)).isEmpty();
        assertThat(BomDesignatorUtils.parseDesignatorList("")).isEmpty();
        assertThat(BomDesignatorUtils.parseDesignatorList("   ")).isEmpty();
    }

    @Test
    void testParseMixedRangeSingleAndTrailingComma() {
        List<String> result = BomDesignatorUtils.parseDesignatorList("R101-R105, R108, C12");
        assertThat(result).containsExactly("R101", "R102", "R103", "R104", "R105", "R108", "C12");
        assertThat(BomDesignatorUtils.countDesignators("R101-R105, R108, C12")).isEqualTo(7);
    }

    @Test
    void testParseZeroPaddingWidth() {
        List<String> result = BomDesignatorUtils.parseDesignatorList("R001-R005");
        assertThat(result).containsExactly("R001", "R002", "R003", "R004", "R005");
    }

    @Test
    void testParseSingleValueKeepsGivenWidth() {
        assertThat(BomDesignatorUtils.parseDesignatorList("C12")).containsExactly("C12");
        assertThat(BomDesignatorUtils.parseDesignatorList("U3")).containsExactly("U3");
    }

    @Test
    void testParseReverseRangeKeepsOriginal() {
        // 起止颠倒视为非法，保留原串
        assertThat(BomDesignatorUtils.parseDesignatorList("R105-R101")).containsExactly("R105-R101");
    }

    @Test
    void testParseWhitespaceInsideSegment() {
        assertThat(BomDesignatorUtils.parseDesignatorList("R101 - R105")).containsExactly(
                "R101", "R102", "R103", "R104", "R105");
    }

    @Test
    void testParseUnrecognizedKeepsOriginalUppercased() {
        assertThat(BomDesignatorUtils.parseDesignatorList("NOTE")).containsExactly("NOTE");
    }

    @Test
    void testParseConsecutiveCommasSkipped() {
        assertThat(BomDesignatorUtils.parseDesignatorList("R1,,R2")).containsExactly("R1", "R2");
    }
}
