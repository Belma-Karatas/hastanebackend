package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Brans;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BransRepository extends JpaRepository<Brans, Integer> {

    Optional<Brans> findByAd(String ad);

    boolean existsByAd(String ad);
}