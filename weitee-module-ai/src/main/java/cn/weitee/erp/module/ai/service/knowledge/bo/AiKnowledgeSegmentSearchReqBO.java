package cn.weitee.erp.module.ai.service.knowledge.bo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotEmpty;

/**
 * AI 知识库段落搜�?Request BO
 *
 * @author WeTai
 */
@Data
public class AiKnowledgeSegmentSearchReqBO {

    /**
     * 知识库编�?
     */
    @NotNull(message = "知识库编号不能为�?)
    private Long knowledgeId;

    /**
     * 内容
     */
    @NotEmpty(message = "内容不能为空")
    private String content;

    /**
     * 最大返回数�?
     */
    private Integer topK;

    /**
     * 相似度阈�?
     */
    private Double similarityThreshold;

}