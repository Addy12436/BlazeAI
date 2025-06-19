package com.ai.repo;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.ai.entities.UserUsage;

public interface UserUsageRepository extends JpaRepository<UserUsage, Long> {
   Optional<UserUsage> findByEmailAndDate(String email, LocalDate date);
Optional<UserUsage> findGenerationCountByEmailAndDate(String email, LocalDate date);

}
