package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRoutePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.route.ErpProcessRouteSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.*;
import cn.weitee.erp.module.erp.dal.mysql.mrp.*;
import cn.weitee.erp.module.erp.enums.mrp.ErpProcessRouteStatusEnum;
import cn.weitee.erp.module.erp.service.product.ErpProductService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
public class ErpProcessRouteServiceImpl implements ErpProcessRouteService {
    @Resource private ErpProcessRouteMapper routeMapper;
    @Resource private ErpProcessRouteStepMapper stepMapper;
    @Resource private ErpWorkCenterMapper workCenterMapper;
    @Resource private ErpBomMapper bomMapper;
    @Resource private ErpProductionOrderMapper productionOrderMapper;
    @Resource private ErpProductService productService;

    @Override @Transactional(rollbackFor = Exception.class)
    public Long create(ErpProcessRouteSaveReqVO reqVO) {
        validateSave(reqVO, null);
        ErpProcessRouteDO route = BeanUtils.toBean(reqVO, ErpProcessRouteDO.class)
                .setDefaultFlag(Boolean.TRUE.equals(reqVO.getDefaultFlag()))
                .setStatus(ErpProcessRouteStatusEnum.DRAFT.getStatus());
        routeMapper.insert(route);
        saveSteps(route.getId(), reqVO.getSteps());
        refreshDefault(route);
        return route.getId();
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void update(ErpProcessRouteSaveReqVO reqVO) {
        ErpProcessRouteDO existed = validateExists(reqVO.getId());
        validateSave(reqVO, existed.getId());
        ErpProcessRouteDO route = BeanUtils.toBean(reqVO, ErpProcessRouteDO.class)
                .setStatus(existed.getStatus()).setDefaultFlag(Boolean.TRUE.equals(reqVO.getDefaultFlag()));
        routeMapper.updateById(route);
        stepMapper.deleteByRouteId(route.getId());
        saveSteps(route.getId(), reqVO.getSteps());
        refreshDefault(route);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        validateExists(id);
        if (status == null || java.util.Arrays.stream(ErpProcessRouteStatusEnum.values())
                .noneMatch(item -> item.getStatus().equals(status))) throw exception(PROCESS_ROUTE_STATUS_INVALID);
        routeMapper.updateById(new ErpProcessRouteDO().setId(id).setStatus(status));
    }

    @Override @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validateExists(id);
        if (bomMapper.selectCount(ErpBomDO::getRouteId, id) > 0 || productionOrderMapper.selectCount(ErpProductionOrderDO::getRouteId, id) > 0) {
            throw exception(PROCESS_ROUTE_REFERENCED);
        }
        stepMapper.deleteByRouteId(id);
        routeMapper.deleteById(id);
    }

    @Override public ErpProcessRouteDO get(Long id) { return validateExists(id); }
    @Override public PageResult<ErpProcessRouteDO> getPage(ErpProcessRoutePageReqVO reqVO) { return routeMapper.selectPage(reqVO); }
    @Override public List<ErpProcessRouteStepDO> getStepList(Long routeId) { return stepMapper.selectListByRouteId(routeId); }

    private void validateSave(ErpProcessRouteSaveReqVO reqVO, Long id) {
        productService.validProductList(List.of(reqVO.getProductId()));
        ErpProcessRouteDO byCode = routeMapper.selectByRouteCode(reqVO.getRouteCode());
        if (byCode != null && !byCode.getId().equals(id)) throw exception(PROCESS_ROUTE_CODE_DUPLICATE);
        if (reqVO.getEffectiveDate() != null && reqVO.getExpireDate() != null && reqVO.getExpireDate().isBefore(reqVO.getEffectiveDate())) throw exception(PROCESS_ROUTE_DATE_INVALID);
        Set<Integer> stepNos = new HashSet<>(); Set<String> stepCodes = new HashSet<>(); Set<Long> centerIds = new HashSet<>();
        for (ErpProcessRouteSaveReqVO.Step step : reqVO.getSteps()) {
            if (!stepNos.add(step.getStepNo()) || !stepCodes.add(step.getStepCode())) throw exception(PROCESS_ROUTE_STEP_INVALID);
            if (step.getWorkCenterId() != null) centerIds.add(step.getWorkCenterId());
        }
        if (!centerIds.isEmpty() && workCenterMapper.selectByIds(centerIds).size() != centerIds.size()) throw exception(WORK_CENTER_NOT_EXISTS);
    }

    private void saveSteps(Long routeId, List<ErpProcessRouteSaveReqVO.Step> steps) {
        stepMapper.insertBatch(BeanUtils.toBean(steps, ErpProcessRouteStepDO.class, step -> step
                .setRouteId(routeId).setStatus(ErpProcessRouteStatusEnum.ENABLED.getStatus())
                .setOutsourceFlag(Boolean.TRUE.equals(step.getOutsourceFlag())).setQcFlag(Boolean.TRUE.equals(step.getQcFlag()))
                .setReportRequired(!Boolean.FALSE.equals(step.getReportRequired())).setInspectRequired(Boolean.TRUE.equals(step.getInspectRequired()))));
    }

    private void refreshDefault(ErpProcessRouteDO route) {
        if (Boolean.TRUE.equals(route.getDefaultFlag())) routeMapper.update(null, new LambdaUpdateWrapper<ErpProcessRouteDO>()
                .eq(ErpProcessRouteDO::getProductId, route.getProductId()).ne(ErpProcessRouteDO::getId, route.getId())
                .set(ErpProcessRouteDO::getDefaultFlag, false));
    }
    private ErpProcessRouteDO validateExists(Long id) { ErpProcessRouteDO route = routeMapper.selectById(id); if (route == null) throw exception(PROCESS_ROUTE_NOT_EXISTS); return route; }
}
