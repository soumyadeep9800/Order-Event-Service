package com.ecommerce.orderevent.repository;

import com.ecommerce.orderevent.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
