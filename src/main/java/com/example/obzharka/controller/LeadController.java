package com.example.obzharka.controller;

import com.example.obzharka.dto.LeadRequest;
import com.example.obzharka.dto.LeadResponse;
import com.example.obzharka.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lead")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService service;

    @PostMapping
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody LeadRequest request) {
        boolean created = service.create(request);
        return created
                ? ResponseEntity.status(HttpStatus.CREATED).body(LeadResponse.CREATED)
                : ResponseEntity.ok(LeadResponse.DUPLICATE);
    }
}
