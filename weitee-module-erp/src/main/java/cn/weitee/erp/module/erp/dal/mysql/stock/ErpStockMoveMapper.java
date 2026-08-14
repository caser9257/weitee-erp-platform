package cn.weitee.erp.module.erp.dal.mysql.stock;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.stock.vo.move.ErpStockMovePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveDO;
import cn.weitee.erp.module.erp.dal.dataobject.stock.ErpStockMoveItemDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 库存调拨单 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpStockMoveMapper extends BaseMapperX<ErpStockMoveDO> {

    default PageResult<ErpStockMoveDO> selectPage(ErpStockMovePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpStockMoveDO> query = new MPJLambdaWrapperX<ErpStockMoveDO>()
                .likeIfPresent(ErpStockMoveDO::getNo, reqVO.getNo())
                .betweenIfPresent(ErpStockMoveDO::getMoveTime, reqVO.getMoveTime())
                .eqIfPresent(ErpStockMoveDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpStockMoveDO::getRemark, reqVO.getRemark())
                .eqIfPresent(ErpStockMoveDO::getCreator, reqVO.getCreator())
                .orderByDesc(ErpStockMoveDO::getId);
        if (reqVO.getFromWarehouseId() != null || reqVO.getProductId() != null) {
            query.leftJoin(ErpStockMoveItemDO.class, ErpStockMoveItemDO::getMoveId, ErpStockMoveDO::getId)
                    .eq(reqVO.getFromWarehouseId() != null, ErpStockMoveItemDO::getFromWarehouseId, reqVO.getFromWarehouseId())
                    .eq(reqVO.getProductId() != null, ErpStockMoveItemDO::getProductId, reqVO.getProductId())
                    .groupBy(ErpStockMoveDO::getId); // 避免 1 对多查询，产生相同的 1
        }
        return selectJoinPage(reqVO, ErpStockMoveDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpStockMoveDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpStockMoveDO>()
                .eq(ErpStockMoveDO::getId, id).eq(ErpStockMoveDO::getStatus, status));
    }

    default int updateByIdStatusAndProcessInstanceId(Long id, Integer status, String processInstanceId,
                                                      ErpStockMoveDO updateObj) {
        return update(null, new LambdaUpdateWrapper<ErpStockMoveDO>()
                .set(ErpStockMoveDO::getStatus, updateObj.getStatus())
                .set(ErpStockMoveDO::getProcessInstanceId, updateObj.getProcessInstanceId())
                .eq(ErpStockMoveDO::getId, id)
                .eq(ErpStockMoveDO::getStatus, status)
                .eq(ErpStockMoveDO::getProcessInstanceId, processInstanceId));
    }

    default int resetStatusToDraftByBpm(Long id, String processInstanceId) {
        return update(null, new LambdaUpdateWrapper<ErpStockMoveDO>()
                        .set(ErpStockMoveDO::getStatus, 0)
                        .set(ErpStockMoveDO::getProcessInstanceId, null)
                        .eq(ErpStockMoveDO::getId, id)
                        .eq(ErpStockMoveDO::getStatus, 10)
                        .eq(ErpStockMoveDO::getProcessInstanceId, processInstanceId));
    }

    default ErpStockMoveDO selectByNo(String no) {
        return selectOne(ErpStockMoveDO::getNo, no);
    }

}
