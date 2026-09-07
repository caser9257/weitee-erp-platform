package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.voucherfailure.ErpFinanceVoucherFailurePageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceVoucherFailureDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * ERP 凭证生成失败记录 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpFinanceVoucherFailureMapper extends BaseMapperX<ErpFinanceVoucherFailureDO> {

    default ErpFinanceVoucherFailureDO selectPendingByBiz(Integer bizType, Long bizId) {
        return selectOne(new LambdaQueryWrapperX<ErpFinanceVoucherFailureDO>()
                .eq(ErpFinanceVoucherFailureDO::getBizType, bizType)
                .eq(ErpFinanceVoucherFailureDO::getBizId, bizId)
                .eq(ErpFinanceVoucherFailureDO::getStatus,
                        cn.weitee.erp.module.erp.enums.ErpFinanceVoucherFailureStatusEnum.PENDING.getStatus())
                .orderByAsc(ErpFinanceVoucherFailureDO::getId)
                .last("LIMIT 1"));
    }

    default List<ErpFinanceVoucherFailureDO> selectListByBiz(Integer bizType, Collection<Long> bizIds) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceVoucherFailureDO>()
                .eq(ErpFinanceVoucherFailureDO::getBizType, bizType)
                .inIfPresent(ErpFinanceVoucherFailureDO::getBizId, bizIds));
    }

    default PageResult<ErpFinanceVoucherFailureDO> selectPage(ErpFinanceVoucherFailurePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceVoucherFailureDO>()
                .eqIfPresent(ErpFinanceVoucherFailureDO::getBizType, reqVO.getBizType())
                .eqIfPresent(ErpFinanceVoucherFailureDO::getBizId, reqVO.getBizId())
                .eqIfPresent(ErpFinanceVoucherFailureDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpFinanceVoucherFailureDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpFinanceVoucherFailureDO::getId));
    }

    /**
     * CAS 状态更新：仅当记录仍处于 expectStatus 时才更新，防止并发重试/确认互相覆盖
     */
    default int updateStatusByIdAndStatus(Long id, Integer expectStatus, ErpFinanceVoucherFailureDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceVoucherFailureDO>()
                .eq(ErpFinanceVoucherFailureDO::getId, id)
                .eq(ErpFinanceVoucherFailureDO::getStatus, expectStatus));
    }

}
