package com.ai.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.ai.secserviceImpl.SecurityCustomerUserDetailService;

@Configuration
public class SecurityConfig {

    
    @Autowired
    private SecurityCustomerUserDetailService userDetailService;

  //  @Autowired
   // private OAuthAuthenicationSuccessHandler handler;

    @Autowired
    private AuthenticationFailureHandler authFailtureHandler;

    // Configuration of authentication provider for Spring Security
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        // User detail service object:
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        // Password encoder object
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        httpSecurity.authorizeRequests(authorize -> {
            authorize.requestMatchers("/login", "/signup", "/services").permitAll(); // Public pages
            authorize.anyRequest().authenticated(); // All other requests require authentication
        })
                .oauth2Login(oauth -> {
                    oauth.loginPage("/login")
                            .defaultSuccessUrl("/", true);// Redirect after successful login
                           // .successHandler(handler); // Custom success handler (optional)
                });

        // Form default login configuration
        httpSecurity.formLogin(formLogin -> {
            formLogin.loginPage("/login");
            formLogin.loginProcessingUrl("/authenticate"); // This is the URL Spring Security handles for login
            formLogin.defaultSuccessUrl("/");
            formLogin.usernameParameter("email");
            formLogin.passwordParameter("password");
            formLogin.failureHandler(authFailtureHandler);
        });

        // OAuth configurations
        httpSecurity.oauth2Login(oauth -> {
            oauth.loginPage("/login");
            oauth.defaultSuccessUrl("/");
            oauth.failureUrl("/login?error=true");
        
        });

        httpSecurity.logout(logoutForm -> {
            logoutForm.logoutUrl("/do-logout");
            logoutForm.logoutSuccessUrl("/login?logout=true");
        });

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
