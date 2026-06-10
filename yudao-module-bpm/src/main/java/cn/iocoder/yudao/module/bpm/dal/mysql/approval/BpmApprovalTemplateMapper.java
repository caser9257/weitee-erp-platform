package cn.iocoder.yudao.module.bpm.dal.mysql.approval;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.bpm.controller.admin.approval.vo.template.BpmApprovalTemplatePageReqVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.approval.BpmApprovalTemplateDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审批模板 Mapper
 */
@Mapper
public interface BpmApprovalTemplateMapper extends BaseMapperX<BpmApprovalTemplateDO> {

    default PageResult<BpmApprovalTemplateDO> selectPage(BpmApprovalTemplatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BpmApprovalTemplateDO>()
                .likeIfPresent(BpmApprovalTemplateDO::getName, reqVO.getName())
                .eqIfPresent(BpmApprovalTemplateDO::getCategory, reqVO.getCategory())
                .eqIfPresent(BpmApprovalTemplateDO::getStatus, reqVO.getStatus())
                .orderByAsc(BpmApprovalTemplateDO::getSort)
                .orderByDesc(BpmApprovalTemplateDO::getId));
    }

    default BpmApprovalTemplateDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<BpmApprovalTemplateDO>()
                .eq(BpmApprovalTemplateDO::getCode, code)
                .last("LIMIT 1"));
    }

}
