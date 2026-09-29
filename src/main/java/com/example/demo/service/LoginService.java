package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.UserInfo;
import com.example.demo.repository.UserInfoRepository;

import lombok.RequiredArgsConstructor;

/**
 * ログイン画面 service
 * 
 * @author ayyyy
 */


@Service
@RequiredArgsConstructor  //private finalで宣言したインスタンスに対してnewしたものを注入するコンストラクタを実装してくれる。  
public class LoginService {
	
	//ユーザー情報テーブルレポジトリ
	private final UserInfoRepository repository;

	/**
	 * ユーザ情報テーブル主キー検索
	 * @param loginId ログインID
	 * @return ユーザ情報テーブルを主キー検索した結果（１件）
	 */
	
	// ユーザーIDでユーザーを検索
	public Optional<UserInfo> searchUserById(String loginId) {
		return repository.findById(loginId);
	}
}
