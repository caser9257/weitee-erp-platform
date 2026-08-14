package cn.weitee.erp.module.mes.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentPageReqVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentRespVO;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentSaveReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopDocumentDO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopStepBindingDO;
import cn.weitee.erp.module.mes.dal.mysql.MesSopDocumentMapper;
import cn.weitee.erp.module.mes.dal.mysql.MesSopStepBindingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_NOT_EXISTS;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_STATUS_INVALID;
import static cn.weitee.erp.module.mes.enums.ErrorCodeConstants.MES_SOP_DATE_INVALID;

@Service
@Validated
public class MesSopServiceImpl implements MesSopService {

    @Resource
    private MesSopDocumentMapper mesSopDocumentMapper;
    @Resource
    private MesSopStepBindingMapper mesSopStepBindingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSop(MesSopDocumentSaveReqVO reqVO) {
        validateSop(reqVO);
        MesSopDocumentDO sop = BeanUtils.toBean(reqVO, MesSopDocumentDO.class, item -> item.setStatus(0));
        mesSopDocumentMapper.insert(sop);
        saveBindings(sop.getId(), reqVO.getRouteStepIds());
        return sop.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSop(MesSopDocumentSaveReqVO reqVO) {
        MesSopDocumentDO existed = validateSopExists(reqVO.getId());
        if (existed.getStatus() == 1) {
            throw exception(MES_SOP_STATUS_INVALID); // 已发布 SOP 不允许直接修改（后续版本化）
        }
        validateSop(reqVO);
        mesSopDocumentMapper.updateById(BeanUtils.toBean(reqVO, MesSopDocumentDO.class));
        mesSopStepBindingMapper.deleteBySopId(reqVO.getId());
        saveBindings(reqVO.getId(), reqVO.getRouteStepIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        validateSopExists(id);
        if (status != 1 && status != 2) {
            throw exception(MES_SOP_STATUS_INVALID);
        }
        mesSopDocumentMapper.updateById(new MesSopDocumentDO().setId(id).setStatus(status));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSop(Long id) {
        validateSopExists(id);
        mesSopStepBindingMapper.deleteBySopId(id);
        mesSopDocumentMapper.deleteById(id);
    }

    @Override
    public MesSopDocumentRespVO getSop(Long id) {
        MesSopDocumentDO sop = validateSopExists(id);
        return buildRespVO(sop);
    }

    @Override
    public PageResult<MesSopDocumentRespVO> getSopPage(MesSopDocumentPageReqVO pageReqVO) {
        PageResult<MesSopDocumentDO> pageResult = mesSopDocumentMapper.selectPage(pageReqVO);
        return new PageResult<>(pageResult.getList().stream().map(this::buildRespVO).toList(),
                pageResult.getTotal());
    }

    @Override
    public List<MesSopDocumentRespVO> getPublishedSopsByStepId(Long routeStepId) {
        List<MesSopStepBindingDO> bindings = mesSopStepBindingMapper.selectListByStepId(routeStepId);
        if (CollUtil.isEmpty(bindings)) {
            return List.of();
        }
        List<Long> sopIds = bindings.stream().map(MesSopStepBindingDO::getSopId).distinct().toList();
        return mesSopDocumentMapper.selectByIds(sopIds).stream()
                .filter(sop -> sop.getStatus() == 1)
                .map(this::buildRespVO)
                .toList();
    }

    private MesSopDocumentRespVO buildRespVO(MesSopDocumentDO sop) {
        MesSopDocumentRespVO respVO = BeanUtils.toBean(sop, MesSopDocumentRespVO.class);
        respVO.setRouteStepIds(mesSopStepBindingMapper.selectListBySopId(sop.getId()).stream()
                .map(MesSopStepBindingDO::getRouteStepId).toList());
        return respVO;
    }

    private void saveBindings(Long sopId, List<Long> routeStepIds) {
        if (CollUtil.isEmpty(routeStepIds)) {
            return;
        }
        mesSopStepBindingMapper.insertBatch(routeStepIds.stream()
                .map(stepId -> new MesSopStepBindingDO().setSopId(sopId).setRouteStepId(stepId))
                .collect(Collectors.toList()));
    }

    private void validateSop(MesSopDocumentSaveReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getVersion())) {
            reqVO.setVersion("V1.0");
        }
        if (reqVO.getEffectiveDate() != null && reqVO.getExpireDate() != null
                && reqVO.getExpireDate().isBefore(reqVO.getEffectiveDate())) {
            throw exception(MES_SOP_DATE_INVALID);
        }
    }

    private MesSopDocumentDO validateSopExists(Long id) {
        MesSopDocumentDO sop = mesSopDocumentMapper.selectById(id);
        if (sop == null) {
            throw exception(MES_SOP_NOT_EXISTS);
        }
        return sop;
    }
}
