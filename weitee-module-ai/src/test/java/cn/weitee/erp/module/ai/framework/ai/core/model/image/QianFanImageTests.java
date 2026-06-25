package cn.weitee.erp.module.ai.framework.ai.core.model.image;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springaicommunity.qianfan.QianFanImageModel;
import org.springaicommunity.qianfan.QianFanImageOptions;
import org.springaicommunity.qianfan.api.QianFanImageApi;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;

import static cn.weitee.erp.module.ai.framework.ai.core.model.image.StabilityAiImageModelTests.viewImage;

// TODO @芋艿：百度千�?API 提供�?V2 版本，目�?Spring AI 不兼容，可关�?<https://github.com/spring-projects/spring-ai/issues/2179> 进展

/**
 * {@link QianFanImageModel} 集成测试�?
 */
public class QianFanImageTests {

    private final QianFanImageModel imageModel = new QianFanImageModel(
            new QianFanImageApi("qS8k8dYr2nXunagK4SSU8Xjj", "pHGbx51ql2f0hOyabQvSZezahVC3hh3e")); // 密钥

    @Test
    @Disabled
    public void testCall() {
        // 准备参数
        // 只支�?1024x1024�?68x768�?68x1024�?024x768�?76x1024�?024x576
        QianFanImageOptions imageOptions = QianFanImageOptions.builder()
                .model(QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue())
                .width(1024).height(1024)
                .N(1)
                .build();
        ImagePrompt prompt = new ImagePrompt("good", imageOptions);

        // 方法调用
        ImageResponse response = imageModel.call(prompt);
        // 打印结果
        String b64Json = response.getResult().getOutput().getB64Json();
        System.out.println(response);
        viewImage(b64Json);
    }

}
