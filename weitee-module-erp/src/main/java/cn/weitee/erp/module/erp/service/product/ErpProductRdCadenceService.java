package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductImportResultVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpRdCadencePageReqVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 研发物料(Cadence) Service 接口。
 *
 * 仅研发部门(deptId=103)可访问；接口内部再次校验部门，不依赖前端隐藏。
 * Cadence 字段属于物料属性，导入只维护 erp_product_cadence 扩展数据；
 * 已审核物料的 Cadence 变更复用现有物料修改审批服务，禁止直接覆盖主数据。
 */
public interface ErpProductRdCadenceService {

    /**
     * 查询研发物料(Cadence)分页列表，默认只返回 is_pcb_component=1 的物料。
     *
     * @param reqVO 查询条件（部门非研发时抛出无权限）
     * @param deptId 当前登录用户部门编号
     */
    PageResult<ErpProductRespVO> getRdCadencePage(ErpRdCadencePageReqVO reqVO, Long deptId);

    /**
     * 导出研发物料(Cadence)数据，列名使用英文。
     */
    java.util.List<ErpProductRespVO> exportRdCadence(ErpRdCadencePageReqVO reqVO, Long deptId);

    /**
     * 下载 Cadence 英文表头导入模板。
     */
    byte[] downloadTemplate();

    /**
     * 导入预检查：校验每一行，但不落库。
     *
     * @param markAllAsPcb true 时所有行视为 PCB 元器件（随审批一并标记，无需在 Excel 中逐行填写 PCB_Component）
     */
    ErpProductImportResultVO precheckImport(MultipartFile file, Long deptId, boolean markAllAsPcb);

    /**
     * 执行 Cadence 数据导入（已审核物料经修改审批落库）。
     *
     * @param markAllAsPcb true 时所有行视为 PCB 元器件（随审批一并标记，无需在 Excel 中逐行填写 PCB_Component）
     */
    ErpProductImportResultVO importCadence(MultipartFile file, Long userId, Long deptId, boolean markAllAsPcb);

}
