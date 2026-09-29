package com.example.demo.form;

import lombok.Data;

/**
 * ログイン画面 for
 * 
 * @author ayyyy
 */

@Data  //getterとsetterを用意してくれる
public class LoginForm {
	
	//ログインID
	private String loginId;
	
	//パスワード
	private String password;
}