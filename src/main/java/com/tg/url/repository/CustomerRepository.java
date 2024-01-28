package com.tg.url.repository;

import com.tg.url.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // 필요한 쿼리 메서드를 여기에 정의할 수 있습니다.
}
