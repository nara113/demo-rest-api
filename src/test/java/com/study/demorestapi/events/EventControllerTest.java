package com.study.demorestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.demorestapi.common.RestDocsConfiguration;
import com.study.demorestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
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
            .andExpect(jsonPath("free").value(false))
            .andExpect(jsonPath("offline").value(Matchers.not(false)))
            .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        .andExpect(jsonPath("_links.self").exists())
        .andExpect(jsonPath("_links.query-events").exists())
        .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("event neme"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("enrollment begin time"),
                                fieldWithPath("closeEnrollmentDateTime").description("enrollment close time"),
                                fieldWithPath("beginEventDateTime").description("event begin time"),
                                fieldWithPath("endEventDateTime").description("event end time"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("id of event"),
                                fieldWithPath("name").description("event neme"),
                                fieldWithPath("description").description("description of event"),
                                fieldWithPath("beginEnrollmentDateTime").description("enrollment begin time"),
                                fieldWithPath("closeEnrollmentDateTime").description("enrollment close time"),
                                fieldWithPath("beginEventDateTime").description("event begin time"),
                                fieldWithPath("endEventDateTime").description("event end time"),
                                fieldWithPath("location").description("location of event"),
                                fieldWithPath("basePrice").description("base price of event"),
                                fieldWithPath("maxPrice").description("max price of event"),
                                fieldWithPath("limitOfEnrollment").description("limit of enrollment"),
                                fieldWithPath("free").description("free or not"),
                                fieldWithPath("offline").description("offline or not"),
                                fieldWithPath("eventStatus").description("status of event"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query events"),
                                fieldWithPath("_links.update-event.href").description("link to update event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ));
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
                .closeEnrollmentDateTime(LocalDateTime.now().plusDays(1))
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
            .andExpect(jsonPath("errors[0].objectName").exists())
            .andExpect(jsonPath("errors[0].code").exists())
            .andExpect(jsonPath("errors[0].defaultMessage").exists())
            .andExpect(jsonPath("_links.index").exists());
    }
}
