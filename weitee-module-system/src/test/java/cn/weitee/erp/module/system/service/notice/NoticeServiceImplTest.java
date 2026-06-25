package cn.weitee.erp.module.system.service.notice;

import cn.weitee.erp.framework.common.enums.CommonStatusEnum;
import cn.weitee.erp.framework.common.pojo.PageResult;
import cn.weitee.erp.framework.test.core.ut.BaseDbUnitTest;
import cn.weitee.erp.module.system.controller.admin.notice.vo.NoticeBatchUpdateReqVO;
import cn.weitee.erp.module.system.controller.admin.notice.vo.NoticeBatchUpdateResultVO;
import cn.weitee.erp.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.weitee.erp.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.weitee.erp.module.system.dal.dataobject.notice.NoticeDO;
import cn.weitee.erp.module.system.dal.mysql.notice.NoticeMapper;
import cn.weitee.erp.module.system.enums.notice.NoticeTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static cn.weitee.erp.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.weitee.erp.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.weitee.erp.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.weitee.erp.framework.test.core.util.RandomUtils.randomLongId;
import static cn.weitee.erp.framework.test.core.util.RandomUtils.randomPojo;
import static cn.weitee.erp.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static cn.weitee.erp.module.system.enums.ErrorCodeConstants.NOTICE_BATCH_UPDATE_FIELD_NOT_SUPPORT;
import static cn.weitee.erp.module.system.enums.ErrorCodeConstants.NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID;
import static org.junit.jupiter.api.Assertions.*;

