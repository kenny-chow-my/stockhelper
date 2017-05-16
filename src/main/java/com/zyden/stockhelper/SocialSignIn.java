package com.zyden.stockhelper;

import com.zyden.stockhelper.Util.AuthUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.connect.web.SignInAdapter;

/**
 * Created by Kenny on 5/16/2017.
 */
@Configuration
public class SocialSignIn {
        @Bean
        public SignInAdapter authSignInAdapter() {
            return (userId, connection, request) -> {
                AuthUtil.authenticate(connection);
                return null;
            };
        }



}

