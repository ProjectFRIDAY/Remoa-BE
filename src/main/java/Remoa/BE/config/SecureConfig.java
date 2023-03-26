package Remoa.BE.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecureConfig {



    /**
     * Spring Security에서 사용할 password encoder로 BCryptPasswordEncoder 지정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 설정
     */
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {


        http.formLogin().disable();

        http
                .authorizeRequests()
                //api 명세 확정 후 재확인 핋요

                .antMatchers(HttpMethod.POST,"/notice").hasAnyRole( "ADMIN")
                .antMatchers("/**").permitAll()
                .anyRequest().authenticated()
                .and()

                .sessionManagement()
                .maximumSessions(-1) //세션 제한 없음
                .maxSessionsPreventsLogin(false); //중복 접속시 마지막 세션만 유지

        http.csrf().disable();

        return http.build();
    }

}
