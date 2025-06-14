package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Randevu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RandevuRepository extends JpaRepository<Randevu, Integer> {

    // Bir hastanın belirli bir tarih aralığındaki randevularını bulma
    List<Randevu> findByHasta_IdAndRandevuTarihiSaatiBetween(Integer hastaId, LocalDateTime baslangic, LocalDateTime bitis);

    // Bir doktorun belirli bir tarih aralığındaki randevularını bulma
    List<Randevu> findByDoktor_IdAndRandevuTarihiSaatiBetween(Integer doktorId, LocalDateTime baslangic, LocalDateTime bitis);

    // Bir hastanın tüm randevuları (gelecek veya geçmiş)
    List<Randevu> findByHasta_IdOrderByRandevuTarihiSaatiDesc(Integer hastaId);

    // Bir doktorun tüm randevuları (gelecek veya geçmiş)
    List<Randevu> findByDoktor_IdOrderByRandevuTarihiSaatiDesc(Integer doktorId);

    // Bir doktorun belirli bir saatte randevusu olup olmadığını kontrol etme (randevu çakışması için)
    Optional<Randevu> findByDoktor_IdAndRandevuTarihiSaati(Integer doktorId, LocalDateTime randevuTarihiSaati);

    // Bir hastanın belirli bir saatte randevusu olup olmadığını kontrol etme (randevu çakışması için)
    Optional<Randevu> findByHasta_IdAndRandevuTarihiSaati(Integer hastaId, LocalDateTime randevuTarihiSaati);

    // İleride daha karmaşık sorgular için @Query kullanılabilir:
    // Örneğin, bir doktorun belirli bir gündeki tüm randevuları:
    @Query("SELECT r FROM Randevu r WHERE r.doktor.id = :doktorId AND FUNCTION('DATE', r.randevuTarihiSaati) = FUNCTION('DATE', :tarih)")
    List<Randevu> findByDoktorIdAndGun(@Param("doktorId") Integer doktorId, @Param("tarih") LocalDateTime tarih);
}