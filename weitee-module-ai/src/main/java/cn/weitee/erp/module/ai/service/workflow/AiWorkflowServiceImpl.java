package cn.weitee.erp.module.ai.service.workflow;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.object.BeanUtils;
import cn.weitee.erp.module.ai.controller.admin.workflow.vo.AiWorkflowPageReqVO;
import cn.weitee.erp.module.ai.controller.admin.workflow.vo.AiWorkflowSaveReqVO;
import cn.weitee.erp.module.ai.controller.admin.workflow.vo.AiWorkflowTestReqVO;
import cn.weitee.erp.module.ai.dal.dataobject.workflow.AiWorkflowDO;
import cn.weitee.erp.module.ai.dal.mysql.workflow.AiWorkflowMapper;
import cn.weitee.erp.module.ai.service.model.AiModelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.tinyflow.core.Tinyflow;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static cn.weitee.erp.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.weitee.erp.module.ai.enums.ErrorCodeConstants.WORKFLOW_CODE_EXISTS;
import static cn.weitee.erp.module.ai.enums.ErrorCodeConstants.WORKFLOW_NOT_EXISTS;

/**
 * AI 工作�?Service 实现�?
 *
 * @author lesan
 */
@Service
@Slf4j
public class AiWorkflowServiceImpl implements AiWorkflowService {

    @Resource
    private AiWorkflowMapper aiWorkflowMapper;

    @Resource
    private AiModelService apiModelService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Long createWorkflow(AiWorkflowSaveReqVO createReqVO) {
        // 1. 参数校验
        validateCodeUnique(null, createReqVO.getCode());

        // 2. 插入工作流配�?
        AiWorkflowDO workflow = BeanUtils.toBean(createReqVO, AiWorkflowDO.class);
        aiWorkflowMapper.insert(workflow);
        return workflow.getId();
    }

    @Override
    public void updateWorkflow(AiWorkflowSaveReqVO updateReqVO) {
        // 1. 参数校验
        validateWorkflowExists(updateReqVO.getId());
        validateCodeUnique(updateReqVO.getId(), updateReqVO.getCode());

        // 2. 更新工作流配�?
        AiWorkflowDO workflow = BeanUtils.toBean(updateReqVO, AiWorkflowDO.class);
        aiWorkflowMapper.updateById(workflow);
    }

    @Override
    public void deleteWorkflow(Long id) {
        // 1. 校验存在
        validateWorkflowExists(id);

        // 2. 删除工作流配�?
        aiWorkflowMapper.deleteById(id);
    }

    private AiWorkflowDO validateWorkflowExists(Long id) {
        if (ObjUtil.isNull(id)) {
            throw exception(WORKFLOW_NOT_EXISTS);
        }
        AiWorkflowDO workflow = aiWorkflowMapper.selectById(id);
        if (ObjUtil.isNull(workflow)) {
            throw exception(WORKFLOW_NOT_EXISTS);
        }
        return workflow;
    }

    private void validateCodeUnique(Long id, String code) {
        if (StrUtil.isBlank(code)) {
            return;
        }
        AiWorkflowDO workflow = aiWorkflowMapper.selectByCode(code);
        if (ObjUtil.isNull(workflow)) {
            return;
        }
        if (ObjUtil.isNull(id)) {
            throw exception(WORKFLOW_CODE_EXISTS);
        }
        if (ObjUtil.notEqual(workflow.getId(), id)) {
            throw exception(WORKFLOW_CODE_EXISTS);
        }
    }

    @Override
    public AiWorkflowDO getWorkflow(Long id) {
        return aiWorkflowMapper.selectById(id);
    }

    @Override
    public PageResult<AiWorkflowDO> getWorkflowPage(AiWorkflowPageReqVO pageReqVO) {
        return aiWorkflowMapper.selectPage(pageReqVO);
    }

    @Override
    public Object testWorkflow(AiWorkflowTestReqVO testReqVO) {
        // 加载 graph
        String graph = testReqVO.getGraph() != null ? testReqVO.getGraph()
                : validateWorkflowExists(testReqVO.getId()).getGraph();

        // 构建 TinyFlow 执行�?
        Tinyflow tinyflow = parseFlowParam(graph);

        // 执行
        Map<String, Object> variables = testReqVO.getParams();
        return tinyflow.toChain().executeForResult(variables);
    }

    private Tinyflow parseFlowParam(String graph) {
        try {
            JsonNode json = objectMapper.readTree(graph);
            ArrayNode nodeArr = (ArrayNode) json.get("nodes");
            Tinyflow tinyflow = new Tinyflow(graph);
            for (int i = 0; i < nodeArr.size(); i++) {
                JsonNode node = nodeArr.get(i);
                String type = node.get("type").asText();
                switch (type) {
                    case "llmNode":
                        JsonNode data = node.get("data");
                        apiModelService.getLLmProvider4Tinyflow(tinyflow, data.get("llmId").asLong());
                        break;
                    case "internalNode":
                        break;
                    default:
                        break;
                }
            }
            return tinyflow;
        } catch (Exception e) {
            throw new RuntimeException("解析工作流图失败", e);
        }
    }

}
