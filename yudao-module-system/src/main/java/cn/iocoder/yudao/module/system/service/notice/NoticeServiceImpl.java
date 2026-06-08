package cn.iocoder.yudao.module.system.service.notice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeBatchUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeBatchUpdateResultVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.mysql.notice.NoticeMapper;
import cn.iocoder.yudao.module.system.enums.notice.NoticeTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_BATCH_UPDATE_FIELD_NOT_SUPPORT;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID;

/**
 * 通知公告 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    private static final int NOTICE_TITLE_MAX_LENGTH = 50;

    @Resource
    private NoticeMapper noticeMapper;

    @Override
    public Long createNotice(NoticeSaveReqVO createReqVO) {
        NoticeDO notice = BeanUtils.toBean(createReqVO, NoticeDO.class);
        noticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public void updateNotice(NoticeSaveReqVO updateReqVO) {
        // 校验是否存在
        validateNoticeExists(updateReqVO.getId());
        // 更新通知公告
        NoticeDO updateObj = BeanUtils.toBean(updateReqVO, NoticeDO.class);
        noticeMapper.updateById(updateObj);
    }

    @Override
    public void deleteNotice(Long id) {
        // 校验是否存在
        validateNoticeExists(id);
        // 删除通知公告
        noticeMapper.deleteById(id);
    }

    @Override
    public void deleteNoticeList(List<Long> ids) {
        noticeMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeBatchUpdateResultVO updateNoticeBatch(NoticeBatchUpdateReqVO reqVO) {
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(reqVO.getIds()));
        List<NoticeDO> notices = noticeMapper.selectByIds(uniqueIds);
        Map<Long, NoticeDO> noticeMap = CollectionUtils.convertMap(notices, NoticeDO::getId);
        for (Long id : uniqueIds) {
            if (!noticeMap.containsKey(id)) {
                throw exception(NOTICE_NOT_FOUND);
            }
        }

        NoticeBatchFieldUpdater updater = createUpdater(reqVO);
        uniqueIds.forEach(id -> {
            NoticeDO updateObj = new NoticeDO();
            updateObj.setId(id);
            updater.apply(updateObj);
            noticeMapper.updateById(updateObj);
        });

        NoticeBatchUpdateResultVO result = new NoticeBatchUpdateResultVO();
        result.setSuccessCount(uniqueIds.size());
        result.setFailureCount(0);
        result.setUpdatedIds(uniqueIds);
        result.setFailedItems(Collections.emptyList());
        return result;
    }

    @VisibleForTesting
    NoticeBatchFieldUpdater createUpdater(NoticeBatchUpdateReqVO reqVO) {
        switch (reqVO.getFieldKey()) {
            case "title":
                return notice -> {
                    if (reqVO.getValue().length() > NOTICE_TITLE_MAX_LENGTH) {
                        throw exception(NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID, reqVO.getFieldKey(), reqVO.getValue());
                    }
                    notice.setTitle(reqVO.getValue());
                };
            case "type":
                return notice -> notice.setType(parseIntegerValue(reqVO.getFieldKey(), reqVO.getValue(),
                        NoticeTypeEnum.NOTICE.getType(), NoticeTypeEnum.ANNOUNCEMENT.getType()));
            case "content":
                return notice -> notice.setContent(reqVO.getValue());
            case "status":
                return notice -> notice.setStatus(parseIntegerValue(reqVO.getFieldKey(), reqVO.getValue(), CommonStatusEnum.ARRAYS));
            default:
                throw exception(NOTICE_BATCH_UPDATE_FIELD_NOT_SUPPORT, reqVO.getFieldKey());
        }
    }

    private Integer parseIntegerValue(String fieldKey, String value, Integer... allowedValues) {
        Integer parsedValue;
        try {
            parsedValue = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw exception(NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        if (allowedValues != null && allowedValues.length > 0) {
            for (Integer allowedValue : allowedValues) {
                if (allowedValue != null && allowedValue.equals(parsedValue)) {
                    return parsedValue;
                }
            }
            throw exception(NOTICE_BATCH_UPDATE_FIELD_VALUE_INVALID, fieldKey, value);
        }
        return parsedValue;
    }

    @FunctionalInterface
    @VisibleForTesting
    interface NoticeBatchFieldUpdater {
        void apply(NoticeDO notice);
    }

    @Override
    public PageResult<NoticeDO> getNoticePage(NoticePageReqVO reqVO) {
        return noticeMapper.selectPage(reqVO);
    }

    @Override
    public NoticeDO getNotice(Long id) {
        return noticeMapper.selectById(id);
    }

    @VisibleForTesting
    public void validateNoticeExists(Long id) {
        if (id == null) {
            return;
        }
        NoticeDO notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
    }

}
