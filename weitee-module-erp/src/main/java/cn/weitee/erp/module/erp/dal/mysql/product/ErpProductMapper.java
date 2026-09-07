package cn.weitee.erp.module.erp.dal.mysql.product;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.weitee.erp.framework.mybatis.core.mapper.BaseMapperX;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductPageReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ERP 产品 Mapper
 *
 * @author WeTai
 */
@Mapper
public interface ErpProductMapper extends BaseMapperX<ErpProductDO> {

    default PageResult<ErpProductDO> selectPage(ErpProductPageReqVO reqVO) {
        LambdaQueryWrapperX<ErpProductDO> query = new LambdaQueryWrapperX<ErpProductDO>()
                .likeIfPresent(ErpProductDO::getName, reqVO.getName())
                .likeIfPresent(ErpProductDO::getStandard, reqVO.getStandard())
                .likeIfPresent(ErpProductDO::getBrandManufacturer, reqVO.getBrandManufacturer())
                .eqIfPresent(ErpProductDO::getAuditStatus, reqVO.getAuditStatus())
                .eqIfPresent(ErpProductDO::getPcbComponent, reqVO.getPcbComponent())
                .eqIfPresent(ErpProductDO::getCategoryId, reqVO.getCategoryId())
                .betweenIfPresent(ErpProductDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ErpProductDO::getId);
        // 编码沿革：当前码或历史旧码命中均可检索到（改码后历史单据/台账仍可反查）。
        // 注意：and() 返回父类型，必须独立调用、丢弃返回值，否则会丢失 X 的链式方法
        if (reqVO.getMaterialCode() != null && !reqVO.getMaterialCode().isBlank()) {
            String code = reqVO.getMaterialCode().trim();
            query.and(w -> w.like(ErpProductDO::getMaterialCode, code)
                    .or().apply("EXISTS (SELECT 1 FROM erp_product_code_history h "
                            + "WHERE h.product_id = erp_product.id AND h.deleted = 0 "
                            + "AND h.old_code LIKE CONCAT('%', {0}, '%'))", code));
        }
        return selectPage(reqVO, query);
    }

    default Long selectCountByCategoryId(Long categoryId) {
        return selectCount(ErpProductDO::getCategoryId, categoryId);
    }

    default Long selectCountByUnitId(Long unitId) {
        return selectCount(ErpProductDO::getUnitId, unitId);
    }

    default List<ErpProductDO> selectListByStatus(Integer status) {
        return selectList(ErpProductDO::getStatus, status);
    }

    /**
     * 按状态 + 关键字（名称/物料编码/条码 模糊）查询；keyword 为空时退化为全量。
     * 远程搜索下拉专用：万级物料禁止无过滤全量下发。
     */
    default List<ErpProductDO> selectListByStatus(Integer status, String keyword) {
        return selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ErpProductDO>()
                .eq(ErpProductDO::getStatus, status)
                .and(cn.hutool.core.util.StrUtil.isNotBlank(keyword), w -> {
                    String kw = keyword.trim();
                    w.like(ErpProductDO::getName, kw)
                            .or().like(ErpProductDO::getMaterialCode, kw)
                            .or().like(ErpProductDO::getBarCode, kw)
                            .or().apply("EXISTS (SELECT 1 FROM erp_product_code_history h "
                                    + "WHERE h.product_id = erp_product.id AND h.deleted = 0 "
                                    + "AND h.old_code LIKE CONCAT('%', {0}, '%'))", kw);
                }));
    }

    /**
     * 按编码查物料：当前编码精确命中，miss 时以沿革旧码兜底（改码后历史单据/导入文件仍可对上号）。
     * 参数走 #{} 预编译绑定，无注入风险。
     */
    @Select("SELECT p.* FROM erp_product p WHERE p.deleted = 0 "
            + "AND (p.material_code = #{code} "
            + "OR EXISTS (SELECT 1 FROM erp_product_code_history h "
            + "WHERE h.product_id = p.id AND h.deleted = 0 AND h.old_code = #{code})) LIMIT 1")
    ErpProductDO selectByCodeOrHistory(String code);

    /**
     * 批量：当前编码或沿革旧码命中的物料（供导入预收集 Map 兜底，编码数可达数千级）。
     */
    @Select("<script>SELECT p.* FROM erp_product p WHERE p.deleted = 0 AND (p.material_code IN "
            + "<foreach item='c' collection='codes' open='(' separator=',' close=')'>#{c}</foreach> "
            + "OR EXISTS (SELECT 1 FROM erp_product_code_history h WHERE h.product_id = p.id "
            + "AND h.deleted = 0 AND h.old_code IN "
            + "<foreach item='c' collection='codes' open='(' separator=',' close=')'>#{c}</foreach>))</script>")
    List<ErpProductDO> selectListByCodesOrHistoryCodes(java.util.Collection<String> codes);

}