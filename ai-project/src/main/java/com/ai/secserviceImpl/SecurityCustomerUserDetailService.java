package com.ai.secserviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ai.entities.User;
import com.ai.repo.Repo;


@Service

public class SecurityCustomerUserDetailService   implements UserDetailsService {

    @Autowired
    private  Repo repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws  UsernameNotFoundException {
        User user = repo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return new UserDetailsImpl(user); // THIS IS THE CRUCIAL PART
    }

}
