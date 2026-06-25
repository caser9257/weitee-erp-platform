package cn.weitee.erp.module.system.api.notify;

import cn.weitee.erp.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.weitee.erp.module.system.service.notify.NotifySendService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 站内信发送 API 实现类
 *
 * @author xrcoder
 */
@Service
public class NotifyMessageSendApiImpl implements NotifyMessageSendApi {

    @Resource
    private NotifySendService notifySendService;

    @Override
    public Long sendSingleMessageToAdmin(NotifySendSingleToUserReqDTO reqDTO) {
        return notifySendService.sendSingleNotifyToAdmin(reqDTO.getUserId(),
                reqDTO.getTemplateCode(), reqDTO.getTemplateParams());
    }

    @Override
    public Long sendSingleMessageToMember(NotifySendSingleToUserReqDTO reqDTO) {
        throw new UnsupportedOperationException("会员用户已不支持站内信发送");
    }

}
