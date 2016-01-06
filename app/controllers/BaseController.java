package controllers;

import models.AdBanner;
import play.mvc.Controller;
import play.mvc.Http;
import utils.Constants;

import java.util.Iterator;

public class BaseController extends Controller{
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
    public void discardCookies(){
        Iterator<Http.Cookie> cookies = response().cookies().iterator();
        while(cookies.hasNext()){
            response().discardCookie(cookies.next().name());
        }
    }
}
