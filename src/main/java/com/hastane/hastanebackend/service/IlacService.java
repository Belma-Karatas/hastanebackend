package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.entity.Ilac; // Ilac entity'sini import et
// İleride IlacDTO kullanacaksak onu da import edeceğiz.
// import com.hastane.hastanebackend.dto.IlacDTO;

import java.util.List;
import java.util.Optional;

public interface IlacService {

    /**
     * Veritabanındaki tüm ilaçları listeler.
     *
     * @return Ilac nesnelerinden oluşan bir liste.
     */
    List<Ilac> getAllIlaclar();

    /**
     * Verilen ID'ye sahip ilacı bulur.
     *
     * @param id Aranacak ilacın ID'si.
     * @return Bulunursa Ilac nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Ilac> getIlacById(Integer id);

    /**
     * Yeni bir ilaç oluşturur ve veritabanına kaydeder.
     * İlaç adının benzersiz (unique) olup olmadığını kontrol eder.
     *
     * @param ilac Kaydedilecek Ilac nesnesi. (DTO kullanılabilir: IlacDTO ilacDto)
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni Ilac nesnesi.
     * @throws IllegalArgumentException Eğer ilaç adı zaten mevcutsa.
     */
    Ilac createIlac(Ilac ilac); // Veya Ilac createIlac(IlacDTO ilacDto);

    /**
     * Mevcut bir ilacın bilgilerini günceller.
     *
     * @param id          Güncellenecek ilacın ID'si.
     * @param ilacDetails Güncel bilgileri içeren Ilac nesnesi. (DTO kullanılabilir: IlacDTO ilacDetailsDto)
     * @return Güncellenmiş Ilac nesnesi.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile ilaç bulunamazsa.
     * @throws IllegalArgumentException Eğer yeni ilaç adı zaten başka bir ilaçta kullanılıyorsa (opsiyonel kontrol).
     */
    Ilac updateIlac(Integer id, Ilac ilacDetails); // Veya Ilac updateIlac(Integer id, IlacDTO ilacDetailsDto);

    /**
     * Verilen ID'ye sahip ilacı veritabanından siler.
     *
     * @param id Silinecek ilacın ID'si.
     * @throws com.hastane.hastanebackend.exception.ResourceNotFoundException Eğer verilen ID ile ilaç bulunamazsa.
     * @throws DataIntegrityViolationException Eğer ilaç başka kayıtlarla (örn: reçeteler) ilişkiliyse ve silinemiyorsa.
     */
    void deleteIlac(Integer id);

    /**
     * Adında belirli bir anahtar kelime geçen ilaçları arar (büyük/küçük harf duyarsız).
     *
     * @param adKeyword Aranan anahtar kelime.
     * @return Eşleşen ilaçların listesi.
     */
    List<Ilac> searchIlacByAdKeyword(String adKeyword);
}