package services;

import play.cache.Cache;
import utils.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * 登陆token服务。登陆token可用于app端的自动登陆
 * Created by ShenTeng on 2015/1/5.
 */
@Named
@Singleton
public class LoginTokenService {

    @Inject
    private ApplicationService userService;

    /**
     * 创建登陆token
     *
     * @param userId 用户Id
     * @return 登陆token字符串
     */
    public String createLoginToken(Long userId, String pwd) {
        String loginToken = UUID.randomUUID().toString().replace("-", "");
        Cache.set(Constants.CACHE_NAMESPACE_LOGIN_TOKEN + userId + "." + loginToken, pwd, Constants.LOGIN_TOKEN_EXPIRE_IN);
        return loginToken;
    }

    /**
     * 根据登陆token，换取密码
     *
     * @param userId 用户Id
     * @return 登陆token不存在或过期时，返回null
     */
    public String getPwdByLoginToken(Long userId, String loginToken) {
        return (String) Cache.get(Constants.CACHE_NAMESPACE_LOGIN_TOKEN + userId + "." + loginToken);
    }

    public void removeLoginToken(Long userId, String loginToken) {
        Cache.remove(Constants.CACHE_NAMESPACE_LOGIN_TOKEN + userId + "." + loginToken);
    }

}
