package cn.iocoder.yudao.server.erp;

import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelSaveReqVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelFormTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmModelTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmSimpleModelNodeTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskApproveMethodEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignEmptyHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskAssignStartUserHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.enums.definition.BpmUserTaskRejectHandlerTypeEnum;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.yudao.module.bpm.service.definition.BpmModelService;
import cn.iocoder.yudao.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.yudao.server.YudaoServerApplication;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpServletRequest;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = YudaoServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "server.port=0",
                "spring.quartz.auto-startup=false",
                "spring.boot.admin.client.enabled=false",
                "logging.file.name=target/erp-purchase-order-bpm-install-test.log"
        }
)
@ActiveProfiles("local")
class ErpPurchaseOrderBpmInstallIntegrationTest {

    private static final Long TENANT_ID = 1L;
    private static final Long MANAGER_USER_ID = 1L;
    private static final String MODEL_KEY = "erp_purchase_order";
    private static final String MODEL_NAME = "ERP Purchase Order Approval";
    private static final String CATEGORY = "erp_approval";
    private static final String CREATE_PATH = "/erp/purchase/order";
    private static final String VIEW_PATH = "/erp/purchase/order/bpm/detail/index";
    private static final Long LEADER_ROLE_ID = 920002L;
    private static final Long MANAGER_ROLE_ID = 920003L;

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmModelService bpmModelService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Test
    void installPurchaseOrderApprovalProcess() {
        setLoginUser();
        try {
            String modelId = upsertModel();

            bpmModelService.deployModel(MANAGER_USER_ID, modelId);

            ProcessDefinition definition = processDefinitionService.getActiveProcessDefinition(MODEL_KEY);
            assertNotNull(definition, "Purchase order BPM definition should be active after deployment");
            assertEquals(MODEL_KEY, definition.getKey());
            assertEquals(MODEL_NAME, definition.getName());

            BpmProcessDefinitionInfoDO definitionInfo =
                    processDefinitionService.getProcessDefinitionInfo(definition.getId());
            assertNotNull(definitionInfo, "Process definition info should exist after deployment");
            assertEquals(CATEGORY, definitionInfo.getCategory());
            assertEquals(BpmModelTypeEnum.SIMPLE.getType(), definitionInfo.getModelType());
            assertEquals(BpmModelFormTypeEnum.CUSTOM.getType(), definitionInfo.getFormType());
            assertEquals(CREATE_PATH, definitionInfo.getFormCustomCreatePath());
            assertEquals(VIEW_PATH, definitionInfo.getFormCustomViewPath());
            assertTrue(Boolean.TRUE.equals(definitionInfo.getAllowCancelRunningProcess()));
        } finally {
            clearLoginUser();
        }
    }

    @Test
    void buildModel_shouldUsePurchaseRolesInApprovalChain() {
        BpmSimpleModelNodeVO leaderNode = buildModel().getSimpleModel();
        assertNotNull(leaderNode, "The first approval node should exist");
        assertEquals(BpmTaskCandidateStrategyEnum.ROLE.getStrategy(), leaderNode.getCandidateStrategy());
        assertEquals(String.valueOf(LEADER_ROLE_ID), leaderNode.getCandidateParam());

        BpmSimpleModelNodeVO managerNode = leaderNode.getChildNode();
        assertNotNull(managerNode, "The second approval node should exist");
        assertEquals(BpmTaskCandidateStrategyEnum.ROLE.getStrategy(), managerNode.getCandidateStrategy());
        assertEquals(String.valueOf(MANAGER_ROLE_ID), managerNode.getCandidateParam());
    }

    private String upsertModel() {
        BpmModelSaveReqVO reqVO = buildModel();
        Model existingModel = repositoryService.createModelQuery()
                .modelTenantId(String.valueOf(TENANT_ID))
                .modelKey(MODEL_KEY)
                .singleResult();
        if (existingModel == null) {
            return bpmModelService.createModel(reqVO);
        }
        reqVO.setId(existingModel.getId());
        bpmModelService.updateModel(MANAGER_USER_ID, reqVO);
        return existingModel.getId();
    }

