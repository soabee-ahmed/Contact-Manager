package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
		
	}
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		 
	}
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	/*
	 * protected void configure (AuthenticationManagerBuilder auth) throws
	 * Exception{ auth.authenticationProvider(authenticationProvider()); }
	 * 
	 * @Bean protected void configure (HttpSecurity http) throws Exception{
	 * http.authorizeRequests().requestMatchers("/admin/**").hasRole("ADMIN")
	 * .requestMatchers("/user/**").hasRole("USER")
	 * .requestMatchers("/**").permitAll().and().formLogin().and().csrf().disable();
	 * }
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http.authorizeRequests().requestMatchers("/admin/**").hasRole("ADMIN")
		.requestMatchers("/user/**").hasRole("USER")
		.requestMatchers("/**").permitAll().and().formLogin().loginPage("/signin").and().csrf().disable();
		http.authenticationProvider(authenticationProvider());
		
		return http.build();
		
	}
	
}
