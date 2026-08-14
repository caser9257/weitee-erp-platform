package cn.weitee.erp.module.erp.dal.mysql.finance;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.weitee.erp.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

@Mapper
public interface ErpFinanceExpenseMapper extends BaseMapperX<ErpFinanceExpenseDO> {

    default PageResult<ErpFinanceExpenseDO> selectPage(ErpFinanceExpensePageReqVO reqVO) {
        return selectJoinPage(reqVO, ErpFinanceExpenseDO.class, buildPageQuery(reqVO));
    }

    default PageResult<ErpFinanceExpenseDO> selectPageByDeptIds(ErpFinanceExpensePageReqVO reqVO, Collection<Long> deptIds) {
        return selectJoinPage(reqVO, ErpFinanceExpenseDO.class, buildPageQuery(reqVO)
                .in(ErpFinanceExpenseDO::getDeptId, deptIds));
    }

    private MPJLambdaWrapperX<ErpFinanceExpenseDO> buildPageQuery(ErpFinanceExpensePageReqVO reqVO) {
        return new MPJLambdaWrapperX<ErpFinanceExpenseDO>()
                .likeIfPresent(ErpFinanceExpenseDO::getNo, reqVO.getNo())
                .betweenIfPresent(ErpFinanceExpenseDO::getExpenseTime, reqVO.getExpenseTime())
                .eqIfPresent(ErpFinanceExpenseDO::getExpenseType, reqVO.getExpenseType())
                .eqIfPresent(ErpFinanceExpenseDO::getRdAccountingType, reqVO.getRdAccountingType())
                .eqIfPresent(ErpFinanceExpenseDO::getDeptId, reqVO.getDeptId())
                .eqIfPresent(ErpFinanceExpenseDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ErpFinanceExpenseDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpFinanceExpenseDO::getCreator, reqVO.getCreator())
                .eqIfPresent(ErpFinanceExpenseDO::getFinanceUserId, reqVO.getFinanceUserId())
                .eqIfPresent(ErpFinanceExpenseDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(ErpFinanceExpenseDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpFinanceExpenseDO::getRemark, reqVO.getRemark())
                .orderByDesc(ErpFinanceExpenseDO::getId);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinanceExpenseDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getId, id)
                .eq(ErpFinanceExpenseDO::getStatus, status));
    }

    default int clearProcessInstanceId(Long id, String processInstanceId) {
        return update(null, new LambdaUpdateWrapper<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getId, id)
                .eq(ErpFinanceExpenseDO::getProcessInstanceId, processInstanceId)
                .set(ErpFinanceExpenseDO::getProcessInstanceId, null));
    }

    default int resetStatusToDraftByBpm(Long id, String processInstanceId) {
        return update(new LambdaUpdateWrapper<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getId, id)
                .eq(ErpFinanceExpenseDO::getStatus, ErpAuditStatus.PROCESS.getStatus())
                .eq(ErpFinanceExpenseDO::getProcessInstanceId, processInstanceId)
                .set(ErpFinanceExpenseDO::getStatus, ErpAuditStatus.DRAFT.getStatus())
                .set(ErpFinanceExpenseDO::getProcessInstanceId, null));
    }

    default ErpFinanceExpenseDO selectByNo(String no) {
        return selectOne(ErpFinanceExpenseDO::getNo, no);
    }

    default java.util.List<ErpFinanceExpenseDO> selectListByResearchExpense(ErpFinanceResearchExpenseSummaryReqVO reqVO) {
        return selectList(new MPJLambdaWrapperX<ErpFinanceExpenseDO>()
                .eqIfPresent(ErpFinanceExpenseDO::getProjectId, reqVO.getProjectId())
                .eqIfPresent(ErpFinanceExpenseDO::getResearchCategory, reqVO.getResearchCategory())
                .geIfPresent(ErpFinanceExpenseDO::getExpenseTime, reqVO.getBeginTime())
                .leIfPresent(ErpFinanceExpenseDO::getExpenseTime, reqVO.getEndTime())
                .eq(ErpFinanceExpenseDO::getStatus, ErpAuditStatus.APPROVE.getStatus())
                .orderByDesc(ErpFinanceExpenseDO::getId));
    }

}
