package cn.weitee.erp.module.erp.service.project;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.framework.security.core.util.SecurityFrameworkUtils;
import cn.weitee.erp.module.erp.controller.admin.project.vo.project.ErpProjectMcConfirmReqVO;
import cn.weitee.erp.module.erp.controller.admin.sale.vo.order.ErpSaleOrderSaveReqVO;
import cn.weitee.erp.module.erp.controller.admin.project.vo.project.ErpProjectPageReqVO;
import cn.weitee.erp.module.erp.controller.admin.project.vo.project.ErpProjectPcConfirmReqVO;
import cn.weitee.erp.module.erp.controller.admin.project.vo.project.ErpProjectSaveReqVO;
import cn.weitee.erp.module.erp.dal.dataobject.project.ErpProjectDO;
import cn.weitee.erp.module.erp.dal.dataobject.sale.ErpSaleOrderDO;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpProductionSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.mrp.ErpPurchaseSuggestMapper;
import cn.weitee.erp.module.erp.dal.mysql.project.ErpProjectMapper;
import cn.weitee.erp.module.erp.dal.mysql.sale.ErpSaleOrderMapper;
import cn.weitee.erp.module.erp.dal.redis.no.ErpNoRedisDAO;
import cn.weitee.erp.module.erp.enums.ErpAuditStatus;
import cn.weitee.erp.module.erp.enums.ErpBusinessTypeConstants;
import cn.weitee.erp.module.erp.enums.ErpProjectTypeConstants;
import cn.weitee.erp.module.erp.enums.mrp.ErpMrpSuggestStatusEnum;
import cn.weitee.erp.module.erp.service.sale.ErpCustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_DELETE_FAIL_EXISTS_SALE_ORDER;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_MC_CONFIRM_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_MC_CONFIRM_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_NO_EXISTS;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_PC_CONFIRM_FORBIDDEN;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_PC_CONFIRM_STATUS_INVALID;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_SOURCE_NOT_ENABLE;
import static cn.weitee.erp.module.erp.enums.ErrorCodeConstants.PROJECT_SOURCE_REQUIRED;

/**
 * ERP 项目 Service 实现类
 */
@Service
@Validated
public class ErpProjectServiceImpl implements ErpProjectService {

    private static final String PROJECT_ROLE_TASK_PENDING_MC_SUMMARY = "MRP 建议仍待 MC 处理";

    @Resource
    private ErpProjectMapper erpProjectMapper;
    @Resource
    private ErpCustomerService customerService;
    @Resource
    private ErpSaleOrderMapper erpSaleOrderMapper;
    @Resource
    private ErpPurchaseSuggestMapper erpPurchaseSuggestMapper;
    @Resource
    private ErpProductionSuggestMapper erpProductionSuggestMapper;
    @Resource
    private ErpNoRedisDAO noRedisDAO;
    @Resource
    private ErpProjectRoleTaskService projectRoleTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProject(ErpProjectSaveReqVO createReqVO) {
        customerService.validateCustomer(createReqVO.getCustomerId());
        String no = createReqVO.getNo();
        if (StrUtil.isBlank(no)) {
            no = noRedisDAO.generate(ErpNoRedisDAO.PROJECT_NO_PREFIX);
        }
        validateProjectNoUnique(null, no);
        String finalNo = no;

        ErpProjectDO project = BeanUtils.toBean(createReqVO, ErpProjectDO.class, item -> {
            item.setNo(finalNo);
            item.setDeliveryDate(convertDeliveryDate(createReqVO.getDeliveryDate()));
            item.setPcStatus("PENDING");
            item.setMcStatus("PENDING");
        });
        erpProjectMapper.insert(project);
        return project.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(ErpProjectSaveReqVO updateReqVO) {
        ErpProjectDO currentProject = validateProjectExists(updateReqVO.getId());
        customerService.validateCustomer(updateReqVO.getCustomerId());
        validateProjectNoUnique(updateReqVO.getId(), updateReqVO.getNo());

        ErpProjectDO updateObj = BeanUtils.toBean(updateReqVO, ErpProjectDO.class,
                item -> item.setDeliveryDate(convertDeliveryDate(updateReqVO.getDeliveryDate())));
        erpProjectMapper.updateById(updateObj);
        syncPendingRoleTasksAfterProjectUpdate(buildUpdatedProject(currentProject, updateObj));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        ErpProjectDO project = validateProjectExists(id);
        if (erpSaleOrderMapper.selectCountByProjectId(id) > 0) {
            throw exception(PROJECT_DELETE_FAIL_EXISTS_SALE_ORDER, project.getName());
        }
        erpProjectMapper.deleteById(id);
    }

    @Override
    public ErpProjectDO getProject(Long id) {
        return erpProjectMapper.selectById(id);
    }

    @Override
    public ErpProjectDO validateProject(Long id) {
        ErpProjectDO project = validateProjectExists(id);
        if (!CommonStatusEnum.isEnable(project.getStatus())) {
            throw exception(PROJECT_NOT_ENABLE, project.getName());
        }
        return project;
    }

    @Override
    public List<ErpProjectDO> getProjectList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return erpProjectMapper.selectList(ErpProjectDO::getId, ids);
    }

