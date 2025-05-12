package com.medicalmanager.medical.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final UserDetailsService uds;
  private final PasswordEncoder    pw;

  public SecurityConfig(UserDetailsService uds, PasswordEncoder pw) {
    this.uds = uds;
    this.pw  = pw;
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(pw);
    return p;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         DaoAuthenticationProvider authProvider)
                                         throws Exception {
    http
      .authenticationProvider(authProvider)    // <-- register it here
      .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
      .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
      .authorizeHttpRequests(a -> a
         .requestMatchers("/h2-console/**").permitAll()
         .requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**").permitAll()
         .requestMatchers("/admin/**").hasRole("ADMIN")
         .requestMatchers("/doctor/**").hasRole("DOCTOR")
         .requestMatchers("/patient/**").hasRole("PATIENT")
         .anyRequest().authenticated()
      )
      .formLogin(f -> f
         .loginPage("/login")
         .defaultSuccessUrl("/dashboard", true)
         .permitAll()
      )
      .logout(l -> l
         .logoutSuccessUrl("/login?logout")
         .permitAll()
      );
    return http.build();
  }

  @Bean
  public FilterRegistrationBean<HiddenHttpMethodFilter> hiddenHttpMethodFilter(){
      FilterRegistrationBean<HiddenHttpMethodFilter> bean = 
          new FilterRegistrationBean<>(new HiddenHttpMethodFilter());
      bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
      return bean;
  }
}
