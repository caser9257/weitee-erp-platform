package cn.weitee.erp.module.mes.service;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportConfirmReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordRespVO;

public interface MesSopImportService {

    /**
     * OCR 识别上传图片并生成草稿记录。
     *
     * @param fileName 文件名
     * @param imageBytes 图片字节
     * @return 导入记录（含识别文本）
     */
    MesSopImportRecordRespVO ocrImport(String fileName, byte[] imageBytes);

    /**
     * 校对确认草稿 → 转正式 SOP（草稿状态待校对 → 已确认并生成草稿态 SOP）。
     */
    Long confirmImport(MesSopImportConfirmReqVO reqVO);

    PageResult<MesSopImportRecordRespVO> getImportPage(MesSopImportRecordPageReqVO pageReqVO);

}
