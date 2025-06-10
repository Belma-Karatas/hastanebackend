package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Personel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonelRepository extends JpaRepository<Personel, Integer> {

    Optional<Personel> findByKullanici_Email(String email);

    List<Personel> findByDepartman_Id(Integer departmanId);

    @Query("SELECT p FROM Personel p JOIN p.kullanici u JOIN u.roller r WHERE r.ad = :rolAdi")
    List<Personel> findByRolAdi(String rolAdi);
}