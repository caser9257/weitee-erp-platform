package cn.iocoder.yudao.module.erp.dal.mysql.finance;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.erp.controller.admin.finance.vo.asset.ErpFinanceAssetPageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.finance.ErpFinanceAssetDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ErpFinanceAssetMapper extends BaseMapperX<ErpFinanceAssetDO> {

    default PageResult<ErpFinanceAssetDO> selectPage(ErpFinanceAssetPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceAssetDO>()
                .likeIfPresent(ErpFinanceAssetDO::getNo, reqVO.getNo())
                .likeIfPresent(ErpFinanceAssetDO::getName, reqVO.getName())
                .likeIfPresent(ErpFinanceAssetDO::getCategoryName, reqVO.getCategoryName())
                .eqIfPresent(ErpFinanceAssetDO::getAssetType, reqVO.getAssetType())
                .eqIfPresent(ErpFinanceAssetDO::getStatus, reqVO.getStatus())
                .orderByDesc(ErpFinanceAssetDO::getId));
    }

    default ErpFinanceAssetDO selectByNo(String no) {
        return selectOne(ErpFinanceAssetDO::getNo, no);
    }

    default int updateByIdAndStatus(Long id, Integer status, ErpFinanceAssetDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<ErpFinanceAssetDO>()
                .eq(ErpFinanceAssetDO::getId, id)
                .eq(ErpFinanceAssetDO::getStatus, status));
    }
}
