package com.bd.ssi.common.api;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * API관련 설정
 */
@Configuration
@PropertySource("classpath:/error_message.properties")
public class ApiConfiguration {

//    @Bean
//    public CharacterEncodingFilter characterEncodingFilter(){
//        return new CharacterEncodingFilter("UTF-8", true);
//    }

}
