package com.study.demorestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.demorestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @TestDescription("테스트 성공")
    @Test
    void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("spring")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now().plusDays(1))
                .beginEventDateTime(LocalDateTime.now())
                .endEventDateTime(LocalDateTime.now().plusDays(1))
                .location("korea")
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("id").exists())
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
            .andExpect(jsonPath("id").value(Matchers.not(200)))
            .andExpect(jsonPath("free").value(Matchers.not(true)))
            .andExpect(jsonPath("offline").value(Matchers.not(true)));
    }

    @DisplayName("존재하지 않는 입력값 예외발생")
    @Test
    void createEvent_fail() throws Exception {
        Event event = Event.builder()
                .id(200)
                .name("Spring")
                .description("spring")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now())
                .location("korea")
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .free(true)
                .offline(true)
                .eventStatus(EventStatus.BEGAN_ENROLLMEND)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @DisplayName("입력값이 없어서 검증 에")
    @Test
    void createEvent_empty() throws Exception {
        EventDto event = EventDto.builder()
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("날짜 검증 에러")
    @Test
    void createEvent_wrong() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("spring")
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now().minusDays(1))
                .beginEventDateTime(LocalDateTime.now())
                .endEventDateTime(LocalDateTime.now().plusDays(1))
                .location("korea")
                .basePrice(300)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists());
    }
}
