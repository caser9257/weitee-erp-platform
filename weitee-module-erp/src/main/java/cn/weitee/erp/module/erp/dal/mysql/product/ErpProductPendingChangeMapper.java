package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductPendingChangeDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ErpProductPendingChangeMapper extends BaseMapperX<ErpProductPendingChangeDO> {

    default ErpProductPendingChangeDO selectByProductId(Long productId) {
        return selectOne(ErpProductPendingChangeDO::getProductId, productId);
    }

    default List<ErpProductPendingChangeDO> selectListByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<ErpProductPendingChangeDO>()
                .in(ErpProductPendingChangeDO::getProductId, productIds));
    }

    /**
     * 查询批次的全部暂存记录（批量导入归组，按物料编号升序保证审批详情展示稳定）
     */
    default List<ErpProductPendingChangeDO> selectListByBatchId(Long batchId) {
        return selectList(new LambdaQueryWrapperX<ErpProductPendingChangeDO>()
                .eq(ErpProductPendingChangeDO::getBatchId, batchId)
                .orderByAsc(ErpProductPendingChangeDO::getProductId));
    }

    /**
     * 统计批次成员数（列表 VO 批次提示用，仅取 count）
     */
    default Long selectCountByBatchId(Long batchId) {
        return selectCount(new LambdaQueryWrapperX<ErpProductPendingChangeDO>()
                .eq(ErpProductPendingChangeDO::getBatchId, batchId));
    }

    /**
     * 物理删除物料的暂存记录。
     * 必须物理删除：uk_product_id 为不含 deleted 的物理唯一约束，
     * 若走逻辑删除，被删行仍占用唯一键，同一物料再次提交将触发 DuplicateKey。
     */
    @Delete("DELETE FROM erp_product_pending_change WHERE product_id = #{productId}")
    void physicalDeleteByProductId(Long productId);

}
