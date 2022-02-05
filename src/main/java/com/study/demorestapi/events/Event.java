package com.study.demorestapi.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.study.demorestapi.accounts.Account;
import com.study.demorestapi.accounts.AccountSerializer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@Entity
public class Event {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime beginEnrollmentDateTime;
    private LocalDateTime closeEnrollmentDateTime;
    private LocalDateTime beginEventDateTime;
    private LocalDateTime endEventDateTime;
    private String location; // (optional) 이게 없으면 온라인 모임 ​
    private int basePrice; // (optional)
    private int maxPrice; // (optional)
    private int limitOfEnrollment;
    private boolean offline;
    private boolean free;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus = EventStatus.DRAFT;

    @ManyToOne
    @JsonSerialize(using = AccountSerializer.class)
    private Account manager;

    public void update() {
        free = basePrice == 0 && maxPrice == 0;

        // isBlank java 11에 추
        offline = location != null && !location.isBlank();
    }
}

