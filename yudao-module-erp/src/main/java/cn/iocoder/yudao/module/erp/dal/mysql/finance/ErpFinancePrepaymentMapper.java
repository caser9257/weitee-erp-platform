package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.prepayment.ErpFinancePrepaymentPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinancePrepaymentDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpFinancePrepaymentMapper extends BaseMapperX<ErpFinancePrepaymentDO> {

    default PageResult<ErpFinancePrepaymentDO> selectPage(ErpFinancePrepaymentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinancePrepaymentDO>()
                .likeIfPresent(ErpFinancePrepaymentDO::getNo, reqVO.getNo())
                .betweenIfPresent(ErpFinancePrepaymentDO::getPrepaymentTime, reqVO.getPrepaymentTime())
                .eqIfPresent(ErpFinancePrepaymentDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpFinancePrepaymentDO::getFinanceUserId, reqVO.getFinanceUserId())
                .eqIfPresent(ErpFinancePrepaymentDO::getAccountId, reqVO.getAccountId())
                .eqIfPresent(ErpFinancePrepaymentDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ErpFinancePrepaymentDO::getRemark, reqVO.getRemark())
                .orderByDesc(ErpFinancePrepaymentDO::getId));
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinancePrepaymentDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinancePrepaymentDO>()
                .eq(ErpFinancePrepaymentDO::getId, id)
                .eq(ErpFinancePrepaymentDO::getStatus, status));
    }

    default ErpFinancePrepaymentDO selectByNo(String no) {
        return selectOne(ErpFinancePrepaymentDO::getNo, no);
    }

}
