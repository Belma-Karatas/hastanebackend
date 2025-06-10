package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.entity.Departman;
import java.util.List;
import java.util.Optional;

/**
 * Departman varlığı ile ilgili iş mantığı operasyonlarını tanımlayan arayüz.
 * Bu arayüz, Controller katmanı ile Repository katmanı arasında bir köprü görevi görür.
 */
public interface DepartmanService {

    /**
     * Veritabanındaki tüm departmanları listeler.
     *
     * @return Departman nesnelerinden oluşan bir liste.
     */
    List<Departman> getAllDepartmanlar();

    /**
     * Verilen ID'ye sahip departmanı bulur.
     *
     * @param id Aranacak departmanın ID'si.
     * @return Bulunursa Departman nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<Departman> getDepartmanById(Integer id);

    /**
     * Yeni bir departman oluşturur ve veritabanına kaydeder.
     *
     * @param departman Kaydedilecek Departman nesnesi.
     * @return Veritabanına kaydedilmiş, ID'si atanmış yeni Departman nesnesi.
     */
    Departman createDepartman(Departman departman);

    /**
     * Mevcut bir departmanın bilgilerini günceller.
     *
     * @param id               Güncellenecek departmanın ID'si.
     * @param departmanDetails Güncel bilgileri içeren Departman nesnesi.
     * @return Güncellenmiş Departman nesnesi.
     */
    Departman updateDepartman(Integer id, Departman departmanDetails);

    /**
     * Verilen ID'ye sahip departmanı veritabanından siler.
     *
     * @param id Silinecek departmanın ID'si.
     */
    void deleteDepartman(Integer id);
}