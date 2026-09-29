package com.example.demo.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ユーザー権限種別
 * 
 * @author ayyyy
 */
@Getter
@AllArgsConstructor
public enum AuthorityKind {
    
    /* 管理者の閲覧が可能 */
	ITEM_AND_USER_MANAGER("1"),

    /* 一般ユーザーの閲覧、更新が可能 */
    ITEM_MANAGER("2");

    private String authorityKind;
}
