package com.study.demorestapi.events;

import com.study.demorestapi.common.ErrorResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

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
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()) {
            return badRequest(errors);
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

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorResource.modelOf(errors));
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        final Page<Event> page = eventRepository.findAll(pageable);
        final PagedModel pagedModel = assembler.toModel(page, e -> EntityModel.of(e, linkTo(EventController.class).slash(e.getId()).withSelfRel()));
        pagedModel.add(Link.of("docs/index.html#resources-events-list").withRel("profile"));

        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity queryEvent(@PathVariable Integer id) {
        final Optional<Event> event = eventRepository.findById(id);

        if (event.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final Event e = event.get();
        final EventResource eventResource = new EventResource(e);
        eventResource.add(Link.of("docs/index.html#resources-events-get").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id, @RequestBody @Valid EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        final Optional<Event> event = eventRepository.findById(id);
        if (event.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final Event existingEvent = event.get();
        modelMapper.map(eventDto, existingEvent);
        final Event savedEvent = eventRepository.save(existingEvent);

        final EventResource eventResource = new EventResource(savedEvent);
        eventResource.add(Link.of("docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);
    }
}
