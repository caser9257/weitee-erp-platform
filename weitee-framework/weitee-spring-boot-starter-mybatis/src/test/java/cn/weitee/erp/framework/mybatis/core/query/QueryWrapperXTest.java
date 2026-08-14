package cn.weitee.erp.framework.mybatis.core.query;

import cn.weitee.erp.framework.mybatis.core.util.JdbcUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class QueryWrapperXTest {

    @Test
    void limitN_shouldAppendLimitForMysql() {
        try (MockedStatic<JdbcUtils> jdbcUtils = mockStatic(JdbcUtils.class)) {
            jdbcUtils.when(JdbcUtils::getDbType).thenReturn(DbType.MYSQL);

            QueryWrapperX<Object> query = new QueryWrapperX<Object>().limitN(1);

            assertEquals(" LIMIT 1", query.getSqlSegment());
        }
    }

}
