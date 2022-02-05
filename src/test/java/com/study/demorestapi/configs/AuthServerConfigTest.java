package com.study.demorestapi.configs;

import com.study.demorestapi.common.AppProperties;
import com.study.demorestapi.common.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    private AppProperties properties;

    @DisplayName("인증 토큰 발급 테스트")
    @Test
    void getAuthToken() throws Exception {
        mockMvc.perform(post("/oauth/token")
        .with(httpBasic(properties.getClientId(), properties.getClientSecret()))
        .param("username", properties.getUserUsername())
        .param("password", properties.getUserPassword())
        .param("grant_type", "password"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }
}