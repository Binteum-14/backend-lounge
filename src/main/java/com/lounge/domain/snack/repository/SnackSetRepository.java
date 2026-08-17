package com.lounge.domain.snack.repository;

import com.lounge.domain.snack.entity.SnackSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SnackSetRepository extends JpaRepository<SnackSet, Long> {

    @EntityGraph(attributePaths = "productVariant")
    Optional<SnackSet> findBySnack_Id(Long snackId);
}
