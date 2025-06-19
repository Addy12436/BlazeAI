package com.ai.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


import org.springframework.stereotype.Service;
import com.ai.entities.User;
import com.ai.entities.UserUsage;
import com.ai.repo.Repo;
import com.ai.repo.UserUsageRepository;


@Service
public class UsageLimitService {

    private final UserUsageRepository userUsageRepository;
    private final Repo userRepository;  // Repository to fetch user details

    public UsageLimitService(UserUsageRepository userUsageRepository, Repo userRepository) {
        this.userUsageRepository = userUsageRepository;
        this.userRepository = userRepository;
    }

    public boolean canGenerateWebsite(String userId) {
        // Fetch user details to check if the user is Pro
        User user = userRepository.findByEmail(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // If the user is Pro, there's no limit
        if (user.isPro()) {
            return true;
        }

        // Proceed with the limit check for non-Pro users
        LocalDate today = LocalDate.now();
        UserUsage usage = userUsageRepository.findByEmailAndDate(userId, today)
                .orElseGet(() -> {
                    UserUsage newUsage = new UserUsage();
                    newUsage.setEmail(userId);
                    newUsage.setDate(today);
                    newUsage.setGenerationCount(0);
                    return newUsage;
                });

    if (usage.getGenerationCount() >= 10) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", false);
    result.put("message", "⚠️ You’ve reached your daily limit of 10 generations.");
    return  false;
}


        // Increment the generation count and save the usage record
        usage.setGenerationCount(usage.getGenerationCount() + 1);
        userUsageRepository.save(usage);

        return true;
    }

    public Optional<UserUsage> findGenerationCountByEmailAndDate(String email, LocalDate date) {
        return userUsageRepository.findGenerationCountByEmailAndDate(email, date);
    }


   
}

