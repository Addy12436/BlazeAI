package com.ai.repo;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.entities.User;



@Repository
public interface Repo extends JpaRepository<User, String> {
    // Custom query methods can be defined here if needed
    Optional<User> findByEmail(String userId);  // ✅ return Optional



//Optional<User> findByEmailAndPassword(String email, String password);

//Optional<User> findByEmailToken(String id);
}