    private BpmModelSaveReqVO buildModel() {
        BpmModelSaveReqVO reqVO = new BpmModelSaveReqVO();
        reqVO.setKey(MODEL_KEY);
        reqVO.setName(MODEL_NAME);
        reqVO.setCategory(CATEGORY);
        reqVO.setDescription("Default ERP purchase order approval workflow");
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setFormType(BpmModelFormTypeEnum.CUSTOM.getType());
        reqVO.setFormCustomCreatePath(CREATE_PATH);
        reqVO.setFormCustomViewPath(VIEW_PATH);
        reqVO.setVisible(Boolean.TRUE);
        reqVO.setManagerUserIds(List.of(MANAGER_USER_ID));
        reqVO.setAllowCancelRunningProcess(Boolean.TRUE);
        reqVO.setSimpleModel(buildLeaderNode());
        return reqVO;
    }

    private void setLoginUser() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(MANAGER_USER_ID);
        loginUser.setUserType(1);
        SecurityFrameworkUtils.setLoginUser(loginUser, new MockHttpServletRequest());
    }

    private void clearLoginUser() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private BpmSimpleModelNodeVO buildLeaderNode() {
        return buildApproveNode(
                "purchaseOrderApproveLeader",
                "Purchase Leader Approval",
                "Assign to role: 采购审批组长",
                LEADER_ROLE_ID,
                buildManagerNode()
        );
    }

    private BpmSimpleModelNodeVO buildManagerNode() {
        return buildApproveNode(
                "purchaseOrderApproveManager",
                "Purchase Manager Approval",
                "Assign to role: 采购审批经理",
                MANAGER_ROLE_ID,
                buildEndNode()
        );
    }

    private BpmSimpleModelNodeVO buildEndNode() {
        BpmSimpleModelNodeVO node = new BpmSimpleModelNodeVO();
        node.setId("purchaseOrderApproveEnd");
        node.setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
        node.setName("End");
        return node;
    }

    private BpmSimpleModelNodeVO buildApproveNode(String id, String name, String showText,
                                                  Long roleId, BpmSimpleModelNodeVO childNode) {
        BpmSimpleModelNodeVO node = new BpmSimpleModelNodeVO();
        node.setId(id);
        node.setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType());
        node.setName(name);
        node.setShowText(showText);
        node.setCandidateStrategy(BpmTaskCandidateStrategyEnum.ROLE.getStrategy());
        node.setCandidateParam(String.valueOf(roleId));
        node.setApproveType(1);
        node.setApproveMethod(BpmUserTaskApproveMethodEnum.SEQUENTIAL.getMethod());
        node.setApproveRatio(100);
        node.setSignEnable(Boolean.FALSE);
        node.setReasonRequire(Boolean.TRUE);
        BpmSimpleModelNodeVO.RejectHandler rejectHandler = new BpmSimpleModelNodeVO.RejectHandler();
        rejectHandler.setType(BpmUserTaskRejectHandlerTypeEnum.FINISH_PROCESS_INSTANCE.getType());
        node.setRejectHandler(rejectHandler);
        node.setAssignStartUserHandlerType(BpmUserTaskAssignStartUserHandlerTypeEnum.SKIP.getType());
        BpmSimpleModelNodeVO.AssignEmptyHandler assignEmptyHandler = new BpmSimpleModelNodeVO.AssignEmptyHandler();
        assignEmptyHandler.setType(BpmUserTaskAssignEmptyHandlerTypeEnum.ASSIGN_USER.getType());
        assignEmptyHandler.setUserIds(List.of(MANAGER_USER_ID));
        node.setAssignEmptyHandler(assignEmptyHandler);
        node.setChildNode(childNode);
        return node;
    }

}
