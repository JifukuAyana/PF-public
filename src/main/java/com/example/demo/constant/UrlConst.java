package com.example.demo.constant;

/**
 * URL 定数クラス
 *
 * @author ayyyy
 */
public class UrlConst {

    /** ログイン画面 */
    public static final String LOGIN = "/login";

    /** ユーザー登録画面 */
    public static final String SIGNUP = "/signup";

    /** メニュー画面 */
    public static final String DASH = "/dash";

    /** 認証不要画面 */
    public static final String[] NO_AUTHENTICATION = {LOGIN, SIGNUP,"/webjars/**"};

}
