package cn.weitee.erp.module.erp.dal.mysql.rd;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.rd.vo.bom.ErpRdBomPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.rd.ErpRdBomDO;
import cn.hutool.core.util.StrUtil;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpRdBomMapper extends BaseMapperX<ErpRdBomDO> {

    default ErpRdBomDO selectByIdentity(Long productId, String bomCode, String version) {
        LambdaQueryWrapperX<ErpRdBomDO> wrapper = new LambdaQueryWrapperX<ErpRdBomDO>()
                .eq(ErpRdBomDO::getProductId, productId)
                .eq(ErpRdBomDO::getBomCode, bomCode.trim());
        if (StrUtil.isBlank(version)) {
            wrapper.and(query -> query.isNull(ErpRdBomDO::getVersion)
                    .or()
                    .eq(ErpRdBomDO::getVersion, ""));
        } else {
            wrapper.eq(ErpRdBomDO::getVersion, version.trim());
        }
        return selectOne(wrapper.orderByDesc(ErpRdBomDO::getId).last("LIMIT 1"));
    }

    default PageResult<ErpRdBomDO> selectPage(ErpRdBomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpRdBomDO>()
                .eqIfPresent(ErpRdBomDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpRdBomDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpRdBomDO::getBomCode, reqVO.getBomCode())
                .orderByDesc(ErpRdBomDO::getId));
    }

}
