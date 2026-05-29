package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.apestimate.ErpApEstimatePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpApEstimateDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface ErpApEstimateMapper extends BaseMapperX<ErpApEstimateDO> {

    default PageResult<ErpApEstimateDO> selectPage(ErpApEstimatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpApEstimateDO>()
                .likeIfPresent(ErpApEstimateDO::getEstimateNo, reqVO.getEstimateNo())
                .eqIfPresent(ErpApEstimateDO::getEstimateMonth, reqVO.getEstimateMonth())
                .likeIfPresent(ErpApEstimateDO::getSourcePurchaseInNo, reqVO.getSourcePurchaseInNo())
                .eqIfPresent(ErpApEstimateDO::getSupplierId, reqVO.getSupplierId())
                .eqIfPresent(ErpApEstimateDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpApEstimateDO::getId));
    }

    default ErpApEstimateDO selectBySourceBizTypeAndSourceBizId(Integer sourceBizType, Long sourceBizId) {
        return selectOne(ErpApEstimateDO::getSourceBizType, sourceBizType,
                ErpApEstimateDO::getSourceBizId, sourceBizId);
    }

    default ErpApEstimateDO selectByEstimateNo(String estimateNo) {
        return selectOne(ErpApEstimateDO::getEstimateNo, estimateNo);
    }

    default int updateReverseInfoById(Long id, Integer status, Long reverseUserId, LocalDateTime reverseTime,
                                      Integer reverseType, Long reverseSourceId, String reverseSourceNo,
                                      String reverseRemark) {
        return update(null, new LambdaUpdateWrapper<ErpApEstimateDO>()
                .eq(ErpApEstimateDO::getId, id)
                .set(ErpApEstimateDO::getStatus, status)
                .set(ErpApEstimateDO::getReverseUserId, reverseUserId)
                .set(ErpApEstimateDO::getReverseTime, reverseTime)
                .set(ErpApEstimateDO::getReverseType, reverseType)
                .set(ErpApEstimateDO::getReverseSourceId, reverseSourceId)
                .set(ErpApEstimateDO::getReverseSourceNo, reverseSourceNo)
                .set(ErpApEstimateDO::getReverseRemark, reverseRemark));
    }

    default int restoreOpenStatusById(Long id, Integer status) {
        return update(null, new LambdaUpdateWrapper<ErpApEstimateDO>()
                .eq(ErpApEstimateDO::getId, id)
                .set(ErpApEstimateDO::getStatus, status)
                .set(ErpApEstimateDO::getReverseUserId, null)
                .set(ErpApEstimateDO::getReverseTime, null)
                .set(ErpApEstimateDO::getReverseType, null)
                .set(ErpApEstimateDO::getReverseSourceId, null)
                .set(ErpApEstimateDO::getReverseSourceNo, null)
                .set(ErpApEstimateDO::getReverseRemark, null));
    }

}
