package cn.weitee.erp.module.mes.service;

import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportConfirmReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordRespVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopDocumentDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopImportRecordDO;
import cn.weitee.erp.module.mes.dal.mysql.MesSopDocumentMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesSopImportRecordMapper;
import cn.weitee.erp.module.mes.infra.ocr.SopOcrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_IMPORT_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_IMPORT_STATUS_INVALID;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_NO_DUPLICATE;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_OCR_FAILED;

@Service
@Validated
public class MesSopImportServiceImpl implements MesSopImportService {

    private static final Logger log = LoggerFactory.getLogger(MesSopImportServiceImpl.class);

    /** 导入记录状态：0待校对 1已确认 2已转正式 3识别失败 */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_CONFIRMED = 1;
    private static final int STATUS_DONE = 2;
    private static final int STATUS_FAILED = 3;

    @Resource
    private MesSopImportRecordMapper mesSopImportRecordMapper;
    @Resource
    private MesSopDocumentMapper mesSopDocumentMapper;
    @Resource
    private SopOcrClient sopOcrClient;

    @Override
    public MesSopImportRecordRespVO ocrImport(String fileName, byte[] imageBytes) {
        MesSopImportRecordDO record = new MesSopImportRecordDO()
                .setFileName(fileName)
                .setStatus(STATUS_PENDING);
        try {
            String text = sopOcrClient.recognize(imageBytes);
            if (StrUtil.isBlank(text)) {
                throw new IllegalStateException("OCR 识别结果为空");
            }
            record.setOcrText(text);
        } catch (Exception e) {
            log.error("[ocrImport][OCR 识别失败 file={}]", fileName, e);
            record.setStatus(STATUS_FAILED);
            record.setErrorMsg(StrUtil.maxLength(e.getMessage(), 500));
            mesSopImportRecordMapper.insert(record);
            throw exception(MES_SOP_OCR_FAILED);
        }
        mesSopImportRecordMapper.insert(record);
        return buildRespVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmImport(MesSopImportConfirmReqVO reqVO) {
        MesSopImportRecordDO record = mesSopImportRecordMapper.selectById(reqVO.getImportRecordId());
        if (record == null) {
            throw exception(MES_SOP_IMPORT_NOT_EXISTS);
        }
        if (record.getStatus() != STATUS_PENDING && record.getStatus() != STATUS_CONFIRMED) {
            throw exception(MES_SOP_IMPORT_STATUS_INVALID);
        }
        // 生成草稿态 SOP（OCR 仅作导入辅助，不直接发布——需人工后续发布）
        // 释放同名软删记录的唯一键，允许编码复用
        mesSopDocumentMapper.deletePhysicalBySopNo(reqVO.getSopNo());
        MesSopDocumentDO sop = new MesSopDocumentDO()
                .setSopNo(reqVO.getSopNo())
                .setTitle(reqVO.getTitle())
                .setVersion(StrUtil.blankToDefault(reqVO.getVersion(), "V1.0"))
                .setContent(reqVO.getContent() != null ? reqVO.getContent() : record.getOcrText())
                .setAttachmentUrl(record.getFileUrl())
                .setStatus(0)
                .setRemark(reqVO.getRemark());
        try {
            mesSopDocumentMapper.insert(sop);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw exception(MES_SOP_NO_DUPLICATE);
        }
        mesSopImportRecordMapper.updateById(new MesSopImportRecordDO()
                .setId(record.getId())
                .setStatus(STATUS_DONE)
                .setSopId(sop.getId()));
        return sop.getId();
    }

    @Override
    public PageResult<MesSopImportRecordRespVO> getImportPage(MesSopImportRecordPageReqVO pageReqVO) {
        PageResult<MesSopImportRecordDO> pageResult = mesSopImportRecordMapper.selectPage(pageReqVO);
        return new PageResult<>(pageResult.getList().stream().map(this::buildRespVO).toList(),
                pageResult.getTotal());
    }

    private MesSopImportRecordRespVO buildRespVO(MesSopImportRecordDO record) {
        return BeanUtils.toBean(record, MesSopImportRecordRespVO.class);
    }
}
