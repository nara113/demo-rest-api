package com.study.demorestapi.accounts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void findByUsername() {
        // Given
        final String username = "a@a.com";
        final String password = "1234";

        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        accountService.saveAccount(account);

        // When
        final UserDetails userDetails = accountService.loadUserByUsername(username);

        // Then
        assertThat(passwordEncoder.matches(password, userDetails.getPassword())).isTrue();
    }

    @Test
    void findByUsernameFail_1() {
        final UsernameNotFoundException exception = Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            accountService.loadUserByUsername("abc");
        });

        assertThat(exception.getMessage()).contains("abc");
    }

    @Test
    void findByUsernameFail_2() {
        try {
            accountService.loadUserByUsername("abc");
            fail("fail");
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).contains("abc");
        }
    }
}