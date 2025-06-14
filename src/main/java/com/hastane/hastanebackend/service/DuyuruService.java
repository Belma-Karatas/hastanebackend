package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.DuyuruDTO;

import java.util.List;
import java.util.Optional;

public interface DuyuruService {

    /**
     * Tüm duyuruları (genellikle yayın tarihine göre en yeniden eskiye) DTO olarak listeler.
     * Herkesin erişimine açık olabilir.
     *
     * @return DuyuruDTO nesnelerinden oluşan bir liste.
     */
    List<DuyuruDTO> getAllDuyurular();

    /**
     * Verilen ID'ye sahip duyuruyu DTO olarak bulur.
     * Herkesin erişimine açık olabilir.
     *
     * @param id Aranacak duyurunun ID'si.
     * @return Bulunursa DuyuruDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<DuyuruDTO> getDuyuruById(Integer id);

    /**
     * Yeni bir duyuru oluşturur ve veritabanına kaydeder.
     * Bu işlem sadece ADMIN tarafından yapılır. Yayınlayan personel, işlemi yapan ADMIN kullanıcısı olur.
     *
     * @param duyuruDTO        Kaydedilecek duyuru bilgilerini içeren DTO (başlık, içerik).
     * @param adminKullaniciId Duyuruyu yayınlayan ADMIN kullanıcısının ID'si.
     * @return Veritabanına kaydedilmiş, ID'si ve yayın tarihi atanmış yeni DuyuruDTO nesnesi.
     */
    DuyuruDTO createDuyuru(DuyuruDTO duyuruDTO, Integer adminKullaniciId);

    /**
     * Mevcut bir duyurunun bilgilerini günceller (başlık, içerik).
     * Bu işlem sadece duyuruyu yayınlayan ADMIN veya başka bir ADMIN tarafından yapılabilir.
     *
     * @param id               Güncellenecek duyurunun ID'si.
     * @param duyuruDTO        Güncel bilgileri içeren DuyuruDTO nesnesi.
     * @param adminKullaniciId İşlemi yapan ADMIN kullanıcısının ID'si.
     * @return Güncellenmiş DuyuruDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile duyuru bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException Eğer işlemi yapan adminin yetkisi yoksa (örn: sadece kendi yayınladığını güncelleyebilir gibi bir kural varsa).
     */
    DuyuruDTO updateDuyuru(Integer id, DuyuruDTO duyuruDTO, Integer adminKullaniciId);

    /**
     * Verilen ID'ye sahip duyuruyu veritabanından siler.
     * Bu işlem sadece duyuruyu yayınlayan ADMIN veya başka bir ADMIN tarafından yapılabilir.
     *
     * @param id               Silinecek duyurunun ID'si.
     * @param adminKullaniciId İşlemi yapan ADMIN kullanıcısının ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile duyuru bulunamazsa.
     * @throws org.springframework.security.access.AccessDeniedException Yetki sorunları için.
     */
    void deleteDuyuru(Integer id, Integer adminKullaniciId);
}