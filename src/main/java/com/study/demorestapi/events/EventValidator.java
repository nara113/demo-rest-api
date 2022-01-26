package com.study.demorestapi.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator {

    public void validate(EventDto eventDto, Errors errors) {
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() > 0) {
            errors.rejectValue("basePrice", "wrong value", "base price is wrong.");
            errors.rejectValue("maxPrice", "wrong value", "max price is wrong.");
        }

        if (eventDto.getCloseEnrollmentDateTime().isBefore(eventDto.getBeginEnrollmentDateTime()) ||
            eventDto.getEndEventDateTime().isBefore(eventDto.getBeginEventDateTime())) {
            errors.rejectValue("endEventDateTime", "wrong value");
        }
    }
}
