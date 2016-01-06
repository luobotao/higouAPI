package utils;

import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;

import java.util.UUID;

/**
 * 管理系统登录Cache
 * Created by ShenTeng on 2015/1/27.
 */
public class AdminLoginCache {

    public static Long getByTokenAndRefresh(String token) {
        if (null == token) {
            return null;
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }

        Long adminId = (Long) Cache.get(Constants.CACHE_NAMESPACE_ADMINSESSION_USER + token);
        if (null == adminId) {
            return null;
        }

        Cache.set(Constants.CACHE_NAMESPACE_ADMINSESSION_USER + token, adminId, Constants.ADMINSESSION_TOKEN_EXPIRE_IN);

        return adminId;
    }

    public static Long getByToken(String token) {
        if (null == token) {
            return null;
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }

        Long adminId = (Long) Cache.get(Constants.CACHE_NAMESPACE_ADMINSESSION_USER + token);

        return adminId;
    }

    public static boolean isLoginByToken(String token) {
        return null != getByToken(token);
    }

    public static String save(Long adminId) {
        String token = UUID.randomUUID().toString().replace("-", "");

        Cache.set(Constants.CACHE_NAMESPACE_ADMINSESSION_USER + token, adminId, Constants.ADMINSESSION_TOKEN_EXPIRE_IN);

        return token;
    }

    public static void removeByToken(String token) {
        if (StringUtils.isNotBlank(token)) {
            Cache.remove(Constants.CACHE_NAMESPACE_ADMINSESSION_USER + token);
        }
    }

}
