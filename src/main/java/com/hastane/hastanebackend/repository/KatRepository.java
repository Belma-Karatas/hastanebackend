package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Kat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KatRepository extends JpaRepository<Kat, Integer> {

    /**
     * Bir katı adıyla bulur. Kat adının unique olduğunu varsayıyoruz.
     *
     * @param ad Aranacak katın adı.
     * @return Bulunursa Kat nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Kat> findByAd(String ad);

    /**
     * Verilen isme sahip bir katın veritabanında olup olmadığını kontrol eder.
     *
     * @param ad Kontrol edilecek katın adı.
     * @return Kat varsa true, yoksa false döner.
     */
    boolean existsByAd(String ad);
}