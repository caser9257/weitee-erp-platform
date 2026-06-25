package cn.weitee.erp.module.iot.service.device;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointPageReqVO;
import cn.weitee.erp.module.iot.controller.admin.device.vo.modbus.IotDeviceModbusPointSaveReqVO;
import cn.weitee.erp.module.iot.dal.dataobject.device.IotDeviceModbusPointDO;
import cn.weitee.erp.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.weitee.erp.module.iot.dal.mysql.device.IotDeviceModbusPointMapper;
import cn.weitee.erp.module.iot.service.thingmodel.IotThingModelService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.framework.common.util.collection.CollectionUtils.convertMultiMap;
import static cn.weitee.erp.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 设备 Modbus 点位配置 Service 实现类
 *
 * @author WeTai
 */
@Service
@Validated
public class IotDeviceModbusPointServiceImpl implements IotDeviceModbusPointService {

    @Resource
    private IotDeviceModbusPointMapper iotDeviceModbusPointMapper;

    @Resource
    private IotDeviceService deviceService;

    @Resource
    private IotThingModelService thingModelService;

    @Override
    public Long createDeviceModbusPoint(IotDeviceModbusPointSaveReqVO createReqVO) {
        // 1.1 校验设备存在
        deviceService.validateDeviceExists(createReqVO.getDeviceId());
        // 1.2 校验物模型属性存在
        IotThingModelDO thingModel = validateThingModelExists(createReqVO.getThingModelId());
        // 1.3 校验同一设备下点位唯一性（基于 identifier）
        validateDeviceModbusPointUnique(createReqVO.getDeviceId(), thingModel.getIdentifier(), null);

        // 2. 插入
        IotDeviceModbusPointDO modbusPoint = BeanUtils.toBean(createReqVO, IotDeviceModbusPointDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName()));
        iotDeviceModbusPointMapper.insert(modbusPoint);
        return modbusPoint.getId();
    }

    @Override
    public void updateDeviceModbusPoint(IotDeviceModbusPointSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateDeviceModbusPointExists(updateReqVO.getId());
        // 1.2 校验设备存在
        deviceService.validateDeviceExists(updateReqVO.getDeviceId());
        // 1.3 校验物模型属性存在
        IotThingModelDO thingModel = validateThingModelExists(updateReqVO.getThingModelId());
        // 1.4 校验同一设备下点位唯一性
        validateDeviceModbusPointUnique(updateReqVO.getDeviceId(), thingModel.getIdentifier(), updateReqVO.getId());

        // 2. 更新
        IotDeviceModbusPointDO updateObj = BeanUtils.toBean(updateReqVO, IotDeviceModbusPointDO.class,
                o -> o.setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName()));
        iotDeviceModbusPointMapper.updateById(updateObj);
    }

    @Override
    public void updateDeviceModbusPointByThingModel(Long thingModelId, String identifier, String name) {
        IotDeviceModbusPointDO updateObj = new IotDeviceModbusPointDO()
                .setIdentifier(identifier).setName(name);
        iotDeviceModbusPointMapper.updateByThingModelId(thingModelId, updateObj);
    }

    private IotThingModelDO validateThingModelExists(Long id) {
        IotThingModelDO thingModel = thingModelService.getThingModel(id);
        if (thingModel == null) {
            throw exception(THING_MODEL_NOT_EXISTS);
        }
        return thingModel;
    }

    @Override
    public void deleteDeviceModbusPoint(Long id) {
        // 校验存在
        validateDeviceModbusPointExists(id);
        // 删除
        iotDeviceModbusPointMapper.deleteById(id);
    }

    private void validateDeviceModbusPointExists(Long id) {
        IotDeviceModbusPointDO point = iotDeviceModbusPointMapper.selectById(id);
        if (point == null) {
            throw exception(DEVICE_MODBUS_POINT_NOT_EXISTS);
        }
    }

    private void validateDeviceModbusPointUnique(Long deviceId, String identifier, Long excludeId) {
        IotDeviceModbusPointDO point = iotDeviceModbusPointMapper.selectByDeviceIdAndIdentifier(deviceId, identifier);
        if (point != null && ObjUtil.notEqual(point.getId(), excludeId)) {
            throw exception(DEVICE_MODBUS_POINT_EXISTS);
        }
    }

    @Override
    public IotDeviceModbusPointDO getDeviceModbusPoint(Long id) {
        return iotDeviceModbusPointMapper.selectById(id);
    }

    @Override
    public PageResult<IotDeviceModbusPointDO> getDeviceModbusPointPage(IotDeviceModbusPointPageReqVO pageReqVO) {
        return iotDeviceModbusPointMapper.selectPage(pageReqVO);
    }

    @Override
    public Map<Long, List<IotDeviceModbusPointDO>> getEnabledDeviceModbusPointMapByDeviceIds(Collection<Long> deviceIds) {
        if (CollUtil.isEmpty(deviceIds)) {
            return Collections.emptyMap();
        }
        List<IotDeviceModbusPointDO> pointList = iotDeviceModbusPointMapper.selectListByDeviceIdsAndStatus(
                deviceIds, CommonStatusEnum.ENABLE.getStatus());
        return convertMultiMap(pointList, IotDeviceModbusPointDO::getDeviceId);
    }

}
