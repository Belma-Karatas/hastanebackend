package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.OdaDTO;

import java.util.List;
import java.util.Optional;

public interface OdaService {

    /**
     * Veritabanındaki tüm odaları DTO olarak listeler.
     *
     * @return OdaDTO nesnelerinden oluşan bir liste.
     */
    List<OdaDTO> getAllOdalar();

    /**
     * Belirli bir kata ait tüm odaları DTO olarak listeler.
     *
     * @param katId Kat ID'si.
     * @return Belirtilen kata ait OdaDTO nesnelerinden oluşan bir liste.
     */
    List<OdaDTO> getOdalarByKatId(Integer katId);

    /**
     * Verilen ID'ye sahip odayı DTO olarak bulur.
     *
     * @param id Aranacak odanın ID'si.
     * @return Bulunursa OdaDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<OdaDTO> getOdaById(Integer id);

    /**
     * Yeni bir oda oluşturur ve veritabanına kaydeder.
     * Odanın belirtilen katta aynı numaraya sahip olup olmadığını kontrol eder.
     *
     * @param odaDTO Kaydedilecek oda bilgilerini içeren DTO.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni OdaDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer DTO'da belirtilen kat ID'si ile kat bulunamazsa.
     * @throws IllegalArgumentException Eğer oda numarası o katta zaten mevcutsa veya DTO'da eksik/hatalı bilgi varsa.
     */
    OdaDTO createOda(OdaDTO odaDTO);

    /**
     * Mevcut bir odanın bilgilerini günceller.
     * Kat değişikliği veya oda numarası değişikliği durumunda benzersizlik kontrolü yapar.
     *
     * @param id     Güncellenecek odanın ID'si.
     * @param odaDTO Güncel bilgileri içeren OdaDTO nesnesi.
     * @return Güncellenmiş OdaDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer güncellenecek oda veya DTO'da belirtilen yeni kat ID'si ile kat bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni oda numarası yeni (veya mevcut) katta zaten mevcutsa veya DTO'da eksik/hatalı bilgi varsa.
     */
    OdaDTO updateOda(Integer id, OdaDTO odaDTO);

    /**
     * Verilen ID'ye sahip odayı veritabanından siler.
     *
     * @param id Silinecek odanın ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile oda bulunamazsa.
     * @throws org.springframework.dao.DataIntegrityViolationException Eğer odanın ilişkili yatakları varsa ve silinemiyorsa
     *         (cascade ayarına bağlı olarak bu exception fırlatılmayabilir, yataklar da silinebilir).
     */
    void deleteOda(Integer id);
}