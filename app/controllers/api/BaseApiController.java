package controllers.api;

import play.mvc.Controller;
import utils.Constants;

public class BaseApiController extends Controller {
    public static final String PARAM = "param";
    public <T> T getParam(Class<T> cla) {
        return (T)ctx().args.get(PARAM);
    }
    public String getToken() {
        return session().get(Constants.SESSION_TOKEN);
    }
    public String setToken(String token) {
        return session().put(Constants.SESSION_TOKEN, token);
    }
}
