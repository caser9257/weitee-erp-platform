package cn.weitee.erp.module.ai.service.knowledge.splitter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown QA 格式专用切片�?
 *
 * <p>功能特点�?
 * <ul>
 *   <li>识别二级标题�?# ）作为问题标�?/li>
 *   <li>�?QA 对保持完整（不超�?Token 限制�?/li>
 *   <li>长答案智能切分，每个片段保留完整问题作为上下�?/li>
 *   <li>支持自定�?Token 估算�?/li>
 * </ul>
 *
 * @author runzhen
 */
@Slf4j
@SuppressWarnings("SizeReplaceableByIsEmpty")
public class MarkdownQaSplitter extends TextSplitter {

    /**
     * 二级标题正则：匹�?"## " 开头的�?
     */
    private static final Pattern H2_PATTERN = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);

    /**
     * 段落分隔符：双换�?
     */
    private static final String PARAGRAPH_SEPARATOR = "\n\n";

    /**
     * 句子分隔�?
     */
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[。！�?!?]\\s*");

    /**
     * 分段的最�?Token �?
     */
    private final int chunkSize;

    /**
     * Token 估算器（简单实现：中文按字符数，英文按单词数的 1.3 倍）
     */
    private final TokenEstimator tokenEstimator;

    public MarkdownQaSplitter(int chunkSize) {
        this.chunkSize = chunkSize;
        this.tokenEstimator = new SimpleTokenEstimator();
    }

    @Override
    protected List<String> splitText(String text) {
        if (StrUtil.isEmpty(text)) {
            return Collections.emptyList();
        }

        // 解析 QA �?
        List<QaPair> qaPairs = parseQaPairs(text);
        if (CollUtil.isEmpty(qaPairs)) {
            // 如果没有识别�?QA 格式，按段落切分
            return fallbackSplit(text);
        }

        // 处理每个 QA �?
        List<String> result = new ArrayList<>();
        for (QaPair qaPair : qaPairs) {
            result.addAll(splitQaPair(qaPair));
        }
        return result;
    }

    /**
     * 解析 Markdown QA �?
     *
     * @param content 文本内容
     * @return QA 对列�?
     */
    private List<QaPair> parseQaPairs(String content) {
        // 找到所有二级标题位�?
        List<QaPair> qaPairs = new ArrayList<>();
        List<Integer> headingPositions = new ArrayList<>();
        List<String> questions = new ArrayList<>();
        Matcher matcher = H2_PATTERN.matcher(content);
        while (matcher.find()) {
            headingPositions.add(matcher.start());
            questions.add(matcher.group(1).trim());
        }
        if (CollUtil.isEmpty(headingPositions)) {
            return qaPairs;
        }

        // 提取每个 QA �?
        for (int i = 0; i < headingPositions.size(); i++) {
            int start = headingPositions.get(i);
            int end = (i + 1 < headingPositions.size())
                    ? headingPositions.get(i + 1)
                    : content.length();
            String qaText = content.substring(start, end).trim();
            String question = questions.get(i);
            // 提取答案部分（去掉问题标题）
            String answer = qaText.substring(qaText.indexOf('\n') + 1).trim();
            qaPairs.add(new QaPair(question, answer, qaText));
        }
        return qaPairs;
    }

    /**
     * 切分单个 QA �?
     *
     * @param qaPair QA �?
     * @return 切分后的文本片段列表
     */
    private List<String> splitQaPair(QaPair qaPair) {
        // 如果整个 QA 对不超过限制，保持完�?
        List<String> chunks = new ArrayList<>();
        String fullQa = qaPair.fullText;
        int qaTokens = tokenEstimator.estimate(fullQa);
        if (qaTokens <= chunkSize) {
            chunks.add(fullQa);
            return chunks;
        }

        // 长答案需要切�?
        log.debug("QA 对超�?Token 限制 ({} > {})，开始智能切�? {}", qaTokens, chunkSize, qaPair.question);
        List<String> answerChunks = splitLongAnswer(qaPair.answer, qaPair.question);
        for (String answerChunk : answerChunks) {
            // 每个片段都包含完整问�?
            String chunkText = "## " + qaPair.question + "\n" + answerChunk;
            chunks.add(chunkText);
        }
        return chunks;
    }

    /**
     * 切分长答�?
     *
     * @param answer 答案文本
     * @param question 问题文本
     * @return 切分后的答案片段列表
     */
    private List<String> splitLongAnswer(String answer, String question) {
        List<String> chunks = new ArrayList<>();
        // 预留问题�?Token 空间
        String questionHeader = "## " + question + "\n";
        int questionTokens = tokenEstimator.estimate(questionHeader);
        int availableTokens = chunkSize - questionTokens - 10; // 预留 10 �?Token 的缓�?

        // 先按段落切分
        String[] paragraphs = answer.split(PARAGRAPH_SEPARATOR);
        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;
        for (String paragraph : paragraphs) {
            if (StrUtil.isEmpty(paragraph)) {
                continue;
            }
            int paragraphTokens = tokenEstimator.estimate(paragraph);
            // 如果单个段落就超过限制，需要按句子切分
            if (paragraphTokens > availableTokens) {
                // 先保存当前块
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentTokens = 0;
                }
                // 按句子切分长段落
                chunks.addAll(splitLongParagraph(paragraph, availableTokens));
                continue;
            }
            // 如果加上这个段落会超过限�?
            if (currentTokens + paragraphTokens > availableTokens && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                currentTokens = 0;
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            // 添加段落
            currentChunk.append(paragraph);
            currentTokens += paragraphTokens;
        }

        // 添加最后一�?
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return CollUtil.isEmpty(chunks) ? Collections.singletonList(answer) : chunks;
    }

    /**
     * 切分长段落（按句子）
     *
     * @param paragraph 段落文本
     * @param availableTokens 可用�?Token �?
     * @return 切分后的文本片段列表
     */
    private List<String> splitLongParagraph(String paragraph, int availableTokens) {
        // 按句子切�?
        List<String> chunks = new ArrayList<>();
        String[] sentences = SENTENCE_PATTERN.split(paragraph);

        // 按句子累积切�?
        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;
        for (String sentence : sentences) {
            if (StrUtil.isEmpty(sentence)) {
                continue;
            }
            int sentenceTokens = tokenEstimator.estimate(sentence);
            // 如果单个句子就超过限制，强制切分
            if (sentenceTokens > availableTokens) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                    currentTokens = 0;
                }
                chunks.add(sentence.trim());
                continue;
            }
            // 如果加上这个句子会超过限�?
            if (currentTokens + sentenceTokens > availableTokens && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                currentTokens = 0;
            }
            // 添加句子
            currentChunk.append(sentence);
            currentTokens += sentenceTokens;
        }

        // 添加最后一�?
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks.isEmpty() ? Collections.singletonList(paragraph) : chunks;
    }

    /**
     * 降级切分策略（当未识别到 QA 格式时）
     *
     * @param content 文本内容
     * @return 切分后的文本片段列表
     */
    private List<String> fallbackSplit(String content) {
        // 按段落切�?
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = content.split(PARAGRAPH_SEPARATOR);

        // 按段落累积切�?
        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;
        for (String paragraph : paragraphs) {
            if (StrUtil.isEmpty(paragraph)) {
                continue;
            }
            int paragraphTokens = tokenEstimator.estimate(paragraph);
            // 如果加上这个段落会超过限�?
            if (currentTokens + paragraphTokens > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
                currentTokens = 0;
            }
            // 添加段落
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
            currentTokens += paragraphTokens;
        }

        // 添加最后一�?
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks.isEmpty() ? Collections.singletonList(content) : chunks;
    }

    /**
     * QA 对数据结�?
     */
    @AllArgsConstructor
    private static class QaPair {

        String question;
        String answer;
        String fullText;

    }

    /**
     * Token 估算器接�?
     */
    public interface TokenEstimator {

        int estimate(String text);

    }

    /**
     * 简单的 Token 估算器实�?
     * 中文�? 字符 �?1 Token
     * 英文�? 单词 �?1.3 Token
     */
    private static class SimpleTokenEstimator implements TokenEstimator {

        @Override
        public int estimate(String text) {
            if (StrUtil.isEmpty(text)) {
                return 0;
            }

            int chineseChars = 0;
            int englishWords = 0;
            // 简单统计中英文
            for (char c : text.toCharArray()) {
                if (c >= 0x4E00 && c <= 0x9FA5) {
                    chineseChars++;
                }
            }
            // 英文单词估算
            String[] words = text.split("\\s+");
            for (String word : words) {
                if (word.matches(".*[a-zA-Z].*")) {
                    englishWords++;
                }
            }
            return chineseChars + (int) (englishWords * 1.3);
        }
    }

}
