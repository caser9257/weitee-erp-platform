package cn.weitee.erp.module.mes.service.scheduling;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceOccupancyTest {

    @Test
    void addAndFindOverlap_shouldDetectOverlapping() {
        ResourceOccupancy occupancy = new ResourceOccupancy();
        occupancy.add(1L, LocalDateTime.of(2026, 8, 17, 8, 0), LocalDateTime.of(2026, 8, 17, 12, 0));

        // 重叠：10:00-14:00
        assertTrue(occupancy.findOverlap(1L,
                LocalDateTime.of(2026, 8, 17, 10, 0), LocalDateTime.of(2026, 8, 17, 14, 0)).isPresent());
        // 不重叠：13:00-14:00
        assertTrue(occupancy.findOverlap(1L,
                LocalDateTime.of(2026, 8, 17, 13, 0), LocalDateTime.of(2026, 8, 17, 14, 0)).isEmpty());
        // 边界相接：12:00-14:00 不重叠（end == start）
        assertTrue(occupancy.findOverlap(1L,
                LocalDateTime.of(2026, 8, 17, 12, 0), LocalDateTime.of(2026, 8, 17, 14, 0)).isEmpty());
    }

    @Test
    void findOverlaps_shouldReturnAllConflicts() {
        ResourceOccupancy occupancy = new ResourceOccupancy();
        occupancy.add(1L, LocalDateTime.of(2026, 8, 17, 8, 0), LocalDateTime.of(2026, 8, 17, 10, 0));
        occupancy.add(1L, LocalDateTime.of(2026, 8, 17, 9, 0), LocalDateTime.of(2026, 8, 17, 11, 0));
        occupancy.add(1L, LocalDateTime.of(2026, 8, 17, 13, 0), LocalDateTime.of(2026, 8, 17, 14, 0));

        assertEquals(3, occupancy.findOverlaps(1L,
                LocalDateTime.of(2026, 8, 17, 9, 30), LocalDateTime.of(2026, 8, 17, 13, 30)).size());
    }
}
