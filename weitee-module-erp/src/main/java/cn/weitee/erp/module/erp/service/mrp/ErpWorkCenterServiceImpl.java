package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.workcenter.ErpWorkCenterSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProcessRouteStepDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpWorkCenterDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpDeviceMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProcessRouteStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpWorkCenterMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
public class ErpWorkCenterServiceImpl implements ErpWorkCenterService {

    @Resource
    private ErpWorkCenterMapper workCenterMapper;
    @Resource
    private ErpDeviceMapper deviceMapper;
    @Resource
    private ErpProcessRouteStepMapper routeStepMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ErpWorkCenterSaveReqVO reqVO) {
        validateSave(reqVO, null);
        ErpWorkCenterDO workCenter = BeanUtils.toBean(reqVO, ErpWorkCenterDO.class)
                .setEnableDeviceDispatch(Boolean.TRUE.equals(reqVO.getEnableDeviceDispatch()));
        workCenterMapper.insert(workCenter);
        return workCenter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ErpWorkCenterSaveReqVO reqVO) {
        ErpWorkCenterDO existed = validateExists(reqVO.getId());
        validateSave(reqVO, existed.getId());
        ErpWorkCenterDO workCenter = BeanUtils.toBean(reqVO, ErpWorkCenterDO.class)
                .setEnableDeviceDispatch(Boolean.TRUE.equals(reqVO.getEnableDeviceDispatch()));
        workCenterMapper.updateById(workCenter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validateExists(id);
        // 工作中心下存在设备时不允许删除，避免设备台账失去归属
        if (deviceMapper.selectCount(ErpDeviceDO::getWorkCenterId, id) > 0) {
            throw exception(WORK_CENTER_REFERENCED);
        }
        // 工艺路线工序引用该工作中心时不允许删除，只能停用
        if (routeStepMapper.selectCount(ErpProcessRouteStepDO::getWorkCenterId, id) > 0) {
            throw exception(WORK_CENTER_REFERENCED);
        }
        workCenterMapper.deleteById(id);
    }

    @Override
    public ErpWorkCenterDO get(Long id) {
        return validateExists(id);
    }

    @Override
    public PageResult<ErpWorkCenterDO> getPage(ErpWorkCenterPageReqVO reqVO) {
        return workCenterMapper.selectPage(reqVO);
    }

    @Override
    public List<ErpWorkCenterDO> getEnabledList() {
        return workCenterMapper.selectEnabledList();
    }

    @Override
    public List<ErpWorkCenterDO> getWorkCenterList(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return workCenterMapper.selectByIds(ids);
    }

    private void validateSave(ErpWorkCenterSaveReqVO reqVO, Long id) {
        ErpWorkCenterDO byCode = workCenterMapper.selectByCenterCode(reqVO.getCenterCode());
        if (byCode != null && !byCode.getId().equals(id)) {
            throw exception(WORK_CENTER_CODE_DUPLICATE);
        }
    }

    private ErpWorkCenterDO validateExists(Long id) {
        ErpWorkCenterDO workCenter = workCenterMapper.selectById(id);
        if (workCenter == null) {
            throw exception(WORK_CENTER_NOT_EXISTS);
        }
        return workCenter;
    }

}
