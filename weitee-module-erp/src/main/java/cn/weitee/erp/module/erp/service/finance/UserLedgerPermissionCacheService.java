package cn.weitee.erp.module.erp.service.finance;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户账簿权限缓存服务
 * 使用 Redis 缓存用户的可见账簿列表，减少数据库查询
 */
@Slf4j
@Service
public class UserLedgerPermissionCacheService {

    /**
     * 缓存过期时间（分钟）
     */
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取用户可见的账簿ID列表
     *
     * @param userId 用户ID
     * @return 账簿ID列表，null 表示不限制（可看所有账簿）
     */
    public List<Long> getVisibleLedgerIds(Long userId) {
        String key = buildCacheKey(userId);
        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(key);
            if (cachedValue == null) {
                return null;
            }
            if ("ALL".equals(cachedValue)) {
                return null; // null 表示不限制
            }
            if ("EMPTY".equals(cachedValue)) {
                return Collections.emptyList();
            }
            // 解析逗号分隔的ID列表
            return CollUtil.toList(cachedValue.split(",")).stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取用户账簿权限缓存失败，userId={}", userId, e);
            return null;
        }
    }

    /**
     * 设置用户可见的账簿ID列表
     *
     * @param userId      用户ID
     * @param ledgerIds   账簿ID列表，null 表示不限制
     */
    public void setVisibleLedgerIds(Long userId, List<Long> ledgerIds) {
        String key = buildCacheKey(userId);
        try {
            String value;
            if (ledgerIds == null) {
                value = "ALL";
            } else if (ledgerIds.isEmpty()) {
                value = "EMPTY";
            } else {
                value = CollUtil.join(ledgerIds, ",");
            }
            stringRedisTemplate.opsForValue().set(key, value, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.debug("设置用户账簿权限缓存。userId={}, ledgerIds={}", userId, value);
        } catch (Exception e) {
            log.warn("设置用户账簿权限缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 清除用户的账簿权限缓存
     *
     * @param userId 用户ID
     */
    public void clearCache(Long userId) {
        String key = buildCacheKey(userId);
        try {
            stringRedisTemplate.delete(key);
            log.debug("清除用户账簿权限缓存。userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户账簿权限缓存失败，userId={}", userId, e);
        }
    }

    /**
     * 构建缓存 key
     */
    private String buildCacheKey(Long userId) {
        return "erp:finance:ledger:permission:user:" + userId;
    }

}
