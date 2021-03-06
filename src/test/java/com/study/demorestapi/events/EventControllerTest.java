package com.study.demorestapi.events;

import com.study.demorestapi.accounts.Account;
import com.study.demorestapi.accounts.AccountRepository;
import com.study.demorestapi.accounts.AccountRole;
import com.study.demorestapi.accounts.AccountService;
import com.study.demorestapi.common.AppProperties;
import com.study.demorestapi.common.BaseControllerTest;
import com.study.demorestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties properties;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @DisplayName("?????? ?????? ??????")
    @Test
    String getAuthToken() throws Exception {
        // Given
        Account account = Account.builder()
                .email(properties.getUserUsername())
                .password(properties.getUserPassword())
                .roles(Set.of(AccountRole.USER, AccountRole.ADMIN))
                .build();

        accountService.saveAccount(account);

        final ResultActions perform = mockMvc.perform(MockMvcRequestBuilders.post("/oauth/token")
                .with(httpBasic(properties.getClientId(), properties.getClientSecret()))
                .param("username", properties.getUserUsername())
                .param("password", properties.getUserPassword())
                .param("grant_type", "password"));
        final String responseBody = perform.andReturn().getResponse().getContentAsString();
        final Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    @TestDescription("????????? ??????")
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
                .header(HttpHeaders.AUTHORIZATION, "bearer " + getAuthToken())
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
                        relaxedResponseFields(
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

    @DisplayName("????????? 30?????? 2????????? ?????????")
    @Test
    void queryEvents() throws Exception {
        getAuthToken();
        IntStream.range(1, 30).forEach(this::generateEvent);

        mockMvc.perform(get("/api/events")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-event.href").doesNotExist())
                .andExpect(jsonPath("page.size").exists())
                .andDo(document("get-events"));
    }

    @DisplayName("????????? 30?????? 2????????? ????????? (??????)")
    @Test
    void queryEventsWithAuth() throws Exception {
        final String authToken = getAuthToken();
        IntStream.range(1, 30).forEach(this::generateEvent);

        mockMvc.perform(get("/api/events")
                .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken)
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self.href").exists())
                .andExpect(jsonPath("_links.profile.href").exists())
                .andExpect(jsonPath("_links.create-event.href").exists())
                .andExpect(jsonPath("page.size").exists())
                .andDo(document("get-events"));
    }

    @DisplayName("????????? ??????")
    @Test
    void getEvent() throws Exception {
        getAuthToken();
        final Event event = generateEvent(100);

        mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("description").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.update-event").doesNotExist())
                .andDo(document("get-event"));
    }

    @DisplayName("????????? ?????? (??????)")
    @Test
    void getEventWithAuth() throws Exception {
        final String authToken = getAuthToken();

        final Event event = generateEvent(100);

        mockMvc.perform(get("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("description").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("get-event"));
    }

    @DisplayName("?????? ????????? ???????????? ??? 404 ????????????")
    @Test
    void getEvent404() throws Exception {
        mockMvc.perform(get("/api/events/99999"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("????????? ?????? ??????")
    @Test
    void updateEventWithAuth() throws Exception {
        final String authToken = getAuthToken();
        final Event event = generateEvent(200);
        final EventDto eventDto = modelMapper.map(event, EventDto.class);
        String newName = "updated event";
        eventDto.setName(newName);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                    .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(newName))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-event"));
    }

    @DisplayName("????????? ?????? ?????? (?????? ??????)")
    @Test
    void updateEvent() throws Exception {
        getAuthToken();
        final Event event = generateEvent(200);
        final EventDto eventDto = modelMapper.map(event, EventDto.class);
        String newName = "updated event";
        eventDto.setName(newName);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("????????? ???????????? ?????? ??????")
    @Test
    void updateEvent400_empty() throws Exception {
        final String authToken = getAuthToken();
        final Event event = generateEvent(200);
        final EventDto eventDto = new EventDto();

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("????????? ????????? ?????? ?????? ??????")
    @Test
    void updateEvent400_wrong() throws Exception {
        final String authToken = getAuthToken();
        final Event event = generateEvent(200);
        final EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(1000);

        mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("???????????? ?????? ????????? ?????? ??????")
    @Test
    void updateEvent404() throws Exception {
        final String authToken = getAuthToken();
        final Event event = generateEvent(200);
        final EventDto eventDto = modelMapper.map(event, EventDto.class);

        mockMvc.perform(put("/api/events/99999")
                .header(HttpHeaders.AUTHORIZATION, "bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int i) {
        final Optional<Account> account = accountRepository.findByEmail(properties.getUserUsername());

        Event event = Event.builder()
                .name("event " + i)
                .description("test " + i)
                .beginEnrollmentDateTime(LocalDateTime.now())
                .closeEnrollmentDateTime(LocalDateTime.now().plusDays(1))
                .beginEventDateTime(LocalDateTime.now())
                .endEventDateTime(LocalDateTime.now().plusDays(1))
                .location("korea")
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .manager(Account.builder().id(account.get().getId()).build())
                .build();

        return eventRepository.save(event);
    }

    @DisplayName("???????????? ?????? ????????? ????????????")
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
                .header(HttpHeaders.AUTHORIZATION, "bearer " + getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @DisplayName("???????????? ????????? ?????? ???")
    @Test
    void createEvent_empty() throws Exception {
        EventDto event = EventDto.builder()
                .build();

        mockMvc.perform(post("/api/events")
                .header(HttpHeaders.AUTHORIZATION, "bearer " + getAuthToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(mapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("?????? ?????? ??????")
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
                .header(HttpHeaders.AUTHORIZATION, "bearer " + getAuthToken())
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
