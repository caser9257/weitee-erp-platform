package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.assemble.ErpStockAssemblePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockAssembleItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpStockAssembleMapper extends BaseMapperX<ErpStockAssembleDO> {

    default PageResult<ErpStockAssembleDO> selectPage(ErpStockAssemblePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpStockAssembleDO> query = new MPJLambdaWrapperX<ErpStockAssembleDO>()
                .likeIfPresent(ErpStockAssembleDO::getNo, reqVO.getNo())
                .eqIfPresent(ErpStockAssembleDO::getActionType, reqVO.getActionType())
                .eqIfPresent(ErpStockAssembleDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(ErpStockAssembleDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ErpStockAssembleDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpStockAssembleDO::getId);
        if (reqVO.getDetailProductId() != null) {
            query.leftJoin(ErpStockAssembleItemDO.class,
                    ErpStockAssembleItemDO::getAssembleId, ErpStockAssembleDO::getId)
                    .eq(ErpStockAssembleItemDO::getProductId, reqVO.getDetailProductId())
                    .groupBy(ErpStockAssembleDO::getId);
        }
        return selectJoinPage(reqVO, ErpStockAssembleDO.class, query);
    }

    default ErpStockAssembleDO selectByNo(String no) {
        return selectOne(ErpStockAssembleDO::getNo, no);
    }

    default int updateStatusIfMatch(Long id, Integer status, Integer targetStatus) {
        return update(new ErpStockAssembleDO().setStatus(targetStatus),
                new LambdaUpdateWrapper<ErpStockAssembleDO>()
                        .eq(ErpStockAssembleDO::getId, id)
                        .eq(ErpStockAssembleDO::getStatus, status));
    }
}
