package com.hastane.hastanebackend.repository;

import com.hastane.hastanebackend.entity.Muayene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MuayeneRepository extends JpaRepository<Muayene, Integer> {

    // Bir hastanın tüm muayenelerini getirme (tarihe göre sıralı)
    List<Muayene> findByHasta_IdOrderByMuayeneTarihiSaatiDesc(Integer hastaId);

    // Bir doktorun tüm muayenelerini getirme (tarihe göre sıralı)
    List<Muayene> findByDoktor_IdOrderByMuayeneTarihiSaatiDesc(Integer doktorId);

    // Bir doktorun belirli bir tarih aralığındaki muayeneleri
    List<Muayene> findByDoktor_IdAndMuayeneTarihiSaatiBetween(Integer doktorId, LocalDateTime baslangic, LocalDateTime bitis);

    // Bir hastanın belirli bir tarih aralığındaki muayeneleri
    List<Muayene> findByHasta_IdAndMuayeneTarihiSaatiBetween(Integer hastaId, LocalDateTime baslangic, LocalDateTime bitis);

    // Bir randevuya ait muayeneyi bulma (Randevu ID'si unique olduğu için Optional döner)
    Optional<Muayene> findByRandevu_Id(Integer randevuId);

    // Belirli bir tanıya sahip muayeneleri listeleme (ileride raporlama için gerekebilir)
    // List<Muayene> findByTaniContainingIgnoreCase(String taniKelimesi);
}