package cn.weitee.erp.module.erp.dal.mysql.mrp;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.mrp.vo.device.ErpDevicePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.mrp.ErpDeviceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpDeviceMapper extends BaseMapperX<ErpDeviceDO> {

    default PageResult<ErpDeviceDO> selectPage(ErpDevicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpDeviceDO>()
                .likeIfPresent(ErpDeviceDO::getDeviceCode, reqVO.getDeviceCode())
                .likeIfPresent(ErpDeviceDO::getDeviceName, reqVO.getDeviceName())
                .eqIfPresent(ErpDeviceDO::getWorkCenterId, reqVO.getWorkCenterId())
                .eqIfPresent(ErpDeviceDO::getDeviceStatus, reqVO.getDeviceStatus())
                .orderByDesc(ErpDeviceDO::getId));
    }

    default ErpDeviceDO selectByDeviceCode(String deviceCode) {
        return selectOne(ErpDeviceDO::getDeviceCode, deviceCode);
    }
}
