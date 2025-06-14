package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.IzinTuru;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IzinTuruRepository extends JpaRepository<IzinTuru, Integer> {
    Optional<IzinTuru> findByAd(String ad);
    boolean existsByAd(String ad);
}