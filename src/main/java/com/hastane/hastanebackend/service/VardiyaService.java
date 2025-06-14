package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.VardiyaDTO;

import java.util.List;
import java.util.Optional;

public interface VardiyaService {

    /**
     * Veritabanındaki tüm vardiya tanımlarını DTO olarak listeler.
     *
     * @return VardiyaDTO nesnelerinden oluşan bir liste.
     */
    List<VardiyaDTO> getAllVardiyalar();

    /**
     * Verilen ID'ye sahip vardiya tanımını DTO olarak bulur.
     *
     * @param id Aranacak vardiyanın ID'si.
     * @return Bulunursa VardiyaDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<VardiyaDTO> getVardiyaById(Integer id);

    /**
     * Yeni bir vardiya tanımı oluşturur ve veritabanına kaydeder.
     * Vardiya adının benzersiz olup olmadığını kontrol eder.
     * Bu işlem sadece ADMIN tarafından yapılır.
     *
     * @param vardiyaDTO Kaydedilecek vardiya bilgilerini içeren DTO.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni VardiyaDTO nesnesi.
     * @throws IllegalArgumentException Eğer vardiya adı zaten mevcutsa veya başlangıç/bitiş saatleri mantıksızsa.
     */
    VardiyaDTO createVardiya(VardiyaDTO vardiyaDTO);

    /**
     * Mevcut bir vardiya tanımının bilgilerini günceller.
     * Bu işlem sadece ADMIN tarafından yapılır.
     *
     * @param id         Güncellenecek vardiyanın ID'si.
     * @param vardiyaDTO Güncel bilgileri içeren VardiyaDTO nesnesi.
     * @return Güncellenmiş VardiyaDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile vardiya bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni vardiya adı zaten başka bir vardiyada kullanılıyorsa veya saatler mantıksızsa.
     */
    VardiyaDTO updateVardiya(Integer id, VardiyaDTO vardiyaDTO);

    /**
     * Verilen ID'ye sahip vardiya tanımını veritabanından siler.
     * Bu işlem sadece ADMIN tarafından yapılır.
     * Silmeden önce bu vardiyanın herhangi bir personel vardiya atamasında kullanılıp kullanılmadığı kontrol edilmelidir.
     *
     * @param id Silinecek vardiyanın ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile vardiya bulunamazsa.
     * @throws IllegalStateException Eğer vardiya aktif personel vardiya atamalarında kullanılıyorsa ve silinemiyorsa.
     */
    void deleteVardiya(Integer id);
}