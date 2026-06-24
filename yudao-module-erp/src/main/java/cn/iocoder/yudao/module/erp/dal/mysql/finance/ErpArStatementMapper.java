package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpArStatementDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 应收台账 Mapper
 *
 * @author system
 */
@Mapper
public interface ErpArStatementMapper extends BaseMapperX<ErpArStatementDO> {

    /**
     * 分页查询应收台账
     */
    default PageResult<ErpArStatementDO> selectPage(cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpArStatementDO>()
                .likeIfPresent(ErpArStatementDO::getStatementNo, reqVO.getStatementNo())
                .eqIfPresent(ErpArStatementDO::getBizType, reqVO.getBizType())
                .likeIfPresent(ErpArStatementDO::getBizNo, reqVO.getBizNo())
                .eqIfPresent(ErpArStatementDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(ErpArStatementDO::getSourceOrderId, reqVO.getSourceOrderId())
                .eqIfPresent(ErpArStatementDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(ErpArStatementDO::getCurrencyCode, reqVO.getCurrencyCode())
                .eqIfPresent(ErpArStatementDO::getInvoiceStatus, reqVO.getInvoiceStatus())
                .eqIfPresent(ErpArStatementDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ErpArStatementDO::getBizDate, reqVO.getBizDate())
                .betweenIfPresent(ErpArStatementDO::getDueDate, reqVO.getDueDate())
                .orderByDesc(ErpArStatementDO::getId));
    }

    /**
     * 根据业务类型和业务ID查询台账
     */
    default ErpArStatementDO selectByBizTypeAndBizId(Integer bizType, Long bizId) {
        return selectOne(ErpArStatementDO::getBizType, bizType, ErpArStatementDO::getBizId, bizId);
    }

    /**
     * 根据业务类型和业务ID列表查询台账
     */
    default List<ErpArStatementDO> selectListByBizTypeAndBizIds(Integer bizType, Collection<Long> bizIds) {
        return selectList(new LambdaQueryWrapperX<ErpArStatementDO>()
                .eq(ErpArStatementDO::getBizType, bizType)
                .inIfPresent(ErpArStatementDO::getBizId, bizIds)
                .orderByAsc(ErpArStatementDO::getId));
    }

    /**
     * 根据来源订单ID查询台账列表
     */
    default List<ErpArStatementDO> selectListBySourceOrderId(Long sourceOrderId) {
        return selectList(ErpArStatementDO::getSourceOrderId, sourceOrderId);
    }

    /**
     * 更新发票信息
     */
    default int updateInvoiceById(Long id, Integer invoiceStatus, String invoiceNo, BigDecimal invoiceAmount) {
        return update(null, new LambdaUpdateWrapper<ErpArStatementDO>()
                .eq(ErpArStatementDO::getId, id)
                .set(ErpArStatementDO::getInvoiceStatus, invoiceStatus)
                .set(ErpArStatementDO::getInvoiceNo, invoiceNo)
                .set(ErpArStatementDO::getInvoiceAmount, invoiceAmount));
    }

    /**
     * 更新已收金额和状态
     */
    default int updateReceivedAmountById(Long id, BigDecimal receivedAmount, BigDecimal remainAmount, Integer status) {
        return update(null, new LambdaUpdateWrapper<ErpArStatementDO>()
                .eq(ErpArStatementDO::getId, id)
                .set(ErpArStatementDO::getReceivedAmount, receivedAmount)
                .set(ErpArStatementDO::getRemainAmount, remainAmount)
                .set(ErpArStatementDO::getStatus, status));
    }

    /**
     * 按来源订单ID汇总应收台账金额（SQL 聚合，避免全量加载到内存）
     *
     * @param sourceOrderId 来源订单ID
     * @return 汇总结果数组：[0]=count, [1]=totalAmount, [2]=totalReceivedAmount, [3]=totalRemainAmount
     */
    default java.util.Map<String, BigDecimal> selectSummaryBySourceOrderId(Long sourceOrderId) {
        // 使用 selectList 只查必要字段，避免加载全部列
        java.util.List<ErpArStatementDO> list = selectList(new LambdaQueryWrapperX<ErpArStatementDO>()
                .eq(ErpArStatementDO::getSourceOrderId, sourceOrderId)
                .ne(ErpArStatementDO::getStatus, 3) // 排除已关闭
                .select(ErpArStatementDO::getAmount, ErpArStatementDO::getReceivedAmount, ErpArStatementDO::getRemainAmount));
        java.util.Map<String, BigDecimal> result = new java.util.HashMap<>();
        result.put("count", new BigDecimal(list.size()));
        result.put("totalAmount", list.stream().map(s -> s.getAmount() != null ? s.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("totalReceivedAmount", list.stream().map(s -> s.getReceivedAmount() != null ? s.getReceivedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        result.put("totalRemainAmount", list.stream().map(s -> s.getRemainAmount() != null ? s.getRemainAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }

}
