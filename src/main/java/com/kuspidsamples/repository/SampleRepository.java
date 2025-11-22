package com.kuspidsamples.repository;

import com.kuspidsamples.entity.Sample;
import com.kuspidsamples.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findByUser(User user);

    Page<Sample> findByUser(User user, Pageable pageable);

    List<Sample> findByUserId(Long userId);

    Optional<Sample> findByIdAndUserId(Long id, Long userId);
}