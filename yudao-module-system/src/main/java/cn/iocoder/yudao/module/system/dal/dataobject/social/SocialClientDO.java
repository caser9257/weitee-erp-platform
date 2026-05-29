package cn.iocoder.yudao.module.system.dal.dataobject.social;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;

/**
 * 绀句氦瀹㈡埛绔?DO
 *
 * 瀵瑰簲 {@link AuthConfig} 閰嶇疆锛屾弧瓒充笉鍚岀鎴凤紝鏈夎嚜宸辩殑瀹㈡埛绔厤缃紝瀹炵幇绀句氦锛堜笁鏂癸級鐧诲綍
 *
 * @author 鑺嬮亾婧愮爜
 */
@TableName(value = "system_social_client", autoResultMap = true)
@KeySequence("system_social_client_seq") // 鐢ㄤ簬 Oracle銆丳ostgreSQL銆並ingbase銆丏B2銆丠2 鏁版嵁搴撶殑涓婚敭鑷銆傚鏋滄槸 MySQL 绛夋暟鎹簱锛屽彲涓嶅啓銆?
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialClientDO extends      BaseDO {

    /**
     * 缂栧彿锛岃嚜澧?
     */
    @TableId
    private Long id;
    /**
     * 搴旂敤鍚?
     */
    private String name;
    /**
     * 绀句氦绫诲瀷
     *
     * 鏋氫妇 {@link SocialTypeEnum}
     */
    private Integer socialType;
    /**
     * 鐢ㄦ埛绫诲瀷
     *
     * 鐩殑锛氫笉鍚岀敤鎴风被鍨嬶紝瀵瑰簲涓嶅悓鐨勫皬绋嬪簭锛岄渶瑕佽嚜宸辩殑閰嶇疆
     *
     * 鏋氫妇 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 鐘舵€?
     *
     * 鏋氫妇 {@link CommonStatusEnum}
     */
    private Integer status;

    /**
     * 瀹㈡埛绔?id
     */
    private String clientId;
    /**
     * 瀹㈡埛绔?Secret
     */
    private String clientSecret;

    /**
     * 浠ｇ悊缂栧彿
     *
     * 鐩墠鍙湁閮ㄥ垎鈥滅ぞ浜ょ被鍨嬧€濆湪浣跨敤锛?
     * 1. 浼佷笟寰俊锛氬搴旀巿鏉冩柟鐨勭綉椤靛簲鐢?ID
     */
    private String agentId;

    /**
     * publicKey 鍏挜
     *
     * 鐩墠鍙湁閮ㄥ垎鈥滅ぞ浜ょ被鍨嬧€濆湪浣跨敤锛?
     * 1. 鏀粯瀹濓細鏀粯瀹濆叕閽?
     */
    private String publicKey;

}
