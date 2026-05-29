package cn.iocoder.yudao.module.erp.service.mrp;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.mrp.ErpMrpStockReservationSummaryDO;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationMapper;
import cn.iocoder.yudao.module.erp.dal.mysql.mrp.ErpMrpStockReservationSummaryMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class ErpMrpStockReservationSummaryServiceImpl implements ErpMrpStockReservationSummaryService {

    @Resource
    private ErpMrpStockReservationMapper erpMrpStockReservationMapper;
    @Resource
    private ErpMrpStockReservationSummaryMapper erpMrpStockReservationSummaryMapper;

    @Override
    public List<ErpMrpStockReservationSummaryDO> getActiveSummaryList() {
        return erpMrpStockReservationSummaryMapper.selectActiveList();
    }

    @Override
    public void refreshSummaryByProductIds(Collection<Long> productIds) {
        Set<Long> targetProductIds = normalizeProductIds(productIds);
        if (CollUtil.isEmpty(targetProductIds)) {
            return;
        }
        Map<Long, List<ErpMrpStockReservationDO>> reservationMap = erpMrpStockReservationMapper
                .selectActiveListByProductIds(targetProductIds).stream()
                .collect(Collectors.groupingBy(ErpMrpStockReservationDO::getProductId));
        for (Long productId : targetProductIds) {
            List<ErpMrpStockReservationDO> reservations = reservationMap.getOrDefault(productId, Collections.emptyList());
            upsertSummary(productId, reservations);
        }
    }

    private void upsertSummary(Long productId, List<ErpMrpStockReservationDO> reservations) {
        ErpMrpStockReservationSummaryDO existing = erpMrpStockReservationSummaryMapper.selectByProductId(productId);
        BigDecimal activeReservedQty = reservations.stream()
                .map(ErpMrpStockReservationDO::getReservedQty)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int activeReservationCount = reservations.size();
        int activeProjectCount = (int) reservations.stream()
                .map(ErpMrpStockReservationDO::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        LocalDateTime lastReservedTime = reservations.stream()
                .map(ErpMrpStockReservationDO::getCreateTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
        if (existing == null) {
            if (activeReservationCount <= 0) {
                return;
            }
            erpMrpStockReservationSummaryMapper.insert(new ErpMrpStockReservationSummaryDO()
                    .setProductId(productId)
                    .setActiveReservedQty(activeReservedQty)
                    .setActiveProjectCount(activeProjectCount)
                    .setActiveReservationCount(activeReservationCount)
                    .setLastReservedTime(lastReservedTime)
                    .setVersion(1L));
            return;
        }
        erpMrpStockReservationSummaryMapper.updateById(new ErpMrpStockReservationSummaryDO()
                .setId(existing.getId())
                .setActiveReservedQty(activeReservedQty)
                .setActiveProjectCount(activeProjectCount)
                .setActiveReservationCount(activeReservationCount)
                .setLastReservedTime(lastReservedTime)
                .setVersion(defaultLong(existing.getVersion()) + 1));
    }

    private Set<Long> normalizeProductIds(Collection<Long> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return Collections.emptySet();
        }
        return productIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

}