@Import(NoticeServiceImpl.class)
class NoticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NoticeServiceImpl noticeService;

    @Resource
    private NoticeMapper noticeMapper;

    @Test
    public void testGetNoticePage_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class, o -> {
            o.setTitle("尼古拉斯赵四来啦！");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        noticeMapper.insert(dbNotice);
        // 测试 title 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setTitle("尼古拉斯凯奇也来啦！")));
        // 测试 status 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setTitle("尼古拉斯赵四来啦！");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(reqVO);
        // 验证查询结果经过筛选
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotice, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 查询
        NoticeDO notice = noticeService.getNotice(dbNotice.getId());

        // 验证插入与读取对象是否一致
        assertNotNull(notice);
        assertPojoEquals(dbNotice, notice);
    }

    @Test
    public void testCreateNotice_success() {
        // 准备参数
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class)
                .setId(null); // 避免 id 被赋值

        // 调用
        Long noticeId = noticeService.createNotice(reqVO);
        // 校验插入属性是否正确
        assertNotNull(noticeId);
        NoticeDO notice = noticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice, "id");
    }

    @Test
    public void testUpdateNotice_success() {
        // 插入前置数据
        NoticeDO dbNoticeDO = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNoticeDO);

        // 准备更新参数
        NoticeSaveReqVO reqVO = randomPojo(NoticeSaveReqVO.class, o -> o.setId(dbNoticeDO.getId()));

        // 更新
        noticeService.updateNotice(reqVO);
        // 检验是否更新成功
        NoticeDO notice = noticeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testDeleteNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 删除
        noticeService.deleteNotice(dbNotice.getId());

        // 检查是否删除成功
        assertNull(noticeMapper.selectById(dbNotice.getId()));
    }

    @Test
    public void testUpdateNoticeBatch_success() {
        // 插入前置数据
        NoticeDO notice01 = randomPojo(NoticeDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        noticeMapper.insert(notice01);
        NoticeDO notice02 = randomPojo(NoticeDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        noticeMapper.insert(notice02);

        // 准备参数
        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(notice01.getId(), notice02.getId()));
        reqVO.setFieldKey("status");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(CommonStatusEnum.DISABLE.getStatus()));

        // 更新
        NoticeBatchUpdateResultVO result = noticeService.updateNoticeBatch(reqVO);

        // 检验更新结果
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(Arrays.asList(notice01.getId(), notice02.getId()), result.getUpdatedIds());
        assertTrue(result.getFailedItems().isEmpty());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), noticeMapper.selectById(notice01.getId()).getStatus());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), noticeMapper.selectById(notice02.getId()).getStatus());
    }

    @Test
    public void testUpdateNoticeBatch_fieldNotSupport() {
        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(randomLongId()));
        reqVO.setFieldKey("unknown");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(CommonStatusEnum.DISABLE.getStatus()));

        assertServiceException(() -> noticeService.updateNoticeBatch(reqVO),
                NOTICE_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
    }

    @Test
    public void testUpdateNoticeBatch_duplicateIds_success() {
        NoticeDO notice = randomPojo(NoticeDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(notice.getId(), notice.getId()));
        reqVO.setFieldKey("status");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(CommonStatusEnum.DISABLE.getStatus()));

        NoticeBatchUpdateResultVO result = noticeService.updateNoticeBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertEquals(Arrays.asList(notice.getId()), result.getUpdatedIds());
        assertTrue(result.getFailedItems().isEmpty());
        assertEquals(CommonStatusEnum.DISABLE.getStatus(), noticeMapper.selectById(notice.getId()).getStatus());
    }

    @Test
    public void testUpdateNoticeBatch_notExists() {
        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(Arrays.asList(randomLongId()));
        reqVO.setFieldKey("status");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(CommonStatusEnum.DISABLE.getStatus()));

        assertServiceException(() -> noticeService.updateNoticeBatch(reqVO), NOTICE_NOT_FOUND);
    }

    @Test
    public void testUpdateNoticeBatch_title_success() {
        NoticeDO notice = randomPojo(NoticeDO.class);
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(List.of(notice.getId()));
        reqVO.setFieldKey("title");
        reqVO.setMode("overwrite");
        reqVO.setValue("新公告标题");

        NoticeBatchUpdateResultVO result = noticeService.updateNoticeBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals("新公告标题", noticeMapper.selectById(notice.getId()).getTitle());
    }

    @Test
    public void testUpdateNoticeBatch_type_success() {
        NoticeDO notice = randomPojo(NoticeDO.class);
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(List.of(notice.getId()));
        reqVO.setFieldKey("type");
        reqVO.setMode("overwrite");
        reqVO.setValue(String.valueOf(NoticeTypeEnum.ANNOUNCEMENT.getType()));

        NoticeBatchUpdateResultVO result = noticeService.updateNoticeBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals(NoticeTypeEnum.ANNOUNCEMENT.getType(), noticeMapper.selectById(notice.getId()).getType());
    }

    @Test
    public void testUpdateNoticeBatch_content_success() {
        NoticeDO notice = randomPojo(NoticeDO.class);
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(List.of(notice.getId()));
        reqVO.setFieldKey("content");
        reqVO.setMode("overwrite");
        reqVO.setValue("新的公告内容");

        NoticeBatchUpdateResultVO result = noticeService.updateNoticeBatch(reqVO);

        assertEquals(1, result.getSuccessCount());
        assertEquals("新的公告内容", noticeMapper.selectById(notice.getId()).getContent());
    }

    @Test
    public void testUpdateNoticeBatch_titleTooLong() {
        NoticeDO notice = randomPojo(NoticeDO.class);
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(List.of(notice.getId()));
        reqVO.setFieldKey("title");
        reqVO.setMode("overwrite");
        reqVO.setValue("a".repeat(51));

        assertServiceException(() -> noticeService.updateNoticeBatch(reqVO),
                NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID, "title", reqVO.getValue());
    }

    @Test
    public void testUpdateNoticeBatch_typeInvalid() {
        NoticeDO notice = randomPojo(NoticeDO.class);
        noticeMapper.insert(notice);

        NoticeBatchUpdateReqVO reqVO = new NoticeBatchUpdateReqVO();
        reqVO.setIds(List.of(notice.getId()));
        reqVO.setFieldKey("type");
        reqVO.setMode("overwrite");
        reqVO.setValue("3");

        assertServiceException(() -> noticeService.updateNoticeBatch(reqVO),
                NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID, "type", reqVO.getValue());
    }

    @Test
    public void testValidateNoticeExists_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 成功调用
        noticeService.validateNoticeExists(dbNotice.getId());
    }

    @Test
    public void testValidateNoticeExists_noExists() {
        assertServiceException(() ->
                noticeService.validateNoticeExists(randomLongId()), NOTICE_NOT_FOUND);
    }

}
