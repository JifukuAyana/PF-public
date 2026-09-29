package com.example.demo.controller;

import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.UserInfo;
import com.example.demo.form.SetForm;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.repository.UserLikeHistoryRepository;

@Controller
@RequestMapping("/set")
public class SetController {

    private final UserInfoRepository userInfoRepository;
    private final UserLikeHistoryRepository userLikeHistoryRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public SetController(UserInfoRepository userInfoRepository,
                         UserLikeHistoryRepository userLikeHistoryRepository,
                         BCryptPasswordEncoder passwordEncoder,
                         UserDetailsService userDetailsService) {
        this.userInfoRepository = userInfoRepository;
        this.userLikeHistoryRepository = userLikeHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @ModelAttribute("hasUserManageAuth")
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof UserInfo userInfo) {
            return "1".equals(userInfo.getAuthority());
        }
        return false;
    }

    @GetMapping
    public String view(Model model) {
        if (!model.containsAttribute("user")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loginId = authentication.getName();

            Optional<UserInfo> user = userInfoRepository.findById(loginId);
            SetForm form = new SetForm();
            user.ifPresent(u -> {
                form.setLoginId(u.getLoginId());
                form.setMail(u.getMail());
            });

            model.addAttribute("user", form);
        }
        return "set";
    }

    @PostMapping("/updateUser")
    @Transactional
    public String updateUser(@Validated @ModelAttribute("user") SetForm form,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", form);
            redirectAttributes.addFlashAttribute("message", "common.formError");
            redirectAttributes.addFlashAttribute("isError", true);
            return "redirect:/set";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oldLoginId = authentication.getName();

        Optional<UserInfo> existingUserOptional = userInfoRepository.findById(oldLoginId);

        if (existingUserOptional.isPresent()) {
            UserInfo existingUser = existingUserOptional.get();
            boolean isLoginIdChanged = !oldLoginId.equals(form.getLoginId());

            if (isLoginIdChanged && userInfoRepository.existsById(form.getLoginId())) {
                redirectAttributes.addFlashAttribute("message", "signup.existedLoginId");
                redirectAttributes.addFlashAttribute("isError", true);
                return "redirect:/set";
            }

            existingUser.setMail(form.getMail());

            if (form.getPassword() != null && !form.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(form.getPassword()));
            }

            if (isLoginIdChanged) {
                UserInfo newUser = new UserInfo();
                newUser.setLoginId(form.getLoginId());
                newUser.setMail(existingUser.getMail());
                newUser.setPassword(existingUser.getPassword());
                newUser.setAuthority(existingUser.getAuthority());
                newUser.setAccess(existingUser.getAccess());
                newUser.setFurigana(existingUser.getFurigana());
                newUser.setGender(existingUser.getGender());
                newUser.setAge(existingUser.getAge());
                newUser.setIntroduction(existingUser.getIntroduction());
                newUser.setProfileImage(existingUser.getProfileImage());
                newUser.setDeleted(false);
                newUser.setLikes(existingUser.getLikes());

                userInfoRepository.save(newUser);

                // ✅ 外部キー制約を回避するため user_like_history の loginId を先に更新
                userLikeHistoryRepository.updateLoginId(oldLoginId, newUser.getLoginId());

                userInfoRepository.delete(existingUser);

                // 認証情報を新しいユーザーIDで更新
                UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getLoginId());
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        authentication.getCredentials(),
                        userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            } else {
                userInfoRepository.save(existingUser);
            }

            redirectAttributes.addFlashAttribute("message", "set.resistSucceed");
            redirectAttributes.addFlashAttribute("isError", false);
            return "redirect:/set";
        }

        redirectAttributes.addFlashAttribute("message", "common.formError");
        redirectAttributes.addFlashAttribute("isError", true);
        return "redirect:/set";
    }
}