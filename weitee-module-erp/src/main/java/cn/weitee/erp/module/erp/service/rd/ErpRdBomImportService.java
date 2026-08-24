package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPrecheckResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 研发 BOM Excel 导入 Service
 */
public interface ErpRdBomImportService {

    /**
     * 下载研发 BOM 明细导入模板
     */
    byte[] downloadTemplate();

    /**
     * 按单导入研发 BOM 明细
     *
     * @param productId     成品编号
     * @param bomCode       研发 BOM 编码
     * @param version       版本（可空）
     * @param remark        备注（可空）
     * @param updateSupport 保留参数（当前每次均新建草稿，预留覆盖能力）
     * @param file          明细 Excel
     * @return 导入结果与自动完整性校验问题清单
     */
    ErpRdBomImportResultVO importRdBom(Long productId, String bomCode, String version, String remark,
                                       Boolean updateSupport, MultipartFile file);

    /**
     * 导入预检查（dry-run，纯读不落库）
     *
     * 与导入共用同一解析链路，返回：
     * 待建档清单（物料不存在）、待催审清单（已建档但未审核/停用）、格式类问题清单，
     * 以及智能表头识别结果。待建档与待催审清单均为空时 readyToImport=true。
     *
     * @param productId     成品编号（可空，可由表头智能识别）
     * @param bomCode       研发 BOM 编码（可空）
     * @param version       版本（可空）
     * @param remark        备注（预检查不落库，仅为参数对齐保留）
     * @param updateSupport 保留参数（预检查不使用）
     * @param file          明细 Excel
     * @return 预检查结果
     */
    ErpRdBomPrecheckResultVO precheckRdBom(Long productId, String bomCode, String version, String remark,
                                           Boolean updateSupport, MultipartFile file);

}
