package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductCadenceDO;
import cn.weitee.erp.module.erp.dal.dataobject.product.ErpProductDO;

/**
 * 产品聚合与 Cadence 扩展数据之间的显式映射，避免复制产品主数据。
 */
final class ErpProductCadenceConverter {

    private ErpProductCadenceConverter() {
    }

    static boolean hasContent(ErpProductDO product) {
        return product.getSchematicPart() != null
                || product.getPcbFootprint() != null
                || product.getCadenceDescription() != null
                || product.getManufacturerPartNumber() != null
                || product.getDimension() != null
                || product.getThreeDLib() != null
                || product.getDatasheet() != null
                || product.getLifecycle() != null
                || product.getPreferredPart() != null
                || product.getOperatingTemperature() != null
                || product.getMountingType() != null
                || product.getDnp() != null
                || product.getImportedOrReplacement() != null
                || product.getSecondDescription() != null
                || product.getThirdDescription() != null
                || product.getFourthDescription() != null;
    }

    static ErpProductCadenceDO toCadence(ErpProductDO product) {
        return ErpProductCadenceDO.builder()
                .productId(product.getId())
                .schematicPart(product.getSchematicPart())
                .pcbFootprint(product.getPcbFootprint())
                .cadenceDescription(product.getCadenceDescription())
                .manufacturerPartNumber(product.getManufacturerPartNumber())
                .dimension(product.getDimension())
                .threeDLib(product.getThreeDLib())
                .datasheet(product.getDatasheet())
                .lifecycle(product.getLifecycle())
                .preferredPart(product.getPreferredPart())
                .operatingTemperature(product.getOperatingTemperature())
                .mountingType(product.getMountingType())
                .dnp(product.getDnp())
                .importedOrReplacement(product.getImportedOrReplacement())
                .secondDescription(product.getSecondDescription())
                .thirdDescription(product.getThirdDescription())
                .fourthDescription(product.getFourthDescription())
                .build();
    }

    static void applyToProduct(ErpProductCadenceDO cadence, ErpProductDO product) {
        if (cadence == null) {
            return;
        }
        product.setSchematicPart(cadence.getSchematicPart());
        product.setPcbFootprint(cadence.getPcbFootprint());
        product.setCadenceDescription(cadence.getCadenceDescription());
        product.setManufacturerPartNumber(cadence.getManufacturerPartNumber());
        product.setDimension(cadence.getDimension());
        product.setThreeDLib(cadence.getThreeDLib());
        product.setDatasheet(cadence.getDatasheet());
        product.setLifecycle(cadence.getLifecycle());
        product.setPreferredPart(cadence.getPreferredPart());
        product.setOperatingTemperature(cadence.getOperatingTemperature());
        product.setMountingType(cadence.getMountingType());
        product.setDnp(cadence.getDnp());
        product.setImportedOrReplacement(cadence.getImportedOrReplacement());
        product.setSecondDescription(cadence.getSecondDescription());
        product.setThirdDescription(cadence.getThirdDescription());
        product.setFourthDescription(cadence.getFourthDescription());
    }

    static void mergeMissingValues(ErpProductCadenceDO existing, ErpProductDO target) {
        if (existing == null) {
            return;
        }
        if (target.getSchematicPart() == null) target.setSchematicPart(existing.getSchematicPart());
        if (target.getPcbFootprint() == null) target.setPcbFootprint(existing.getPcbFootprint());
        if (target.getCadenceDescription() == null) target.setCadenceDescription(existing.getCadenceDescription());
        if (target.getManufacturerPartNumber() == null) target.setManufacturerPartNumber(existing.getManufacturerPartNumber());
        if (target.getDimension() == null) target.setDimension(existing.getDimension());
        if (target.getThreeDLib() == null) target.setThreeDLib(existing.getThreeDLib());
        if (target.getDatasheet() == null) target.setDatasheet(existing.getDatasheet());
        if (target.getLifecycle() == null) target.setLifecycle(existing.getLifecycle());
        if (target.getPreferredPart() == null) target.setPreferredPart(existing.getPreferredPart());
        if (target.getOperatingTemperature() == null) target.setOperatingTemperature(existing.getOperatingTemperature());
        if (target.getMountingType() == null) target.setMountingType(existing.getMountingType());
        if (target.getDnp() == null) target.setDnp(existing.getDnp());
        if (target.getImportedOrReplacement() == null) target.setImportedOrReplacement(existing.getImportedOrReplacement());
        if (target.getSecondDescription() == null) target.setSecondDescription(existing.getSecondDescription());
        if (target.getThirdDescription() == null) target.setThirdDescription(existing.getThirdDescription());
        if (target.getFourthDescription() == null) target.setFourthDescription(existing.getFourthDescription());
    }

}
