package com.study.demorestapi.configs;

import com.study.demorestapi.accounts.Account;
import com.study.demorestapi.accounts.AccountRole;
import com.study.demorestapi.accounts.AccountService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ApplicationRunner applicationRunner(AccountService accountService) {
        return args -> {
            Account account = Account.builder()
                    .email("a@a.com")
                    .password("1234")
                    .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                    .build();

            accountService.saveAccount(account);
        };
    }
}
