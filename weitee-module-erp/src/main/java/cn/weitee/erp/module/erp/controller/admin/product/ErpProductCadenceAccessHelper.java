package cn.weitee.erp.module.erp.controller.admin.product;

import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductApprovalViewRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.weitee.erp.module.erp.controller.admin.product.vo.product.ProductSaveReqVO;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 产品 Cadence 字段的部门级访问控制。
 */
public final class ErpProductCadenceAccessHelper {

    public static final Long CADENCE_RD_DEPT_ID = 103L;

    private ErpProductCadenceAccessHelper() {
    }

    public static boolean canAccess(Long deptId) {
        return CADENCE_RD_DEPT_ID.equals(deptId);
    }

    static boolean hasCadenceFields(ProductSaveReqVO reqVO) {
        return Stream.of(reqVO.getPcbComponent(), reqVO.getSchematicPart(), reqVO.getPcbFootprint(),
                        reqVO.getCadenceDescription(), reqVO.getManufacturerPartNumber(), reqVO.getDimension(),
                        reqVO.getThreeDLib(),
                        reqVO.getDatasheet(), reqVO.getLifecycle(), reqVO.getPreferredPart(),
                        reqVO.getOperatingTemperature(), reqVO.getMountingType(), reqVO.getDnp(),
                        reqVO.getImportedOrReplacement(), reqVO.getSecondDescription(), reqVO.getThirdDescription(),
                        reqVO.getFourthDescription())
                .anyMatch(Objects::nonNull);
    }

    static void maskCadenceFields(ErpProductRespVO product) {
        if (product == null) {
            return;
        }
        product.setPcbComponent(null);
        product.setSchematicPart(null);
        product.setPcbFootprint(null);
        product.setCadenceDescription(null);
        product.setManufacturerPartNumber(null);
        product.setDimension(null);
        product.setThreeDLib(null);
        product.setDatasheet(null);
        product.setLifecycle(null);
        product.setPreferredPart(null);
        product.setOperatingTemperature(null);
        product.setMountingType(null);
        product.setDnp(null);
        product.setImportedOrReplacement(null);
        product.setSecondDescription(null);
        product.setThirdDescription(null);
        product.setFourthDescription(null);
    }

    static void maskCadenceFields(ErpProductApprovalViewRespVO view) {
        if (view == null) {
            return;
        }
        maskCadenceFields(view.getCurrent());
        maskCadenceFields(view.getTarget());
        if (view.getDiffs() != null) {
            view.setDiffs(view.getDiffs().stream()
                    .filter(diff -> !isCadenceField(diff.getField()))
                    .toList());
        }
        if (view.getChangedFields() != null) {
            view.setChangedFields(Arrays.stream(view.getChangedFields().split(","))
                    .filter(field -> !isCadenceField(field))
                    .collect(Collectors.joining(",")));
        }
    }

    private static boolean isCadenceField(String field) {
        return "pcbComponent".equals(field) || (field != null && field.startsWith("cadence"));
    }

}
