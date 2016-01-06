package services;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import utils.Constants;
import utils.Numbers;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * 动态token服务
 * Created by ShenTeng on 2015/1/4.
 */
@Named
@Singleton
public class DynamicTokenService {
    private static final Logger.ALogger LOGGER = Logger.of(DynamicTokenService.class);

    public String getDynamicTokenFromHeader(String tokenHeader) {
        if (null == tokenHeader || tokenHeader.length() < Constants.DYNAMIC_TOKEN_LENGTH) {
            return null;
        }
        return tokenHeader.substring(0, Constants.DYNAMIC_TOKEN_LENGTH);
    }

    public boolean checkDynamicToken(String baseToken, String tokenHeader) {
        String debugInfo = "check dynamic token. baseToken=" + baseToken + " ,tokenHeader=" + tokenHeader;


        if (StringUtils.isBlank(baseToken) || StringUtils.isBlank(tokenHeader)) {
            LOGGER.debug(debugInfo + " ,result: false ,reason: blank param");
            return false;
        }

        if (tokenHeader.length() <= Constants.DYNAMIC_TOKEN_LENGTH) {
            LOGGER.debug(debugInfo + " ,result: false ,reason: illegal tokenHeader length");
            return false;
        }

        String dynamicToken = getDynamicTokenFromHeader(tokenHeader);
        String randomNumStr = tokenHeader.substring(Constants.DYNAMIC_TOKEN_LENGTH);

        Long randomNum = Numbers.parseLong(randomNumStr, (Long) null);
        if (null == randomNum) {
            LOGGER.debug(debugInfo + " ,randomNumStr=" + randomNumStr + " ,result: false ,reason: parse randomNum error");
            return false;
        }

        String newToken = getNewToken(baseToken, randomNum);

        if (null == newToken) {
            LOGGER.debug(debugInfo + " ,randomNum=" + randomNum + " ,result: false,reason: getNewToken error");
            return false;
        } else {
            LOGGER.debug(debugInfo + " ,result: true");
            return newToken.equals(dynamicToken);
        }
    }

    private String getNewToken(String oldToken, long random) {
        String str = changeToken(oldToken);
        long l = random & 0xFFFFFFFF;
        int factor = 0;
        if (isOdd(random)) {
            factor = (int) (l ^ 0xFDB99BDF);
            return DigestUtils.md5Hex(str + Integer.toBinaryString(factor));
        } else {
            factor = (int) (l ^ 0x9BDFFDB9);
            return DigestUtils.md5Hex(Integer.toBinaryString(factor) + str);
        }
    }

    private String changeToken(String token) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 26; i--) {
            sb.append(token.charAt(i));
        }
        for (int i = 15; i >= 9; i--) {
            sb.append(token.charAt(i));
        }
        for (int i = 0; i <= 8; i++) {
            sb.append(token.charAt(i));
        }
        for (int i = 16; i <= 25; i++) {
            sb.append(token.charAt(i));
        }
        return sb.toString();
    }

    private boolean isOdd(long current) {
        return (current & 0x1L) == 1;
    }

}
