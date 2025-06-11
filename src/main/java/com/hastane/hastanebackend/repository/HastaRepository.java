package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Hasta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HastaRepository extends JpaRepository<Hasta, Integer> {

    Optional<Hasta> findByTcKimlikNo(String tcKimlikNo);

    boolean existsByTcKimlikNo(String tcKimlikNo);

    // Kullanici ID'sine göre hasta bulmak için (ileride gerekebilir)
    Optional<Hasta> findByKullanici_Id(Integer kullaniciId);

    // Kullanici email'ine göre hasta bulmak için (ileride gerekebilir)
    Optional<Hasta> findByKullanici_Email(String email);
}