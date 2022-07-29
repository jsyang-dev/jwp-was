package controller;

import db.DataBase;
import model.User;
import webserver.http.HttpRequest;
import webserver.http.HttpResponse;

public class UserLoginController extends Controller {
    @Override
    public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) {
        final User user = DataBase.findUserById(httpRequest.getAttribute("userId"));
        if (isLogin(user, httpRequest.getAttribute("password"))) {
            setLoginCookie(httpResponse, false);
            httpResponse.redirect("/user/login_failed.html");
            return;
        }

        setLoginCookie(httpResponse, true);
        httpResponse.redirect("/index.html");
    }

    private boolean isLogin(User user, String password) {
        return user == null || !user.isLogin(password);
    }

    private void setLoginCookie(HttpResponse httpResponse, boolean login) {
        httpResponse.addHeader("Set-Cookie", "logined=" + login + "; Path=/");
    }
}