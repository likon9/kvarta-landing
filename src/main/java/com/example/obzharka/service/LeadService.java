package com.example.obzharka.service;

import com.example.obzharka.dto.LeadRequest;
import com.example.obzharka.entity.Order;
import com.example.obzharka.repository.OrderRepository;
import com.example.obzharka.util.PhoneNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LeadService {

    private static final Duration DEDUP_WINDOW = Duration.ofMinutes(10);

    private final OrderRepository repository;

    /** @return true, если заявка создана; false, если это повтор за последние 10 минут */
    @Transactional
    public boolean create(LeadRequest request) {
        String phone = PhoneNormalizer.normalize(request.phone());

        repository.lockByKey(phone);
        if (repository.existsByPhoneAndCreatedAtAfter(phone, Instant.now().minus(DEDUP_WINDOW))) {
            return false;
        }

        Order order = new Order();
        order.setName(request.name());
        order.setPhone(phone);
        order.setInterest(request.interest());
        order.setHeardFrom(request.heardFrom());
        order.setComment(request.comment());
        order.setLandingUrl(request.landingUrl());
        repository.save(order);
        return true;
    }
}