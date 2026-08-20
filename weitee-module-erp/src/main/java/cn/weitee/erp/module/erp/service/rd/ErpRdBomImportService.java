package cn.weitee.erp.module.erp.service.rd;

import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomImportResultVO;
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

}
