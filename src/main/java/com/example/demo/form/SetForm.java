package com.example.demo.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class SetForm {

	 /** ログインID */
	@NotBlank(message = "{error.loginId.required}")
	@Size(max = 255, message = "{error.loginId.length}")
	private String loginId;

    /** メールアドレス */
	@NotBlank(message = "{error.mail.required}")
	@Size(max = 255, message = "{error.mail.length}")
	@Email(message = "{error.mail.format}")
	private String mail;

	/** パスワード */
    private String password;

    @AssertTrue(message = "{error.password.length}")
    public boolean isPasswordLengthValid() {
        return password == null || password.isEmpty() || (password.length() >= 8 && password.length() <= 32);
    }

    @AssertTrue(message = "{error.password.format}")
    public boolean isPasswordFormatValid() {
        return password == null || password.isEmpty() || password.matches("^[a-z0-9_-]*$");
    }

}