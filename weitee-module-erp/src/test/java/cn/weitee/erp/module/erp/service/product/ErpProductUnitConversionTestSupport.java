package cn.weitee.erp.module.erp.service.product;

import cn.weitee.erp.module.erp.service.product.ErpProductUnitConversionService.ConversionRequest;
import cn.weitee.erp.module.erp.service.product.ErpProductUnitConversionService.ConversionResult;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;

/**
 * 单测支撑：构造"基本单位直通"的单位换算服务。
 *
 * 用于未关注单位换算的既有单测，行为等价于按产品基本单位录入：
 * inputCount 原样作为 baseCount，不产生换算率。
 */
public final class ErpProductUnitConversionTestSupport {

    private ErpProductUnitConversionTestSupport() {
    }

    public static ErpProductUnitConversionService passthrough() {
        return (ErpProductUnitConversionService) Proxy.newProxyInstance(
                ErpProductUnitConversionService.class.getClassLoader(),
                new Class<?>[]{ErpProductUnitConversionService.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "convertBatch" -> {
                            @SuppressWarnings("unchecked")
                            List<ConversionRequest> requests = (List<ConversionRequest>) args[0];
                            return requests.stream()
                                    .map(r -> new ConversionResult(
                                            r.getInputUnitId(), r.getInputCount(), null, r.getInputCount()))
                                    .toList();
                        }
                        case "convert" -> {
                            return new ConversionResult(
                                    (Long) args[1], (BigDecimal) args[2], null, (BigDecimal) args[2]);
                        }
                        default -> {
                            return null;
                        }
                    }
                });
    }

}
