package com.lounge.domain.snack.repository;

import com.lounge.domain.snack.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnackRepository extends JpaRepository<Snack, Long> {

    List<Snack> findByActiveTrueOrderByIdAsc();

    Optional<Snack> findByIdAndActiveTrue(Long id);
}
