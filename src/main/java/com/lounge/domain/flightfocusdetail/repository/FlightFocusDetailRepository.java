package com.lounge.domain.flightfocusdetail.repository;

import com.lounge.domain.flightfocusdetail.entity.FlightFocusDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightFocusDetailRepository extends JpaRepository<FlightFocusDetail, Long> {

    void deleteByFocusRecord_User_Id(Long userId);
}
