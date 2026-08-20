package cn.weitee.erp.module.infra.service.file;

import cn.weitee.erp.module.infra.framework.drm.DrmContext;
import cn.weitee.erp.module.infra.framework.drm.DrmDetectionResult;
import cn.weitee.erp.module.infra.framework.drm.DrmPolicyMapper;
import cn.weitee.erp.module.infra.framework.drm.DrmServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomingFileProtectionServiceTest {

    @Mock
    private DrmServiceClient drmServiceClient;
    @Mock
    private DrmPolicyMapper drmPolicyMapper;

    @Test
    void preparePlainContent_encryptedFile_decryptsBeforeReturn() {
        byte[] encrypted = {1, 2};
        byte[] plain = {3, 4};
        DrmContext context = DrmContext.builder().authorId("100").build();
        when(drmServiceClient.detect(encrypted, "a.xlsx")).thenReturn(new DrmDetectionResult(true));
        when(drmPolicyMapper.currentUserContext()).thenReturn(context);
        when(drmServiceClient.decrypt(encrypted, "a.xlsx", context)).thenReturn(plain);

        byte[] result = new IncomingFileProtectionService(drmServiceClient, drmPolicyMapper)
                .preparePlainContent(encrypted, "a.xlsx");

        assertArrayEquals(plain, result);
        verify(drmServiceClient).decrypt(encrypted, "a.xlsx", context);
    }

    @Test
    void preparePlainContent_plainFile_doesNotCallDecrypt() {
        byte[] content = {1, 2};
        when(drmServiceClient.detect(content, "a.xlsx")).thenReturn(new DrmDetectionResult(false));

        byte[] result = new IncomingFileProtectionService(drmServiceClient, drmPolicyMapper)
                .preparePlainContent(content, "a.xlsx");

        assertArrayEquals(content, result);
        verify(drmServiceClient, never()).decrypt(any(), anyString(), any());
    }

    @Test
    void preparePlainContent_drmFailure_propagatesException() {
        byte[] encrypted = {1, 2};
        when(drmServiceClient.detect(encrypted, "a.xlsx"))
                .thenThrow(new RuntimeException("drm unavailable"));

        assertThrows(RuntimeException.class, () -> new IncomingFileProtectionService(drmServiceClient, drmPolicyMapper)
                .preparePlainContent(encrypted, "a.xlsx"));
    }
}
