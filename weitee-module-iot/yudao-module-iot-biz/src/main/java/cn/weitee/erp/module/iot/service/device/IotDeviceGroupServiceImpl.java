package cn.weitee.erp.module.iot.service.device;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.iot.controller.admin.device.vo.group.IotDeviceGroupPageReqVO;
import cn.weitee.erp.module.iot.controller.admin.device.vo.group.IotDeviceGroupSaveReqVO;
import cn.weitee.erp.module.iot.dal.dataobject.device.IotDeviceGroupDO;
import cn.weitee.erp.module.iot.dal.mysql.device.IotDeviceGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.iot.enums.ErrorCodeConstants.DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS;
import static cn.weitee.erp.module.iot.enums.ErrorCodeConstants.DEVICE_GROUP_NOT_EXISTS;

/**
 * IoT 设备分组 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class IotDeviceGroupServiceImpl implements IotDeviceGroupService {

    @Resource
    private IotDeviceGroupMapper iotDeviceGroupMapper;

    @Resource
    private IotDeviceService deviceService;

    @Override
    public Long createDeviceGroup(IotDeviceGroupSaveReqVO createReqVO) {
        // 插入
        IotDeviceGroupDO deviceGroup = BeanUtils.toBean(createReqVO, IotDeviceGroupDO.class);
        iotDeviceGroupMapper.insert(deviceGroup);
        // 返回
        return deviceGroup.getId();
    }

    @Override
    public void updateDeviceGroup(IotDeviceGroupSaveReqVO updateReqVO) {
        // 校验存在
        validateDeviceGroupExists(updateReqVO.getId());
        // 更新
        IotDeviceGroupDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceGroupDO.class);
        iotDeviceGroupMapper.updateById(updateObj);
    }

    @Override
    public void deleteDeviceGroup(Long id) {
        // 1.1 校验存在
        validateDeviceGroupExists(id);
        // 1.2 校验是否存在设备
        if (deviceService.getDeviceCountByGroupId(id) > 0) {
            throw exception(DEVICE_GROUP_DELETE_FAIL_DEVICE_EXISTS);
        }

        // 删除
        iotDeviceGroupMapper.deleteById(id);
    }

    @Override
    public IotDeviceGroupDO validateDeviceGroupExists(Long id) {
        IotDeviceGroupDO group = iotDeviceGroupMapper.selectById(id);
        if (group == null) {
            throw exception(DEVICE_GROUP_NOT_EXISTS);
        }
        return group;
    }

    @Override
    public IotDeviceGroupDO getDeviceGroup(Long id) {
        return iotDeviceGroupMapper.selectById(id);
    }

    @Override
    public IotDeviceGroupDO getDeviceGroupByName(String name) {
        return iotDeviceGroupMapper.selectByName(name);
    }

    @Override
    public PageResult<IotDeviceGroupDO> getDeviceGroupPage(IotDeviceGroupPageReqVO pageReqVO) {
        return iotDeviceGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<IotDeviceGroupDO> getDeviceGroupListByStatus(Integer status) {
        return iotDeviceGroupMapper.selectListByStatus(status);
    }

}