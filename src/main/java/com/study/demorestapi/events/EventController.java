package com.study.demorestapi.events;

import com.study.demorestapi.common.ErrorResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.stream.IntStream;

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
            return ResponseEntity.badRequest().body(ErrorResource.modelOf(errors));
        }

        eventValidator.validate(eventDto, errors);

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorResource.modelOf(errors));
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

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        final Page<Event> page = eventRepository.findAll(pageable);
        final PagedModel pagedModel = assembler.toModel(page, e -> EntityModel.of(e, linkTo(EventController.class).slash(e.getId()).withSelfRel()));
        pagedModel.add(Link.of("docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.ok(pagedModel);
    }
}
