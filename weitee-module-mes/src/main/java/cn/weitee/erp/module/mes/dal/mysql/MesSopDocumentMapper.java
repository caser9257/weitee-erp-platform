package cn.weitee.erp.module.mes.dal.mysql;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.module.mes.controller.admin.vo.sop.MesSopDocumentPageReqVO;
import cn.weitee.erp.module.mes.dal.dataobject.MesSopDocumentDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MesSopDocumentMapper extends BaseMapperX<MesSopDocumentDO> {

    default PageResult<MesSopDocumentDO> selectPage(MesSopDocumentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesSopDocumentDO>()
                .likeIfPresent(MesSopDocumentDO::getSopNo, reqVO.getSopNo())
                .likeIfPresent(MesSopDocumentDO::getTitle, reqVO.getTitle())
                .eqIfPresent(MesSopDocumentDO::getStatus, reqVO.getStatus())
                .orderByDesc(MesSopDocumentDO::getId));
    }

    /**
     * 物理删除同名软删记录：释放唯一键，允许编码复用（软删后重新创建同编码）。
     */
    @Delete("DELETE FROM mes_sop_document WHERE sop_no = #{sopNo} AND deleted = 1")
    int deletePhysicalBySopNo(@Param("sopNo") String sopNo);
}
