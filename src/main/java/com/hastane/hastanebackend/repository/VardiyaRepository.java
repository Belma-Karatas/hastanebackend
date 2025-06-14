package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Vardiya;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VardiyaRepository extends JpaRepository<Vardiya, Integer> {
    Optional<Vardiya> findByAd(String ad);
    boolean existsByAd(String ad);
}