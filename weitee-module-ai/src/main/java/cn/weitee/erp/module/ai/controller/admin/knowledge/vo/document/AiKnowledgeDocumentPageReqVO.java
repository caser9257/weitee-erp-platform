package cn.weitee.erp.module.ai.controller.admin.knowledge.vo.document;

import cn.weitee.erp.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 知识库文档的分页 Request VO")
@Data
public class AiKnowledgeDocumentPageReqVO extends PageParam {

    @Schema(description = "知识库编�?, example = "1")
    private Long knowledgeId;

    @Schema(description = "文档名称", example = "Java 开发手�?)
    private String name;

}
