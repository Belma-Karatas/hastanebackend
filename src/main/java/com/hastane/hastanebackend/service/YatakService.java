package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.YatakDTO;

import java.util.List;
import java.util.Optional;

public interface YatakService {

    /**
     * Veritabanındaki tüm yatakları DTO olarak listeler.
     *
     * @return YatakDTO nesnelerinden oluşan bir liste.
     */
    List<YatakDTO> getAllYataklar();

    /**
     * Belirli bir odaya ait tüm yatakları DTO olarak listeler.
     *
     * @param odaId Oda ID'si.
     * @return Belirtilen odaya ait YatakDTO nesnelerinden oluşan bir liste.
     */
    List<YatakDTO> getYataklarByOdaId(Integer odaId);

    /**
     * Verilen ID'ye sahip yatağı DTO olarak bulur.
     *
     * @param id Aranacak yatağın ID'si.
     * @return Bulunursa YatakDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<YatakDTO> getYatakById(Integer id);

    /**
     * Yeni bir yatak oluşturur ve veritabanına kaydeder.
     * Yatağın belirtilen odada aynı numaraya sahip olup olmadığını kontrol eder.
     *
     * @param yatakDTO Kaydedilecek yatak bilgilerini içeren DTO.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni YatakDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer DTO'da belirtilen oda ID'si ile oda bulunamazsa.
     * @throws IllegalArgumentException Eğer yatak numarası o odada zaten mevcutsa veya DTO'da eksik/hatalı bilgi varsa.
     */
    YatakDTO createYatak(YatakDTO yatakDTO);

    /**
     * Mevcut bir yatağın bilgilerini günceller (örn: yatak numarası, ait olduğu oda, dolu/boş durumu).
     * Oda değişikliği veya yatak numarası değişikliği durumunda benzersizlik kontrolü yapar.
     *
     * @param id       Güncellenecek yatağın ID'si.
     * @param yatakDTO Güncel bilgileri içeren YatakDTO nesnesi.
     * @return Güncellenmiş YatakDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer güncellenecek yatak veya DTO'da belirtilen yeni oda ID'si ile oda bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni yatak numarası yeni (veya mevcut) odada zaten mevcutsa veya DTO'da eksik/hatalı bilgi varsa.
     * @throws IllegalStateException Eğer dolu bir yatağın odası değiştirilmeye çalışılırsa (opsiyonel iş kuralı).
     */
    YatakDTO updateYatak(Integer id, YatakDTO yatakDTO);

    /**
     * Bir yatağın dolu/boş durumunu günceller.
     * Bu, bir hasta yatışı yapıldığında veya hasta taburcu olduğunda kullanılır.
     *
     * @param yatakId Güncellenecek yatağın ID'si.
     * @param doluMu  Yatağın yeni dolu/boş durumu.
     * @return Güncellenmiş YatakDTO nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer yatak bulunamazsa.
     */
    YatakDTO updateYatakDolulukDurumu(Integer yatakId, boolean doluMu);


    /**
     * Verilen ID'ye sahip yatağı veritabanından siler.
     *
     * @param id Silinecek yatağın ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile yatak bulunamazsa.
     * @throws IllegalStateException Eğer yatak doluysa ve silinemiyorsa (iş kuralı).
     * @throws org.springframework.dao.DataIntegrityViolationException Eğer yatağın aktif bir yatış kaydı varsa (Yatis entity'si eklendiğinde).
     */
    void deleteYatak(Integer id);

    /**
     * Belirli bir odadaki boş yatakları listeler.
     * @param odaId Oda ID'si
     * @return Boş yatakların listesi (DTO olarak)
     */
    List<YatakDTO> getBosYataklarByOdaId(Integer odaId);

    /**
     * Hastanedeki tüm boş yatakları listeler.
     * @return Tüm boş yatakların listesi (DTO olarak)
     */
    List<YatakDTO> getTumBosYataklar();
}