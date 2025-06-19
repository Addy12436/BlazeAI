package com.ai.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ai.entities.User;
import com.ai.secserviceImpl.UserDetailsImpl;
import com.ai.service.UsageLimitService;

@Controller
public class Main {

     @Autowired
    private UsageLimitService usageLimitService;

    @GetMapping("/")
public String index(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = "Guest";
    String profilePictureUrl = "/images/profile.png"; // Default image
    int dailyMessageLimit = 0;
    int messagesUsed = 0;
    boolean isPremium = false;

    if (authentication != null && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getPrincipal())) {

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            User user = userDetails.getUser();
            username = user.getName();

            profilePictureUrl = user.getProfilePic();
            if (profilePictureUrl == null || profilePictureUrl.isEmpty()) {
                profilePictureUrl = "/images/profile.png";
            }

            String loggedInUserEmail = user.getEmail();

            if (user.isPro()) {
                dailyMessageLimit = Integer.MAX_VALUE;
                isPremium = true;
            } else {
                var usageOptional = usageLimitService.findGenerationCountByEmailAndDate(loggedInUserEmail, LocalDate.now());
                if (usageOptional.isPresent()) {
                    messagesUsed = usageOptional.get().getGenerationCount();
                }
                dailyMessageLimit = 10;
            }

        } else if (principal instanceof OAuth2User oauthUser) {
            username = oauthUser.getAttribute("name");
            profilePictureUrl = oauthUser.getAttribute("picture");
            if (profilePictureUrl == null || profilePictureUrl.isEmpty()) {
                profilePictureUrl = "/images/profile.png";
            }

            String email = oauthUser.getAttribute("email");
            var usageOptional = usageLimitService.findGenerationCountByEmailAndDate(email, LocalDate.now());
            if (usageOptional.isPresent()) {
                messagesUsed = usageOptional.get().getGenerationCount();
            }
            dailyMessageLimit = 10;
            // You can mark isPremium true if your DB has info for this Google user.
        }
    }

    model.addAttribute("userName", username);
    model.addAttribute("profilePictureUrl", profilePictureUrl);
    model.addAttribute("username", username);
    model.addAttribute("dailyMessageLimit", dailyMessageLimit);
    model.addAttribute("messagesUsed", messagesUsed);
    model.addAttribute("totalMessages", dailyMessageLimit);
    model.addAttribute("premiumCredits", isPremium ? 100 : 0); // Just a sample, change logic as needed
    model.addAttribute("isPremium", isPremium);

    return "index";
}

}
