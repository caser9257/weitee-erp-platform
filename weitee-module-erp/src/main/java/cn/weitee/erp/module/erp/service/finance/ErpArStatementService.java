package cn.weitee.erp.module.erp.service.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementSummaryRespVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpArStatementDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleReturnDO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 应收台账 Service 接口
 *
 * @author system
 */
public interface ErpArStatementService {

    /**
     * 创建应收台账（销售出库）
     *
     * @param saleOut 销售出库单
     */
    void createStatementForSaleOut(ErpSaleOutDO saleOut);

    /**
     * 创建应收台账（销售退货，负向）
     *
     * @param saleReturn 销售退货单
     */
    void createStatementForSaleReturn(ErpSaleReturnDO saleReturn);

    /**
     * 关闭台账（反审核时）
     *
     * @param bizType 业务类型
     * @param bizId 业务单据ID
     * @param remark 备注
     */
    void closeStatementByBiz(Integer bizType, Long bizId, String remark);

    /**
     * 刷新台账金额
     *
     * @param statementIds 台账ID列表
     */
    void refreshStatementAmountByIds(Collection<Long> statementIds);

    /**
     * 按业务单据刷新台账已收/剩余金额与状态
     *
     * 已收金额来源于该业务单据下已审核收款单的收款单项汇总，幂等可重放
     *
     * @param bizType 业务类型（销售出库/销售退货）
     * @param bizIds 业务单据ID集合
     */
    void refreshStatementAmountByBizIds(Integer bizType, Collection<Long> bizIds);

    /**
     * 写入收款分配事实明细（审批通过时）
     *
     * @param bizType 业务类型
     * @param bizId 业务单据ID
     * @param refId 收款单ID
     * @param refNo 收款单号
     * @param amount 本次分配金额（方向随台账金额方向）
     * @param remark 备注
     */
    void createReceiptAllocatedItem(Integer bizType, Long bizId, Long refId,
                                    String refNo, BigDecimal amount, String remark);

    /**
     * 写入收款退回事实明细（反审核/作废回滚时）
     *
     * @param bizType 业务类型
     * @param bizId 业务单据ID
     * @param refId 收款单ID
     * @param refNo 收款单号
     * @param amount 本次退回金额（与分配方向相反）
     * @param remark 备注
     */
    void createReceiptReturnedItem(Integer bizType, Long bizId, Long refId,
                                   String refNo, BigDecimal amount, String remark);

    /**
     * 分页查询应收台账
     *
     * @param reqVO 分页请求参数
     * @return 应收台账分页结果
     */
    PageResult<ErpArStatementRespVO> getStatementPage(ErpArStatementPageReqVO reqVO);

    /**
     * 获取台账详情
     *
     * @param id 台账ID
     * @return 台账详情
     */
    ErpArStatementRespVO getStatement(Long id);

    /**
     * 获取应收台账汇总（按客户）
     *
     * @param customerId 客户ID（可选）
     * @return 汇总列表
     */
    List<ErpArStatementSummaryRespVO> getStatementSummary(Long customerId);

    /**
     * 获取台账
     *
     * @param id 台账ID
     * @return 台账
     */
    ErpArStatementDO getArStatement(Long id);

    /**
     * 根据业务类型和业务ID获取台账
     *
     * @param bizType 业务类型
     * @param bizId 业务单据ID
     * @return 台账
     */
    ErpArStatementDO getArStatementByBiz(Integer bizType, Long bizId);

    /**
     * 批量获取台账
     *
     * @param ids 台账ID列表
     * @return 台账列表
     */
    List<ErpArStatementDO> getArStatementListByIds(Collection<Long> ids);

}
