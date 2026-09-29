package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.constant.MessageConst;
import com.example.demo.constant.UrlConst;
import com.example.demo.form.LoginForm;
import com.example.demo.service.LoginService;
import com.example.demo.util.AppUtil;

import lombok.RequiredArgsConstructor;

/**
 * ログイン画面 Controller
 * 
 * @author ayyyy
 */

@Controller // コントローラーであることを定義
@RequiredArgsConstructor
public class LoginController {
	
	//ログイン画面　service
    private final LoginService service;

    //PasswordEncoder
    private final PasswordEncoder passwordEncoder;
    
    /** メッセージソース */
    private final MessageSource messageSource;
    
    /** セッション情報 */
    private final HttpSession session;

    
    /**
     * 初期表示
     * 
     * @param model モデル
     * @param form 入力情報
     * @return 表示画面
     */
    
    @GetMapping(UrlConst.LOGIN) // loginにアクセスがあったときにloginという名前のページを表示する。
    public String view(Model model, LoginForm form) { // (modelクラスにあるデータをformクラスに入れる)
        return "login";
    }
    
    /**
     * ログインエラー画面表示
     *
     * @param model モデル
     * @param form 入力情報
     * @return 表示画面
     */
//    @GetMapping(value = UrlConst.LOGIN, params = "error")
//    public String viewWithError(Model model, LoginForm form) {
//    	var errorInfo = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//    	model.addAttribute("errorMsg", errorInfo.getMessage());
//    	return "login";
//
//    }
    
//  案１
	  @GetMapping(value = UrlConst.LOGIN, params = "error")
	  public String viewWithError(Model model, LoginForm form) {
	      var errorInfo = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	
	      String errorMsg;
	
	      if (errorInfo != null && errorInfo.getMessage() != null &&
	          errorInfo.getMessage().equals(MessageConst.LOGIN_DELETED_USER)) {
	          // login.deleted.user の場合は削除済みユーザーとして処理
	          errorMsg = AppUtil.getMessage(messageSource, MessageConst.LOGIN_DELETED_USER);
	      } else {
	          // 一般エラー
	          errorMsg = AppUtil.getMessage(messageSource, MessageConst.LOGIN_WRONG_INPUT);
	      }
	
	      model.addAttribute("errorMsg", errorMsg);
	      return "login";
	  }

  
//  案２
//  @GetMapping(value = UrlConst.LOGIN, params = "error")
//  public String viewWithError(Model model, LoginForm form) {
//      Exception errorInfo = (Exception) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//
//      String errorMsg;
//
//      // DisabledException が直接 or cause として入っているケースを両方見る
//      boolean deleted = errorInfo instanceof DisabledException
//          || (errorInfo instanceof InternalAuthenticationServiceException
//               && errorInfo.getCause() instanceof DisabledException);
//
//      if (deleted) {
//          errorMsg = AppUtil.getMessage(messageSource, MessageConst.LOGIN_DELETED_USER);
//      } else {
//          errorMsg = AppUtil.getMessage(messageSource, MessageConst.LOGIN_WRONG_INPUT);
//      }
//
//      model.addAttribute("errorMsg", errorMsg);
//      return "login";
//  }
    
    
    
    /**
     * ログイン
     * 
     * @param model モデル
     * @param form 入力情報
     * @return 表示画面
     */

    @PostMapping(UrlConst.LOGIN) // formから受け取った値（post送信された値：PWなど）でログイン可否を判断する。
    public String login(Model model, LoginForm form) {
    	var userInfo = service.searchUserById(form.getLoginId());
    	var isCorrectUserAuth = userInfo.isPresent()
    				//(入力されたPW,ハッシュ化されたPW)
    			&& passwordEncoder.matches(form.getPassword(), userInfo.get().getPassword());
        if (isCorrectUserAuth) {
            return "redirect:/dash";
        } else {
        	var errorMsg = AppUtil.getMessage(messageSource, MessageConst.LOGIN_WRONG_INPUT);
            model.addAttribute("errorMsg", errorMsg);
            return "login";
        }
    }
}
