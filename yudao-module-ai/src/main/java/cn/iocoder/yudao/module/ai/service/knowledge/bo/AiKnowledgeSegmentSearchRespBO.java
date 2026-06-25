package cn.iocoder.yudao.module.ai.service.knowledge.bo;

import lombok.Data;

/**
 * AI 知识库段落搜�?Response BO
 *
 * @author WeTai
 */
@Data
public class AiKnowledgeSegmentSearchRespBO {

    /**
     * 段落编号
     */
    private Long id;
    /**
     * 文档编号
     */
    private Long documentId;
    /**
     * 知识库编�?
     */
    private Long knowledgeId;

    /**
     * 内容
     */
    private String content;
    /**
     * 内容长度
     */
    private Integer contentLength;

    /**
     * Token 数量
     */
    private Integer tokens;

    /**
     * 相似度分�?
     */
    private Double score;

}