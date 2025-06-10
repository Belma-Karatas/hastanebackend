package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Departman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Departman entity'si için veritabanı işlemlerini gerçekleştiren Spring Data JPA repository arayüzü.
 */
@Repository
public interface DepartmanRepository extends JpaRepository<Departman, Integer> {

    /**
     * Bir departmanı adıyla bulur. Spring Data JPA, bu metot isminden otomatik olarak
     * "SELECT * FROM Departman WHERE departman_adi = ?" sorgusunu oluşturur.
     *
     * @param ad Aranacak departmanın adı.
     * @return Bulunursa Departman nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Departman> findByAd(String ad);

    /**
     * Verilen isme sahip bir departmanın veritabanında olup olmadığını kontrol eder.
     * Bu, tüm nesneyi çekmekten daha performanslı bir yöntemdir.
     *
     * @param ad Kontrol edilecek departmanın adı.
     * @return Departman varsa true, yoksa false döner.
     */
    boolean existsByAd(String ad);
}