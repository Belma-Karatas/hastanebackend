package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.KatDTO;
import com.hastane.hastanebackend.entity.Kat; // Entity'yi de import edebiliriz, dönüşümlerde gerekebilir

import java.util.List;
import java.util.Optional;

public interface KatService {

    /**
     * Veritabanındaki tüm katları DTO olarak listeler.
     *
     * @return KatDTO nesnelerinden oluşan bir liste.
     */
    List<KatDTO> getAllKatlar();

    /**
     * Verilen ID'ye sahip katı DTO olarak bulur.
     *
     * @param id Aranacak katın ID'si.
     * @return Bulunursa KatDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<KatDTO> getKatById(Integer id);

    /**
     * Yeni bir kat oluşturur ve veritabanına kaydeder.
     * Kat adının benzersiz olup olmadığını kontrol eder.
     *
     * @param katDTO Kaydedilecek kat bilgilerini içeren DTO.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni KatDTO nesnesi.
     * @throws IllegalArgumentException Eğer kat adı zaten mevcutsa.
     */
    KatDTO createKat(KatDTO katDTO);

    /**
     * Mevcut bir katın bilgilerini günceller.
     *
     * @param id     Güncellenecek katın ID'si.
     * @param katDTO Güncel bilgileri içeren KatDTO nesnesi.
     * @return Güncellenmiş KatDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile kat bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni kat adı zaten başka bir katta kullanılıyorsa (ve mevcut kattan farklıysa).
     */
    KatDTO updateKat(Integer id, KatDTO katDTO);

    /**
     * Verilen ID'ye sahip katı veritabanından siler.
     *
     * @param id Silinecek katın ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile kat bulunamazsa.
     * @throws org.springframework.dao.DataIntegrityViolationException Eğer katın ilişkili odaları varsa ve silinemiyorsa
     *         (cascade ayarına bağlı olarak bu exception fırlatılmayabilir, odalar da silinebilir).
     */
    void deleteKat(Integer id);
}