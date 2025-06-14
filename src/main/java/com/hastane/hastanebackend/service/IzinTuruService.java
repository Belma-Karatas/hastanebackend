package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.IzinTuruDTO;

import java.util.List;
import java.util.Optional;

public interface IzinTuruService {

    /**
     * Veritabanındaki tüm izin türlerini DTO olarak listeler.
     *
     * @return IzinTuruDTO nesnelerinden oluşan bir liste.
     */
    List<IzinTuruDTO> getAllIzinTurleri();

    /**
     * Verilen ID'ye sahip izin türünü DTO olarak bulur.
     *
     * @param id Aranacak izin türünün ID'si.
     * @return Bulunursa IzinTuruDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<IzinTuruDTO> getIzinTuruById(Integer id);

    /**
     * Yeni bir izin türü oluşturur ve veritabanına kaydeder.
     * İzin türü adının benzersiz olup olmadığını kontrol eder.
     * Bu işlem genellikle ADMIN tarafından yapılır.
     *
     * @param izinTuruDTO Kaydedilecek izin türü bilgilerini içeren DTO.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni IzinTuruDTO nesnesi.
     * @throws IllegalArgumentException Eğer izin türü adı zaten mevcutsa.
     */
    IzinTuruDTO createIzinTuru(IzinTuruDTO izinTuruDTO);

    /**
     * Mevcut bir izin türünün bilgilerini günceller.
     * Bu işlem genellikle ADMIN tarafından yapılır.
     *
     * @param id          Güncellenecek izin türünün ID'si.
     * @param izinTuruDTO Güncel bilgileri içeren IzinTuruDTO nesnesi.
     * @return Güncellenmiş IzinTuruDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile izin türü bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni izin türü adı zaten başka bir türde kullanılıyorsa (ve mevcut türden farklıysa).
     */
    IzinTuruDTO updateIzinTuru(Integer id, IzinTuruDTO izinTuruDTO);

    /**
     * Verilen ID'ye sahip izin türünü veritabanından siler.
     * Bu işlem genellikle ADMIN tarafından yapılır.
     * Silmeden önce bu izin türünün herhangi bir izin talebinde kullanılıp kullanılmadığı kontrol edilmelidir.
     *
     * @param id Silinecek izin türünün ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile izin türü bulunamazsa.
     * @throws IllegalStateException Eğer izin türü aktif izin taleplerinde kullanılıyorsa ve silinemiyorsa.
     */
    void deleteIzinTuru(Integer id);
}