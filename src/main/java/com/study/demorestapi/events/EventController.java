package com.study.demorestapi.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
@Controller
public class EventController {

    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;

    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Validated EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        final Event event = modelMapper.map(eventDto, Event.class);
        event.update();

        final Event newEvent = eventRepository.save(event);

        final WebMvcLinkBuilder linkBuilder = linkTo(EventController.class).slash(newEvent.getId());

        EntityModel<Event> eventEntityModel = EntityModel.of(newEvent,
                linkBuilder.withSelfRel(),
                linkBuilder.withRel("query-events"),
                linkBuilder.withRel("update-event"),
                Link.of("docs/index.html#resources-events-create").withRel("profile")
        );

        return ResponseEntity.created(linkBuilder.toUri()).body(eventEntityModel);
    }
}
