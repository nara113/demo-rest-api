package com.study.demorestapi.index;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public RepresentationModel index() {
        final RepresentationModel representationModel = new RepresentationModel();
        final Link eventsLink = linkTo(IndexController.class).withRel("events");

        representationModel.add(eventsLink);
        return representationModel;
    }
}
