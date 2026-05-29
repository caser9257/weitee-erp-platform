package cn.iocoder.yudao.module.erp.dal.mysql.rd;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.rd.ErpRdBomDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpRdBomMapper extends BaseMapperX<ErpRdBomDO> {

    default PageResult<ErpRdBomDO> selectPage(ErpRdBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpRdBomDO>()
                .eqIfPresent(ErpRdBomDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpRdBomDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpRdBomDO::getBomCode, reqVO.getBomCode())
                .orderByDesc(ErpRdBomDO::getId));
    }

}
