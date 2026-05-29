package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceExpensePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.expense.ErpFinanceResearchExpenseSummaryReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceExpenseDO;
import cn.iocoder.yudao.module.erp.enums.ErpAuditStatus;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpFinanceExpenseMapper extends BaseMapperX<ErpFinanceExpenseDO> {

    default PageResult<ErpFinanceExpenseDO> selectPage(ErpFinanceExpensePageReqVO reqVO) {
        MPJLambdaWrapperX<ErpFinanceExpenseDO> query = new MPJLambdaWrapperX<ErpFinanceExpenseDO>()
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
        return selectJoinPage(reqVO, ErpFinanceExpenseDO.class, query);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinanceExpenseDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getId, id)
                .eq(ErpFinanceExpenseDO::getStatus, status));
    }

    default int clearProcessInstanceId(Long id) {
        return update(null, new LambdaUpdateWrapper<ErpFinanceExpenseDO>()
                .eq(ErpFinanceExpenseDO::getId, id)
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
