package cn.weitee.erp.module.erp.service.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDevicePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDeviceSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpProductionOrderStepDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpDeviceMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionOrderStepMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpWorkCenterMapper;
import cn.weitee.erp.module.erp.enums.mrp.ErpDeviceStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Arrays;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.*;

@Service
public class ErpDeviceServiceImpl implements ErpDeviceService {

    @Resource
    private ErpDeviceMapper deviceMapper;
    @Resource
    private ErpWorkCenterMapper workCenterMapper;
    @Resource
    private ErpProductionOrderStepMapper productionOrderStepMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ErpDeviceSaveReqVO reqVO) {
        validateSave(reqVO, null);
        ErpDeviceDO device = BeanUtils.toBean(reqVO, ErpDeviceDO.class);
        if (device.getDeviceStatus() == null) {
            device.setDeviceStatus(ErpDeviceStatusEnum.IDLE.getStatus());
        }
        deviceMapper.insert(device);
        return device.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ErpDeviceSaveReqVO reqVO) {
        ErpDeviceDO existed = validateExists(reqVO.getId());
        validateSave(reqVO, existed.getId());
        deviceMapper.updateById(BeanUtils.toBean(reqVO, ErpDeviceDO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        validateExists(id);
        // 已被生产工单工序快照引用的设备不允许删除，保证执行数据可追溯
        if (productionOrderStepMapper.selectCount(ErpProductionOrderStepDO::getDeviceId, id) > 0) {
            throw exception(DEVICE_REFERENCED);
        }
        deviceMapper.deleteById(id);
    }

    @Override
    public ErpDeviceDO get(Long id) {
        return validateExists(id);
    }

    @Override
    public PageResult<ErpDeviceDO> getPage(ErpDevicePageReqVO reqVO) {
        return deviceMapper.selectPage(reqVO);
    }

    private void validateSave(ErpDeviceSaveReqVO reqVO, Long id) {
        ErpDeviceDO byCode = deviceMapper.selectByDeviceCode(reqVO.getDeviceCode());
        if (byCode != null && !byCode.getId().equals(id)) {
            throw exception(DEVICE_CODE_DUPLICATE);
        }
        if (reqVO.getWorkCenterId() != null && workCenterMapper.selectById(reqVO.getWorkCenterId()) == null) {
            throw exception(WORK_CENTER_NOT_EXISTS);
        }
        if (reqVO.getDeviceStatus() != null && Arrays.stream(ErpDeviceStatusEnum.values())
                .noneMatch(item -> item.getStatus().equals(reqVO.getDeviceStatus()))) {
            throw exception(DEVICE_STATUS_INVALID);
        }
        // 停用设备不允许分配工作中心，避免执行时被误排
        if (ErpDeviceStatusEnum.DISABLED.getStatus().equals(reqVO.getDeviceStatus()) && reqVO.getWorkCenterId() != null) {
            throw exception(DEVICE_STATUS_INVALID);
        }
    }

    private ErpDeviceDO validateExists(Long id) {
        ErpDeviceDO device = deviceMapper.selectById(id);
        if (device == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }
        return device;
    }

}
