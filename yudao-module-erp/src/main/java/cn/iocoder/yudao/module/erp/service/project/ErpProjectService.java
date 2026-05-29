package cn.iocoder.yudao.module.erp.service.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectMcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectPcConfirmReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.project.vo.project.ErpProjectSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.project.ErpProjectDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 项目 Service 接口
 */
public interface ErpProjectService {

    Long createProject(@Valid ErpProjectSaveReqVO createReqVO);

    void updateProject(@Valid ErpProjectSaveReqVO updateReqVO);

    void deleteProject(Long id);

    ErpProjectDO getProject(Long id);

    ErpProjectDO validateProject(Long id);

    List<ErpProjectDO> getProjectList(Collection<Long> ids);

    default Map<Long, ErpProjectDO> getProjectMap(Collection<Long> ids) {
        return convertMap(getProjectList(ids), ErpProjectDO::getId);
    }

    PageResult<ErpProjectDO> getProjectPage(ErpProjectPageReqVO pageReqVO);

    PageResult<ErpProjectDO> getAssignedProjectPage(String roleCode, ErpProjectPageReqVO pageReqVO);

    List<ErpProjectDO> getProjectListByStatus(Integer status);

    void confirmPc(@Valid ErpProjectPcConfirmReqVO reqVO);

    void confirmMc(@Valid ErpProjectMcConfirmReqVO reqVO);

    Long createDeliveryProjectFromSource(Long sourceProjectId, ErpSaleOrderSaveReqVO saleReqVO);

    void bindSaleOrder(Long projectId, Long saleOrderId);

}
