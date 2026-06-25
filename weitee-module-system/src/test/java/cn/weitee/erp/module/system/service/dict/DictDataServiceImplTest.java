package cn.weitee.erp.module.system.service.dict;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.common.util.collection.ArrayUtils;
import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataBatchUpdateReqVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataBatchUpdateResultVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataPageReqVO;
import cn.weitee.erp.module.system.controller.admin.dict.vo.data.DictDataSaveReqVO;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictDataDO;
import cn.weitee.erp.module.system.dal.dataobject.dict.DictTypeDO;
import cn.weitee.erp.module.system.dal.mysql.dict.DictDataMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static cn.weitee.erp.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.weitee.erp.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.weitee.erp.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.weitee.erp.framework.test.core.util.RandomUtils.*;
import static cn.weitee.erp.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Import(DictDataServiceImpl.class)
public class DictDataServiceImplTest extends BaseDbUnitTest {

    @Resource
    private DictDataServiceImpl dictDataService;

    @Resource
    private DictDataMapper dictDataMapper;
    @MockBean
    private DictTypeService dictTypeService;

    @Test
    public void testGetDictDataList() {
        // mock 数据
        DictDataDO dictDataDO01 = randomDictDataDO().setDictType("yunai").setSort(2)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        dictDataMapper.insert(dictDataDO01);
        DictDataDO dictDataDO02 = randomDictDataDO().setDictType("yunai").setSort(1)
                .setStatus(CommonStatusEnum.ENABLE.getStatus());
        dictDataMapper.insert(dictDataDO02);
        DictDataDO dictDataDO03 = randomDictDataDO().setDictType("yunai").setSort(3)
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO03);
        DictDataDO dictDataDO04 = randomDictDataDO().setDictType("yunai2").setSort(3)
                .setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO04);
        // 准备参数
        Integer status = CommonStatusEnum.ENABLE.getStatus();
        String dictType = "yunai";

        // 调用
        List<DictDataDO> dictDataDOList = dictDataService.getDictDataList(status, dictType);
        // 断言
        assertEquals(2, dictDataDOList.size());
        assertPojoEquals(dictDataDO02, dictDataDOList.get(0));
        assertPojoEquals(dictDataDO01, dictDataDOList.get(1));
    }

    @Test
    public void testGetDictDataPage() {
        // mock 数据
        DictDataDO dbDictData = randomPojo(DictDataDO.class, o -> { // 等会查询到
            o.setLabel("芋艿");
            o.setDictType("yunai");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        dictDataMapper.insert(dbDictData);
        // 测试 label 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setLabel("艿")));
        // 测试 dictType 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setDictType("nai")));
        // 测试 status 不匹配
        dictDataMapper.insert(cloneIgnoreId(dbDictData, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        DictDataPageReqVO reqVO = new DictDataPageReqVO();
        reqVO.setLabel("芋");
        reqVO.setDictType("yunai");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<DictDataDO> pageResult = dictDataService.getDictDataPage(reqVO);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbDictData, pageResult.getList().get(0));
    }

    @Test
    public void testGetDictData() {
        // mock 数据
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        DictDataDO dictData = dictDataService.getDictData(id);
        // 断言
        assertPojoEquals(dbDictData, dictData);
    }

    @Test
    public void testCreateDictData_success() {
        // 准备参数
        DictDataSaveReqVO reqVO = randomPojo(DictDataSaveReqVO.class,
                o -> o.setStatus(randomCommonStatus()))
                .setId(null); // 防止 id 被赋值
        // mock 方法
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // 调用
        Long dictDataId = dictDataService.createDictData(reqVO);
        // 断言
        assertNotNull(dictDataId);
        // 校验记录的属性是否正确
        DictDataDO dictData = dictDataMapper.selectById(dictDataId);
        assertPojoEquals(reqVO, dictData, "id");
    }

    @Test
    public void testUpdateDictData_success() {
        // mock 数据
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        DictDataSaveReqVO reqVO = randomPojo(DictDataSaveReqVO.class, o -> {
            o.setId(dbDictData.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });
        // mock 方法，字典类型
        when(dictTypeService.getDictType(eq(reqVO.getDictType()))).thenReturn(randomDictTypeDO(reqVO.getDictType()));

        // 调用
        dictDataService.updateDictData(reqVO);
        // 校验是否更新正确
        DictDataDO dictData = dictDataMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, dictData);
    }

    @Test
    public void testUpdateDictDataBatch_status_success() {
        DictDataDO dictData01 = randomDictDataDO(o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        dictDataMapper.insert(dictData01);
        DictDataDO dictData02 = randomDictDataDO(o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        dictDataMapper.insert(dictData02);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData01.getId(), dictData02.getId()));
        reqVO.setFieldKey("status");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(CommonStatusEnum.DISABLE.getStatus()));

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(List.of(dictData01.getId(), dictData02.getId()), result.getUpdatedIds());
        assertTrue(result.getFailedItems().isEmpty());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), dictDataMapper.selectById(dictData01.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), dictDataMapper.selectById(dictData02.getId()).getStatus());
    }

    @Test
    public void testUpdateDictDataBatch_label_success() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId()));
        reqVO.setFieldKey("label");
        reqVO.setMode("overwrite");
        reqVO.setValue("新的字典标签");

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals("新的字典标签", dictDataMapper.selectById(dictData.getId()).getLabel());
    }

    @Test
    public void testUpdateDictDataBatch_sort_success() {
        DictDataDO dictData = randomDictDataDO(o -> o.setSort(1));
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId()));
        reqVO.setFieldKey("sort");
        reqVO.setMode("overwrite");
        reqVO.setValue("99");

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals(99, dictDataMapper.selectById(dictData.getId()).getSort());
    }

    @Test
    public void testUpdateDictDataBatch_colorType_success() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId()));
        reqVO.setFieldKey("colorType");
        reqVO.setMode("overwrite");
        reqVO.setValue("warning");

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals("warning", dictDataMapper.selectById(dictData.getId()).getColorType());
    }

    @Test
    public void testUpdateDictDataBatch_remark_success() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId()));
        reqVO.setFieldKey("remark");
        reqVO.setMode("overwrite");
        reqVO.setValue("批量修改备注");

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals("批量修改备注", dictDataMapper.selectById(dictData.getId()).getRemark());
    }

    @Test
    public void testUpdateDictDataBatch_fieldNotSupport() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId()));
        reqVO.setFieldKey("unknown");
        reqVO.setMode("overwrite");
        reqVO.setValue("1");

        assertServiceException(() -> dictDataService.updateDictDataBatch(reqVO),
                DICT_DATA_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
    }

    @Test
    public void testUpdateDictDataBatch_valueInvalid() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(List.of(dictData.getId()));
        reqVO.setFieldKey("status");
        reqVO.setMode("overwrite");
        reqVO.setValue("2");

        assertServiceException(() -> dictDataService.updateDictDataBatch(reqVO),
                DICT_DATA_BATCH_UPDATE_FIELD_VALUE_INVALID, "status", reqVO.getValue());
    }

    @Test
    public void testUpdateDictDataBatch_duplicateIds_success() {
        DictDataDO dictData = randomDictDataDO();
        dictDataMapper.insert(dictData);

        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(dictData.getId(), dictData.getId()));
        reqVO.setFieldKey("label");
        reqVO.setMode("overwrite");
        reqVO.setValue("重复 ID 标签");

        DictDataBatchUpdateResultVO result = dictDataService.updateDictDataBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals(Arrays.asList(dictData.getId()), result.getUpdatedIds());
        assertEquals("重复 ID 标签", dictDataMapper.selectById(dictData.getId()).getLabel());
    }

    @Test
    public void testUpdateDictDataBatch_notExists() {
        DictDataBatchUpdateReqVO reqVO = new DictDataBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(randomLongId()));
        reqVO.setFieldKey("label");
        reqVO.setMode("overwrite");
        reqVO.setValue("不存在");

        assertServiceException(() -> dictDataService.updateDictDataBatch(reqVO), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testDeleteDictData_success() {
        // mock 数据
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbDictData.getId();

        // 调用
        dictDataService.deleteDictData(id);
        // 校验数据不存在了
        assertNull(dictDataMapper.selectById(id));
    }

    @Test
    public void testValidateDictDataExists_success() {
        // mock 数据
        DictDataDO dbDictData = randomDictDataDO();
        dictDataMapper.insert(dbDictData);// @Sql: 先插入出一条存在的数据

        // 调用成功
        dictDataService.validateDictDataExists(dbDictData.getId());
    }

    @Test
    public void testValidateDictDataExists_notExists() {
        assertServiceException(() -> dictDataService.validateDictDataExists(randomLongId()), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeExists_success() {
        // mock 方法，数据类型被禁用
        String type = randomString();
        when(dictTypeService.getDictType(eq(type))).thenReturn(randomDictTypeDO(type));

        // 调用, 成功
        dictDataService.validateDictTypeExists(type);
    }

    @Test
    public void testValidateDictTypeExists_notExists() {
        assertServiceException(() -> dictDataService.validateDictTypeExists(randomString()), DICT_TYPE_NOT_EXISTS);
    }

    @Test
    public void testValidateDictTypeExists_notEnable() {
        // mock 方法，数据类型被禁用
        String dictType = randomString();
        when(dictTypeService.getDictType(eq(dictType))).thenReturn(
                randomPojo(DictTypeDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.validateDictTypeExists(dictType), DICT_TYPE_NOT_ENABLE);
    }

    @Test
    public void testValidateDictDataValueUnique_success() {
        // 调用，成功
        dictDataService.validateDictDataValueUnique(randomLongId(), randomString(), randomString());
    }

    @Test
    public void testValidateDictDataValueUnique_valueDuplicateForCreate() {
        // 准备参数
        String dictType = randomString();
        String value = randomString();
        // mock 数据
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // 调用，校验异常
        assertServiceException(() -> dictDataService.validateDictDataValueUnique(null, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testValidateDictDataValueUnique_valueDuplicateForUpdate() {
        // 准备参数
        Long id = randomLongId();
        String dictType = randomString();
        String value = randomString();
        // mock 数据
        dictDataMapper.insert(randomDictDataDO(o -> {
            o.setDictType(dictType);
            o.setValue(value);
        }));

        // 调用，校验异常
        assertServiceException(() -> dictDataService.validateDictDataValueUnique(id, dictType, value),
                DICT_DATA_VALUE_DUPLICATE);
    }

    @Test
    public void testGetDictDataCountByDictType() {
        // mock 数据
        dictDataMapper.insert(randomDictDataDO(o -> o.setDictType("yunai")));
        dictDataMapper.insert(randomDictDataDO(o -> o.setDictType("tudou")));
        dictDataMapper.insert(randomDictDataDO(o -> o.setDictType("yunai")));
        // 准备参数
        String dictType = "yunai";

        // 调用
        long count = dictDataService.getDictDataCountByDictType(dictType);
        // 校验
        assertEquals(2L, count);
    }

    @Test
    public void testValidateDictDataList_success() {
        // mock 数据
        DictDataDO dictDataDO = randomDictDataDO().setStatus(CommonStatusEnum.ENABLE.getStatus());
        dictDataMapper.insert(dictDataDO);
        // 准备参数
        String dictType = dictDataDO.getDictType();
        List<String> values = singletonList(dictDataDO.getValue());

        // 调用，无需断言
        dictDataService.validateDictDataList(dictType, values);
    }

    @Test
    public void testValidateDictDataList_notFound() {
        // 准备参数
        String dictType = randomString();
        List<String> values = singletonList(randomString());

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.validateDictDataList(dictType, values), DICT_DATA_NOT_EXISTS);
    }

    @Test
    public void testValidateDictDataList_notEnable() {
        // mock 数据
        DictDataDO dictDataDO = randomDictDataDO().setStatus(CommonStatusEnum.DISABLE.getStatus());
        dictDataMapper.insert(dictDataDO);
        // 准备参数
        String dictType = dictDataDO.getDictType();
        List<String> values = singletonList(dictDataDO.getValue());

        // 调用, 并断言异常
        assertServiceException(() -> dictDataService.validateDictDataList(dictType, values),
                DICT_DATA_NOT_ENABLE, dictDataDO.getLabel());
    }

    @Test
    public void testGetDictData_dictType() {
        // mock 数据
        DictDataDO dictDataDO = randomDictDataDO().setDictType("yunai").setValue("1");
        dictDataMapper.insert(dictDataDO);
        DictDataDO dictDataDO02 = randomDictDataDO().setDictType("yunai").setValue("2");
        dictDataMapper.insert(dictDataDO02);
        // 准备参数
        String dictType = "yunai";
        String value = "1";

        // 调用
        DictDataDO dbDictData = dictDataService.getDictData(dictType, value);
        // 断言
        assertEquals(dictDataDO, dbDictData);
    }

    @Test
    public void testParseDictData() {
        // mock 数据
        DictDataDO dictDataDO = randomDictDataDO().setDictType("yunai").setLabel("1");
        dictDataMapper.insert(dictDataDO);
        DictDataDO dictDataDO02 = randomDictDataDO().setDictType("yunai").setLabel("2");
        dictDataMapper.insert(dictDataDO02);
        // 准备参数
        String dictType = "yunai";
        String label = "1";

        // 调用
        DictDataDO dbDictData = dictDataService.parseDictData(dictType, label);
        // 断言
        assertEquals(dictDataDO, dbDictData);
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static DictDataDO randomDictDataDO(Consumer<DictDataDO>... consumers) {
        Consumer<DictDataDO> consumer = (o) -> {
            o.setStatus(randomCommonStatus()); // 保证 status 的范围
        };
        return randomPojo(DictDataDO.class, ArrayUtils.append(consumer, consumers));
    }

    /**
     * 生成一个有效的字典类型
     *
     * @param type 字典类型
     * @return DictTypeDO 对象
     */
    private static DictTypeDO randomDictTypeDO(String type) {
        return randomPojo(DictTypeDO.class, o -> {
            o.setType(type);
            o.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 保证 status 是开启
        });
    }

}
