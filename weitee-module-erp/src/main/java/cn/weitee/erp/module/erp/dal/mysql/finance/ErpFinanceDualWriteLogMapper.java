package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.dualwrite.ErpFinanceDualWriteLogPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceDualWriteLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 双写日志 Mapper
 */
@Mapper
public interface ErpFinanceDualWriteLogMapper extends BaseMapperX<ErpFinanceDualWriteLogDO> {

    default PageResult<ErpFinanceDualWriteLogDO> selectPage(ErpFinanceDualWriteLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceDualWriteLogDO>()
                .eqIfPresent(ErpFinanceDualWriteLogDO::getSourceVoucherId, reqVO.getSourceVoucherId())
                .eqIfPresent(ErpFinanceDualWriteLogDO::getTargetVoucherId, reqVO.getTargetVoucherId())
                .eqIfPresent(ErpFinanceDualWriteLogDO::getSourceLedgerId, reqVO.getSourceLedgerId())
                .eqIfPresent(ErpFinanceDualWriteLogDO::getTargetLedgerId, reqVO.getTargetLedgerId())
                .eqIfPresent(ErpFinanceDualWriteLogDO::getBizType, reqVO.getBizType())
                .eqIfPresent(ErpFinanceDualWriteLogDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceDualWriteLogDO::getId));
    }

    default ErpFinanceDualWriteLogDO selectLatestByBizTypeAndBizId(Integer bizType, Long bizId, Integer status) {
        return selectList(new LambdaQueryWrapperX<ErpFinanceDualWriteLogDO>()
                .eq(ErpFinanceDualWriteLogDO::getBizType, bizType)
                .eq(ErpFinanceDualWriteLogDO::getBizId, bizId)
                .eq(ErpFinanceDualWriteLogDO::getStatus, status)
                .orderByDesc(ErpFinanceDualWriteLogDO::getId))
                .stream()
                .findFirst()
                .orElse(null);
    }
}