    @Override
    public PageResult<ErpProjectDO> getProjectPage(ErpProjectPageReqVO pageReqVO) {
        return erpProjectMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<ErpProjectDO> getAssignedProjectPage(String roleCode, ErpProjectPageReqVO pageReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (StrUtil.equals(roleCode, "PC")) {
            pageReqVO.setPlanCoordinatorId(loginUserId);
        } else if (StrUtil.equals(roleCode, "MC")) {
            pageReqVO.setMaterialControllerId(loginUserId);
        }
        return erpProjectMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ErpProjectDO> getProjectListByStatus(Integer status) {
        return erpProjectMapper.selectListByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPc(ErpProjectPcConfirmReqVO reqVO) {
        ErpProjectDO project = validateProjectExists(reqVO.getProjectId());
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(project.getPlanCoordinatorId(), loginUserId)) {
            throw exception(PROJECT_PC_CONFIRM_FORBIDDEN);
        }
        if (StrUtil.equalsIgnoreCase(project.getPcStatus(), "DONE")) {
            throw exception(PROJECT_PC_CONFIRM_STATUS_INVALID);
        }
        erpProjectMapper.updateById(ErpProjectDO.builder()
                .id(reqVO.getProjectId())
                .currentStageCode(reqVO.getCurrentStageCode())
                .pcStatus("DONE")
                .pcConfirmTime(LocalDateTime.now())
                .pcRemark(reqVO.getRemark())
                .build());
        projectRoleTaskService.completePcTask(reqVO.getProjectId(), reqVO.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmMc(ErpProjectMcConfirmReqVO reqVO) {
        ErpProjectDO project = validateProjectExists(reqVO.getProjectId());
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (!Objects.equals(project.getMaterialControllerId(), loginUserId)) {
            throw exception(PROJECT_MC_CONFIRM_FORBIDDEN);
        }
        if (StrUtil.equalsIgnoreCase(project.getMcStatus(), "DONE")) {
            throw exception(PROJECT_MC_CONFIRM_STATUS_INVALID);
        }
        erpProjectMapper.updateById(ErpProjectDO.builder()
                .id(reqVO.getProjectId())
                .mcStatus("DONE")
                .mcConfirmTime(LocalDateTime.now())
                .mcRemark(reqVO.getRemark())
                .build());
        projectRoleTaskService.completeMcTask(reqVO.getProjectId(), reqVO.getRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeliveryProjectFromSource(Long sourceProjectId, ErpSaleOrderSaveReqVO saleReqVO) {
        if (sourceProjectId == null) {
            throw exception(PROJECT_SOURCE_REQUIRED);
        }
        ErpProjectDO sourceProject = validateProjectExists(sourceProjectId);
        if (!CommonStatusEnum.isEnable(sourceProject.getStatus())) {
            throw exception(PROJECT_SOURCE_NOT_ENABLE, sourceProject.getName());
        }
        String no = noRedisDAO.generate(ErpNoRedisDAO.PROJECT_NO_PREFIX);
        validateProjectNoUnique(null, no);
        ErpProjectDO deliveryProject = ErpProjectDO.builder()
                .no(no)
                .name(sourceProject.getName() + "-交付")
                .projectType(ErpProjectTypeConstants.DELIVERY)
                .businessType(ErpBusinessTypeConstants.SELF_RESEARCH)
                .sourceType("SALE_ORDER")
                .sourceProjectId(sourceProjectId)
                .projectManagerId(sourceProject.getProjectManagerId())
                .planCoordinatorId(sourceProject.getPlanCoordinatorId())
                .materialControllerId(sourceProject.getMaterialControllerId())
                .ownerDeptId(sourceProject.getOwnerDeptId())
                .currentStageCode("INIT")
                .riskLevel(StrUtil.blankToDefault(sourceProject.getRiskLevel(), "NORMAL"))
                .customerId(sourceProject.getCustomerId())
                .status(CommonStatusEnum.ENABLE.getStatus())
                .pcStatus("PENDING")
                .mcStatus("PENDING")
                .deliveryDate(convertDeliveryDate(saleReqVO.getDeliveryDate()))
                .remark(StrUtil.blankToDefault(saleReqVO.getRemark(), sourceProject.getRemark()))
                .build();
        erpProjectMapper.insert(deliveryProject);
        return deliveryProject.getId();
    }

    @Override
    public void bindSaleOrder(Long projectId, Long saleOrderId) {
        erpProjectMapper.updateById(ErpProjectDO.builder().id(projectId).saleOrderId(saleOrderId).build());
    }

    private ErpProjectDO validateProjectExists(Long id) {
        ErpProjectDO project = erpProjectMapper.selectById(id);
        if (project == null) {
            throw exception(PROJECT_NOT_EXISTS);
        }
        return project;
    }

    private void validateProjectNoUnique(Long id, String no) {
        ErpProjectDO project = erpProjectMapper.selectByNo(no);
        if (project == null) {
            return;
        }
        if (id == null || !project.getId().equals(id)) {
            throw exception(PROJECT_NO_EXISTS);
        }
    }

    private LocalDate convertDeliveryDate(LocalDateTime deliveryDate) {
        return deliveryDate == null ? null : deliveryDate.toLocalDate();
    }

    private ErpProjectDO buildUpdatedProject(ErpProjectDO currentProject, ErpProjectDO updateObj) {
        return BeanUtils.toBean(currentProject, ErpProjectDO.class, target -> {
            target.setNo(updateObj.getNo());
            target.setName(updateObj.getName());
            target.setPlanCoordinatorId(updateObj.getPlanCoordinatorId());
            target.setMaterialControllerId(updateObj.getMaterialControllerId());
            target.setCustomerId(updateObj.getCustomerId());
            target.setStatus(updateObj.getStatus());
            target.setDeliveryDate(updateObj.getDeliveryDate());
            target.setRemark(updateObj.getRemark());
        });
    }

    private void syncPendingRoleTasksAfterProjectUpdate(ErpProjectDO project) {
        refreshPendingPcTaskIfNecessary(project);
        refreshPendingMcTaskIfNecessary(project);
    }

    private void refreshPendingPcTaskIfNecessary(ErpProjectDO project) {
        if (StrUtil.equalsIgnoreCase(project.getPcStatus(), "DONE") || project.getSaleOrderId() == null) {
            return;
        }
        ErpSaleOrderDO saleOrder = erpSaleOrderMapper.selectById(project.getSaleOrderId());
        if (saleOrder == null || !Objects.equals(saleOrder.getProjectId(), project.getId())
                || !Objects.equals(saleOrder.getStatus(), ErpAuditStatus.APPROVE.getStatus())) {
            return;
        }
        projectRoleTaskService.createOrRefreshPcTask(project.getId(), saleOrder.getId(), saleOrder.getDeliveryDate());
    }

    private void refreshPendingMcTaskIfNecessary(ErpProjectDO project) {
        if (StrUtil.equalsIgnoreCase(project.getMcStatus(), "DONE")) {
            return;
        }
        long pendingPurchaseCount = erpPurchaseSuggestMapper.selectCountByProjectIdAndStatus(project.getId(),
                ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus());
        long pendingProductionCount = erpProductionSuggestMapper.selectCountByProjectIdAndStatus(project.getId(),
                ErpMrpSuggestStatusEnum.TO_CONFIRM.getStatus());
        if (pendingPurchaseCount <= 0 && pendingProductionCount <= 0) {
            return;
        }
        projectRoleTaskService.createOrRefreshMcTask(project.getId(), null, PROJECT_ROLE_TASK_PENDING_MC_SUMMARY);
    }

}
