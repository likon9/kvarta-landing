package com.example.obzharka;

import com.example.obzharka.dto.LeadRequest;
import com.example.obzharka.entity.Interest;
import com.example.obzharka.repository.OrderRepository;
import com.example.obzharka.service.LeadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
class LeadConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    LeadService service;

    @Autowired
    OrderRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void parallelRequestsWithSamePhoneCreateOnlyOneLead() throws Exception {
        int threads = 10;
        // один и тот же номер, записанный по-разному
        String[] formats = {"+375 29 123-45-67", "80291234567", "375291234567", "8 (029) 123-45-67"};

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            String phone = formats[i % formats.length];
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();   // все потоки стартуют одновременно
                return service.create(
                        new LeadRequest("Тест", phone, Interest.ETHIOPIA, null, null, null));
            }));
        }

        ready.await();
        start.countDown();

        int created = 0;
        for (Future<Boolean> f : futures) {
            if (f.get(30, TimeUnit.SECONDS)) {
                created++;
            }
        }
        pool.shutdown();

        assertEquals(1, created);
        assertEquals(1, repository.count());
    }
}
