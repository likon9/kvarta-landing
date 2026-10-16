package com.example.obzharka.repository;

import com.example.obzharka.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByPhoneAndCreatedAtAfter(String phone, Instant since);

    /** Блокировка до конца транзакции: параллельные запросы с одним номером встают в очередь. */
    @Query(value = "SELECT CAST(pg_advisory_xact_lock(hashtext(:key)) AS text)", nativeQuery = true)
    String lockByKey(@Param("key") String key);
}
