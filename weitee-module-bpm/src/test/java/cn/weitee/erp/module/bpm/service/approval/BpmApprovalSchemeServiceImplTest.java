package cn.weitee.erp.module.bpm.service.approval;

import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePageReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemePublishReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeRespVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSaveReqVO;
import cn.weitee.erp.module.bpm.controller.admin.approval.vo.scheme.BpmApprovalSchemeSubmitReqVO;
import cn.weitee.erp.module.bpm.dal.dataobject.approval.BpmApprovalSchemeVersionDO;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalRuleMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeMapper;
import cn.weitee.erp.module.bpm.dal.mysql.approval.BpmApprovalSchemeVersionMapper;
import cn.weitee.erp.module.bpm.enums.approval.BpmApprovalSchemeStatusEnum;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static cn.weitee.erp.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.weitee.erp.framework.test.core.util.RandomUtils.randomString;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_RULE_PROCESS_DEFINITION_NOT_EXISTS;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_SCHEME_DEFAULT_RULE_REQUIRED;
import static cn.weitee.erp.module.bpm.enums.ErrorCodeConstants.APPROVAL_SCHEME_VERSION_NOT_ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cn.weitee.erp.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.weitee.erp.module.system.api.permission.PermissionApi;

@Import(BpmApprovalSchemeServiceImpl.class)
class BpmApprovalSchemeServiceImplTest extends BaseDbUnitTest {

    @MockBean
    private PermissionApi permissionApi;

    @MockBean
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private BpmApprovalSchemeServiceImpl approvalSchemeService;

    @Resource
    private BpmApprovalSchemeMapper approvalSchemeMapper;

    @Resource
    private BpmApprovalSchemeVersionMapper approvalSchemeVersionMapper;

    @Resource
    private BpmApprovalRuleMapper approvalRuleMapper;

    @BeforeEach
    void setUp() {
        when(processDefinitionService.getActiveProcessDefinition(anyString()))
                .thenReturn(mock(ProcessDefinition.class));
    }

    @Test
    void testCreateDraft_success() {
        BpmApprovalSchemeSaveReqVO reqVO = buildSaveReqVO();

        Long versionId = approvalSchemeService.createDraft(reqVO);

        assertNotNull(versionId);
        BpmApprovalSchemeVersionDO version = approvalSchemeVersionMapper.selectById(versionId);
        assertEquals(BpmApprovalSchemeStatusEnum.DRAFT.getStatus(), version.getStatus());
        assertEquals(1, approvalRuleMapper.selectListBySchemeVersionId(versionId).size());
        assertEquals(1, approvalSchemeMapper.selectCount());
    }

    @Test
    void testSubmit_requiresDefaultRule() {
        BpmApprovalSchemeSaveReqVO reqVO = buildSaveReqVO();
        reqVO.getRules().forEach(rule -> rule.setDefaultRule(false));
        Long versionId = approvalSchemeService.createDraft(reqVO);

        assertServiceException(() -> approvalSchemeService.submit(new BpmApprovalSchemeSubmitReqVO()
                .setVersionId(versionId)), APPROVAL_SCHEME_DEFAULT_RULE_REQUIRED);
    }

    @Test
    void testPublish_success_disablePreviousActiveVersion() {
        Long firstVersionId = approvalSchemeService.createDraft(buildSaveReqVO());
        approvalSchemeService.submit(new BpmApprovalSchemeSubmitReqVO().setVersionId(firstVersionId));
        approvalSchemeService.publish(new BpmApprovalSchemePublishReqVO().setVersionId(firstVersionId));

        BpmApprovalSchemeSaveReqVO secondReqVO = buildSaveReqVO();
        secondReqVO.setId(approvalSchemeMapper.selectList().get(0).getId());
        secondReqVO.setVersionId(firstVersionId);
        secondReqVO.setName("审批方案二期");
        secondReqVO.setCode("approval_scheme_upgrade");
        Long secondVersionId = approvalSchemeService.createDraft(secondReqVO);
        approvalSchemeService.submit(new BpmApprovalSchemeSubmitReqVO().setVersionId(secondVersionId));

        approvalSchemeService.publish(new BpmApprovalSchemePublishReqVO().setVersionId(secondVersionId));

        assertEquals(BpmApprovalSchemeStatusEnum.DISABLED.getStatus(),
                approvalSchemeVersionMapper.selectById(firstVersionId).getStatus());
        assertEquals(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus(),
                approvalSchemeVersionMapper.selectById(secondVersionId).getStatus());
    }

    @Test
    void testPublish_rejectsMissingProcessDefinition() {
        Long versionId = approvalSchemeService.createDraft(buildSaveReqVO());
        approvalSchemeService.submit(new BpmApprovalSchemeSubmitReqVO().setVersionId(versionId));
        when(processDefinitionService.getActiveProcessDefinition("erp_purchase_in_approval")).thenReturn(null);

        assertServiceException(() -> approvalSchemeService.publish(
                new BpmApprovalSchemePublishReqVO().setVersionId(versionId)),
                APPROVAL_RULE_PROCESS_DEFINITION_NOT_EXISTS, "erp_purchase_in_approval");
    }

    @Test
    void testDisable_onlyActive() {
        Long versionId = approvalSchemeService.createDraft(buildSaveReqVO());

        assertServiceException(() -> approvalSchemeService.disable(versionId), APPROVAL_SCHEME_VERSION_NOT_ACTIVE);
    }

    @Test
    void testGetSchemePage_success() {
        Long versionId = approvalSchemeService.createDraft(buildSaveReqVO());
        approvalSchemeService.submit(new BpmApprovalSchemeSubmitReqVO().setVersionId(versionId));
        approvalSchemeService.publish(new BpmApprovalSchemePublishReqVO().setVersionId(versionId));

        PageResult<BpmApprovalSchemeRespVO> pageResult = approvalSchemeService.getSchemePage(new BpmApprovalSchemePageReqVO());

        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertEquals(BpmApprovalSchemeStatusEnum.ACTIVE.getStatus(), pageResult.getList().get(0).getLatestVersionStatus());
    }

    private static BpmApprovalSchemeSaveReqVO buildSaveReqVO() {
        return new BpmApprovalSchemeSaveReqVO()
                .setName("采购申请审批")
                .setCode("purchase_apply_" + randomString())
                .setModuleCode("erp_purchase")
                .setBizType("purchase_apply")
                .setRemark("测试")
                .setDesignJson("{\"nodes\":[]}")
                .setRules(Collections.singletonList(new BpmApprovalSchemeSaveReqVO.Rule()
                        .setRuleName("默认规则")
                        .setRuleType("DEFAULT")
                        .setPriority(1)
                        .setDefaultRule(true)
                        .setConditionJson("{}")
                        .setProcessJson("erp_purchase_in_approval")));
    }
}
