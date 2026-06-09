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
                "logging.file.name=target/erp-sale-order-bpm-install-test.log"
        }
)
@ActiveProfiles("local")
class ErpSaleOrderBpmInstallIntegrationTest {

    private static final Long TENANT_ID = 1L;
    private static final Long MANAGER_USER_ID = 1L;
    private static final String MODEL_KEY = "erp_sale_order";
    private static final String MODEL_NAME = "ERP Sale Order Approval";
    private static final String CATEGORY = "erp_approval";
    private static final String CREATE_PATH = "/erp/sale/order";
    private static final String VIEW_PATH = "/erp/sale/order/bpm/detail/index";

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private BpmModelService bpmModelService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Test
    void installSaleOrderApprovalProcess() {
        setLoginUser();
        try {
            String modelId = upsertModel();

            bpmModelService.deployModel(MANAGER_USER_ID, modelId);

            ProcessDefinition definition = processDefinitionService.getActiveProcessDefinition(MODEL_KEY);
            assertNotNull(definition, "Sale order BPM definition should be active after deployment");
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
    void buildModel_shouldAssignSecondApprovalToSecondLevelDeptLeaderOnly() {
        BpmSimpleModelNodeVO firstNode = buildModel().getSimpleModel();
        assertNotNull(firstNode, "The first approval node should exist");
        BpmSimpleModelNodeVO secondNode = firstNode.getChildNode();
        assertNotNull(secondNode, "The general manager approval node should exist");
        assertEquals(BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER.getStrategy(),
                secondNode.getCandidateStrategy(),
                "The second approval must only use the applicant's second-level department leader");
        assertEquals("2", secondNode.getCandidateParam(),
                "The second approval must target the second-level department leader");
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
        reqVO.setDescription("Default ERP sale order approval workflow");
        reqVO.setType(BpmModelTypeEnum.SIMPLE.getType());
        reqVO.setFormType(BpmModelFormTypeEnum.CUSTOM.getType());
        reqVO.setFormCustomCreatePath(CREATE_PATH);
        reqVO.setFormCustomViewPath(VIEW_PATH);
        reqVO.setVisible(Boolean.TRUE);
        reqVO.setManagerUserIds(List.of(MANAGER_USER_ID));
        reqVO.setAllowCancelRunningProcess(Boolean.TRUE);
        reqVO.setSimpleModel(buildDepartmentLeaderNode());
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

    private BpmSimpleModelNodeVO buildDepartmentLeaderNode() {
        return buildApproveNode(
                "saleOrderApproveDeptLeader",
                "Department Leader Approval",
                "Direct leader of the applicant department",
                BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER.getStrategy(),
                "1",
                buildGeneralManagerNode()
        );
    }

    private BpmSimpleModelNodeVO buildGeneralManagerNode() {
        return buildApproveNode(
                "saleOrderApproveGeneralManager",
                "General Manager Approval",
                "Escalate to the applicant's second-level department leader",
                BpmTaskCandidateStrategyEnum.START_USER_DEPT_LEADER.getStrategy(),
                "2",
                buildEndNode()
        );
    }

    private BpmSimpleModelNodeVO buildEndNode() {
        BpmSimpleModelNodeVO node = new BpmSimpleModelNodeVO();
        node.setId("saleOrderApproveEnd");
        node.setType(BpmSimpleModelNodeTypeEnum.END_NODE.getType());
        node.setName("End");
        return node;
    }

    private BpmSimpleModelNodeVO buildApproveNode(String id, String name, String showText,
                                                  Integer candidateStrategy, String candidateParam,
                                                  BpmSimpleModelNodeVO childNode) {
        BpmSimpleModelNodeVO node = new BpmSimpleModelNodeVO();
        node.setId(id);
        node.setType(BpmSimpleModelNodeTypeEnum.APPROVE_NODE.getType());
        node.setName(name);
        node.setShowText(showText);
        node.setCandidateStrategy(candidateStrategy);
        node.setCandidateParam(candidateParam);
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
