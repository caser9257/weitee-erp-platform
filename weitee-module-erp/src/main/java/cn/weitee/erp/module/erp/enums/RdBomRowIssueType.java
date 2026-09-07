package cn.weitee.erp.module.erp.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 研发 BOM 导入行级失败原因分类
 *
 * 用于导入失败明细（FailDetail.issueType）与预检查结果归堆：
 * MISSING_MATERIAL / MATERIAL_NOT_APPROVED / MATERIAL_DISABLED 归入物料档案类清单，
 * FORMAT_ERROR 为纯格式问题，CADENCE_DATA_INCOMPLETE 为元器件 Cadence 数据不完整。
 *
 * @author WeTai
 */
@RequiredArgsConstructor
@Getter
public enum RdBomRowIssueType {

    MISSING_MATERIAL("MISSING_MATERIAL", "物料未建档"),
    MATERIAL_NOT_APPROVED("MATERIAL_NOT_APPROVED", "物料未审核通过"),
    MATERIAL_DISABLED("MATERIAL_DISABLED", "物料已停用"),
    SPEC_MISMATCH("SPEC_MISMATCH", "规格型号与产品档案不符"),
    CADENCE_DATA_INCOMPLETE("CADENCE_DATA_INCOMPLETE", "Cadence 元器件数据不完整"),
    FORMAT_ERROR("FORMAT_ERROR", "格式错误"),
    INTEGRITY_ERROR("INTEGRITY_ERROR", "完整性校验未通过"),
    FILE_EMPTY("FILE_EMPTY", "文件未解析到明细行");

    /**
     * 序列化到 VO 的标识串
     */
    private final String code;
    /**
     * 中文名
     */
    private final String name;

}
