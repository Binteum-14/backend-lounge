package com.lounge.domain.snack.repository;

import com.lounge.domain.snack.entity.Snack;
import com.lounge.domain.snack.entity.SnackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SnackRepository extends JpaRepository<Snack, Long> {

    List<Snack> findByActiveTrueOrderByIdAsc();

    List<Snack> findByTypeAndActiveTrueOrderByIdAsc(SnackType type);

    Optional<Snack> findByIdAndActiveTrue(Long id);
}
