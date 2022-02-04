package com.study.demorestapi.configs;

import com.study.demorestapi.accounts.Account;
import com.study.demorestapi.accounts.AccountRole;
import com.study.demorestapi.accounts.AccountService;
import com.study.demorestapi.common.BaseControllerTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    private AccountService accountService;

    @DisplayName("인증 토큰 발급 테스트")
    @Test
    void getAuthToken() throws Exception {
        // Given
        String username = "b@b.com";
        String password = "1234";

        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.USER, AccountRole.ADMIN))
                .build();

        accountService.saveAccount(account);

        String clientId = "myApp";
        String clientSecret = "pass";

        mockMvc.perform(post("/oauth/token")
        .with(httpBasic(clientId, clientSecret))
        .param("username", username)
        .param("password", password)
        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }
}