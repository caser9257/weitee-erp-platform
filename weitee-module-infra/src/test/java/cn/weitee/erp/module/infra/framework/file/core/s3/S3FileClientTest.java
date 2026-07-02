package cn.weitee.erp.module.infra.framework.file.core.s3;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.IdUtil;
import cn.weitee.erp.framework.common.util.validation.ValidationUtils;
import cn.weitee.erp.module.infra.framework.file.core.client.s3.S3FileClient;
import cn.weitee.erp.module.infra.framework.file.core.client.s3.S3FileClientConfig;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * {@link S3FileClient} 集成测试。
 *
 * <p>默认禁用，需要本地准备对应的对象存储环境后再手工启用。</p>
 */
@SuppressWarnings("resource")
public class S3FileClientTest {

    @Test
    @Disabled
    public void testMinio() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setAccessKey("admin");
        config.setAccessSecret("password");
        config.setBucket("weitee-test");
        config.setDomain(null);
        config.setEndpoint("http://127.0.0.1:9000");
        config.setEnablePathStyleAccess(true);
        config.setEnablePublicAccess(true);
        config.setRegion("us-east-1");

        testExecuteUpload(config);
    }

    @Test
    @Disabled
    public void testQiniuPrivateGet() {
        S3FileClientConfig config = new S3FileClientConfig();
        config.setAccessKey(System.getenv("QINIU_ACCESS_KEY"));
        config.setAccessSecret(System.getenv("QINIU_SECRET_KEY"));
        config.setBucket("weitee-private");
        config.setDomain(System.getenv("QINIU_PRIVATE_DOMAIN"));
        config.setEndpoint("s3-cn-south-1.qiniucs.com");
        config.setEnablePathStyleAccess(false);
        config.setEnablePublicAccess(false);

        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        S3FileClient client = new S3FileClient(0L, config);
        client.init();
        String presignedUrl = client.presignGetUrl("output.png", 300);
        System.out.println(presignedUrl);
    }

    private void testExecuteUpload(S3FileClientConfig config) {
        ValidationUtils.validate(Validation.buildDefaultValidatorFactory().getValidator(), config);
        S3FileClient client = new S3FileClient(0L, config);
        client.init();

        String path = IdUtil.fastSimpleUUID() + ".jpg";
        byte[] content = ResourceUtil.readBytes("file/erweima.jpg");
        String fullPath = client.upload(content, path, "image/jpeg");
        System.out.println("访问地址：" + fullPath);

        byte[] bytes = client.getContent(path);
        System.out.println("文件内容：" + bytes.length);
    }

}
