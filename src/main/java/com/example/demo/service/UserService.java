package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.UserInfo;
import com.example.demo.entity.UserLikeHistory;
import com.example.demo.form.UserEditForm;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.repository.UserLikeHistoryRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserLikeHistoryRepository userLikeHistoryRepository;

    // 削除済みユーザーを取得（削除フラグが `true` のデータ）
    public List<UserInfo> getDeletedUsers() {
        return userInfoRepository.findByDeletedTrue();
    }

    // 削除されていないユーザーを取得
    public List<UserInfo> getActiveUsers() {
        return userInfoRepository.findByDeletedFalseOrderByLikesDesc();
    }

    // ステータスを1⇄2で切り替え
    @Transactional
    public void toggleUserAccess(String loginId) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.setAccess(user.getAccess() == 1 ? 2 : 1);
            userInfoRepository.save(user);
        } else {
            throw new RuntimeException("ユーザーが見つかりません");
        }
    }

    // ✅ ステータス切替（フロントのfetchに対応）
    public boolean toggleAccessStatus(String loginId) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.setAccess(user.getAccess() == 1 ? 2 : 1);
            userInfoRepository.save(user);
            return true;
        }
        return false;
    }

    // 🔹 論理削除（deleted = true にする）
    @Transactional
    public void deleteUser(String loginId) {
        userInfoRepository.findById(loginId).ifPresent(user -> {
            user.setDeleted(true);
            userInfoRepository.save(user);
        });
    }

    // ✅ 削除（fetch用）
    public boolean logicalDelete(String loginId) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
        if (userOpt.isPresent()) {
            UserInfo user = userOpt.get();
            user.setDeleted(true);
            userInfoRepository.save(user);
            return true;
        }
        return false;
    }

	// ユーザー復元処理
	@Transactional
	public void restoreUser(String loginId) {
	    Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
	    if (userOpt.isPresent()) {
	        UserInfo user = userOpt.get();
	        user.setDeleted(false);  // 復元
	        userInfoRepository.save(user);
	    } else {
	        throw new RuntimeException("ユーザーが見つかりません");
	    }
	}
	
    // 【完全削除】DBから物理削除
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean permanentlyDeleteUser(String loginId) {
	    Optional<UserInfo> userOpt = userInfoRepository.findById(loginId);
	    if (userOpt.isEmpty()) {
	        return false;
	    }

	    UserInfo user = userOpt.get();

	    // ✅ プロフィール画像ファイル削除処理（default.jpgは除外）
	    String imageName = user.getProfileImage();
	    if (imageName != null && !imageName.equals("default.jpg")) {
	        Path imagePath = Paths.get("upload-dir/profile-images/" + imageName);
	        try {
	            Files.deleteIfExists(imagePath);
	        } catch (IOException e) {
	            e.printStackTrace(); // エラーはログに出す（必要に応じてLoggerに変更可）
	        }
	    }

	 // ✅ いいね履歴の削除（外部キー制約に対応）
	    userLikeHistoryRepository.deleteByLoginId(loginId);

	    // DBから物理削除
	    userInfoRepository.delete(user);
	    userInfoRepository.flush();
	    return true;
	}

	
	//いいね機能
	@Transactional
	public int incrementLikes(String loginId) {
	    UserInfo user = userInfoRepository.findById(loginId)
	        .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));

	    user.setLikes(user.getLikes() + 1);
	    userInfoRepository.save(user);

	    // 🔸 いいね履歴を保存
	    UserLikeHistory history = new UserLikeHistory();
	    history.setLoginId(loginId);
	    history.setLikedAt(LocalDateTime.now());
	    userLikeHistoryRepository.save(history);

	    return user.getLikes();
	}
	
	
    // 🔹 月間ランキング
	public List<String> getMonthlyRankingLoginIds() {
	    LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
	    LocalDateTime now = LocalDateTime.now();
	    return userLikeHistoryRepository.findLikeRanking(oneMonthAgo, now)
	            .stream()
	            .map(obj -> (String) obj[0])
	            .limit(5)
	            .toList();
	}

    // 🔹 年間ランキング 
	public List<String> getYearlyRankingLoginIds() {
	    LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
	    LocalDateTime now = LocalDateTime.now();
	    return userLikeHistoryRepository.findLikeRanking(oneYearAgo, now)
	            .stream()
	            .map(obj -> (String) obj[0])
	            .limit(5)
	            .toList();
	}
	
	// ✅ エラー修正：正しいリポジトリ名を使用
	public UserInfo getUserById(String loginId) {
	    return userInfoRepository.findById(loginId).orElse(null);
	}
	
	@Transactional
	public void updateUserInfo(String loginId, String mail, int access, String authority,
            String furigana, Integer gender, Integer age, String introduction,
            MultipartFile profileImage) {
		UserInfo user = userInfoRepository.findById(loginId).orElse(null);
		if (user == null) return;
		
		user.setMail(mail);
		user.setAccess(access);
		user.setAuthority(authority);
		user.setFurigana(furigana);
		user.setGender(gender != null ? gender : 1);
		user.setAge(age);
		user.setIntroduction(introduction);
		
		if (profileImage != null && !profileImage.isEmpty()) {
		try {
		String originalFileName = profileImage.getOriginalFilename();
		String ext = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
		String newFileName = loginId + ext;
		
		String uploadDir = "upload-dir/profile-images";
		Path uploadPath = Paths.get(uploadDir);
		if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
		
		Files.copy(profileImage.getInputStream(), uploadPath.resolve(newFileName),
		     StandardCopyOption.REPLACE_EXISTING);
		
		user.setProfileImage(newFileName);
		} catch (IOException e) {
		e.printStackTrace(); // ログなどで補足
		}
		}
		
		userInfoRepository.save(user);
		}

	// アクセス許可も禁止も公開サイトに表示
	public List<UserInfo> getAllUsers() {
	    return userInfoRepository.findAll(); 
	}

	@Transactional
	public void updateUserInfoWithForm(UserEditForm form) {
	    String oldId = form.getOriginalLoginId();
	    UserInfo oldUser = userInfoRepository.findById(oldId).orElse(null);
	    if (oldUser == null) return;

	    boolean isIdChanged = !oldId.equals(form.getLoginId());

	    if (isIdChanged && userInfoRepository.existsById(form.getLoginId())) {
	        throw new RuntimeException("既に使用されているログインIDです。");
	    }

	    // 新しいユーザー情報を用意
	    UserInfo newUser = new UserInfo();
	    newUser.setLoginId(form.getLoginId());
	    newUser.setMail(form.getMail());
	    newUser.setAccess(form.getAccess());
	    newUser.setAuthority(form.getAuthority());
	    newUser.setFurigana(form.getFurigana());
	    newUser.setGender(form.getGender());
	    newUser.setAge(form.getAge());
	    newUser.setIntroduction(form.getIntroduction());
	    newUser.setDeleted(false);
	    newUser.setLikes(oldUser.getLikes());

	    if (form.getPassword() != null && !form.getPassword().isEmpty()) {
	        newUser.setPassword(new BCryptPasswordEncoder().encode(form.getPassword()));
	    } else {
	        newUser.setPassword(oldUser.getPassword());
	    }

	    MultipartFile profileImage = form.getProfileImage();
	    if (profileImage != null && !profileImage.isEmpty()) {
	        try {
	            String ext = profileImage.getOriginalFilename()
	                    .substring(profileImage.getOriginalFilename().lastIndexOf(".")).toLowerCase();
	            String newFileName = form.getLoginId() + ext;
	            Path uploadDir = Paths.get("upload-dir/profile-images");
	            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
	            Files.copy(profileImage.getInputStream(), uploadDir.resolve(newFileName), StandardCopyOption.REPLACE_EXISTING);
	            newUser.setProfileImage(newFileName);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    } else {
	        newUser.setProfileImage(oldUser.getProfileImage());
	    }

	    // ① 新しいユーザーを先に保存
	    userInfoRepository.save(newUser);

	    // ② 外部キー参照テーブルを更新
	    if (isIdChanged) {
	        userLikeHistoryRepository.updateLoginId(oldId, form.getLoginId());

	        // ③ 最後に古いユーザーを削除
	        userInfoRepository.deleteById(oldId);
	    }
	}

}