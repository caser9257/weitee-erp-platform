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
class ProtectedExportServiceTest {

    @Mock
    private DrmServiceClient drmServiceClient;
    @Mock
    private DrmPolicyMapper drmPolicyMapper;

    @Test
    void encryptIfNeeded_plainFile_encryptsBeforeReturn() {
        byte[] plain = {1, 2};
        byte[] encrypted = {3, 4};
        DrmContext context = DrmContext.builder().authorId("100").build();
        when(drmServiceClient.detect(plain, "a.xlsx")).thenReturn(new DrmDetectionResult(false));
        when(drmPolicyMapper.currentUserContext()).thenReturn(context);
        when(drmServiceClient.encrypt(plain, "a.xlsx", context)).thenReturn(encrypted);

        byte[] result = new ProtectedExportService(drmServiceClient, drmPolicyMapper)
                .encryptIfNeeded(plain, "a.xlsx");

        assertArrayEquals(encrypted, result);
        verify(drmServiceClient).encrypt(plain, "a.xlsx", context);
    }

    @Test
    void encryptIfNeeded_alreadyEncrypted_doesNotEncryptAgain() {
        byte[] encrypted = {1, 2};
        when(drmServiceClient.detect(encrypted, "a.xlsx")).thenReturn(new DrmDetectionResult(true));

        byte[] result = new ProtectedExportService(drmServiceClient, drmPolicyMapper)
                .encryptIfNeeded(encrypted, "a.xlsx");

        assertArrayEquals(encrypted, result);
        verify(drmServiceClient, never()).encrypt(any(), anyString(), any());
    }

    @Test
    void encryptIfNeeded_drmFailure_propagatesException() {
        byte[] plain = {1, 2};
        when(drmServiceClient.detect(plain, "a.xlsx"))
                .thenThrow(new RuntimeException("drm unavailable"));

        assertThrows(RuntimeException.class, () -> new ProtectedExportService(drmServiceClient, drmPolicyMapper)
                .encryptIfNeeded(plain, "a.xlsx"));
    }
}
