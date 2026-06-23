package cn.iocoder.yudao.module.erp.service.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.arstatement.ErpArStatementSummaryRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpArStatementDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleOutDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.sale.ErpSaleReturnDO;

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
