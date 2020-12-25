package com.bd.ssi.common.security;

import com.bd.ssi.auth.JWTUtil;
import com.bd.ssi.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

/**
 * 스프링 시큐리티 설정 클래스
 */
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SsiSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private SsiUserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;


//    @Bean
//    public PasswordEncoder passwordEncoder(){
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }

    @SuppressWarnings("deprecation")
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
//        super.configure(auth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 유지 안 함
                .and()
                .addFilter(new JwtAuthenticationFilter(authenticationManager(), userRepository, jwtUtil))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), userRepository, jwtUtil))
                .authorizeRequests()
                    .antMatchers(HttpMethod.POST, "/login").permitAll()
                    .antMatchers(HttpMethod.POST, "/pre_make_login_vcs").permitAll()
                    .antMatchers(HttpMethod.GET, "/pre_make_login_vcs").permitAll()
                    .antMatchers(HttpMethod.GET, "/product/image").permitAll()
                    .antMatchers(HttpMethod.GET, "/test_pages/page1").permitAll()
                    .antMatchers(HttpMethod.GET, "/deal/sellList").permitAll()
                    .antMatchers(HttpMethod.GET, "/common/product_init").permitAll()
                    .antMatchers(HttpMethod.GET, "/common/hello").permitAll()
                    .antMatchers(HttpMethod.GET, "/fcmcall").permitAll()
                    .antMatchers(HttpMethod.GET, "/fcmcallD").permitAll()
//                    .antMatchers("/**").hasAuthority("SELLER")
                    .anyRequest().authenticated();

    }

    //    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .authorizeRequests().antMatchers("/auth/authenticate").permitAll()
//                .anyRequest().hasAnyAuthority("USER", "SELLER")
//                .and()
//                .oauth2ResourceServer()
//                .jwt();
////                .and()
////                    .defaultSuccessUrl("/product/list", true)
////                .and()
////                .logout()
////                    .deleteCookies("JSESSIONID")
////                ;
////                .and()
//
//    }
}
