package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Spring Data JPA'nın isimlendirme gücü:
    // Bu metodu yazdığımızda, Spring otomatik olarak "Rol tablosunda 'ad' sütunu
    // benim verdiğim string'e eşit olan kaydı bul" anlamına gelen bir SQL sorgusu oluşturur.
    Optional<Rol> findByAd(String ad);
}