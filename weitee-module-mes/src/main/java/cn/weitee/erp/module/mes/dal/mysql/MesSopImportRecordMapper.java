package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.mes.controller.admin.vo.sopimport.MesSopImportRecordPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopImportRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MesSopImportRecordMapper extends BaseMapperX<MesSopImportRecordDO> {

    default PageResult<MesSopImportRecordDO> selectPage(MesSopImportRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesSopImportRecordDO>()
                .eqIfPresent(MesSopImportRecordDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesSopImportRecordDO::getId));
    }
}
