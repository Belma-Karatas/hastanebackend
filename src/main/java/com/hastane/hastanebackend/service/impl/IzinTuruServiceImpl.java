package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.IzinTuruDTO;
import com.hastane.hastanebackend.entity.IzinTuru;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.IzinTalepRepository; // BU IMPORTU EKLE
import com.hastane.hastanebackend.repository.IzinTuruRepository;
import com.hastane.hastanebackend.service.IzinTuruService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IzinTuruServiceImpl implements IzinTuruService {

    private static final Logger log = LoggerFactory.getLogger(IzinTuruServiceImpl.class);

    private final IzinTuruRepository izinTuruRepository;
    private final IzinTalepRepository izinTalepRepository; // YENİ: Bağımlılık eklendi

    @Autowired
    public IzinTuruServiceImpl(IzinTuruRepository izinTuruRepository,
                                 IzinTalepRepository izinTalepRepository) { // YENİ: Constructor'a eklendi
        this.izinTuruRepository = izinTuruRepository;
        this.izinTalepRepository = izinTalepRepository; // YENİ: Atama yapıldı
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private IzinTuruDTO convertToDTO(IzinTuru izinTuru) {
        if (izinTuru == null) {
            return null;
        }
        return new IzinTuruDTO(izinTuru.getId(), izinTuru.getAd());
    }

    private IzinTuru convertToEntity(IzinTuruDTO izinTuruDTO) {
        if (izinTuruDTO == null) {
            return null;
        }
        IzinTuru izinTuru = new IzinTuru();
        izinTuru.setId(izinTuruDTO.getId());
        izinTuru.setAd(izinTuruDTO.getAd());
        return izinTuru;
    }
    // --- Dönüşüm Metotları Sonu ---

    @Override
    @Transactional(readOnly = true)
    public List<IzinTuruDTO> getAllIzinTurleri() {
        log.info("Tüm izin türleri getiriliyor.");
        return izinTuruRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IzinTuruDTO> getIzinTuruById(Integer id) {
        log.info("İzin Türü ID {} için detaylar getiriliyor.", id);
        return izinTuruRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public IzinTuruDTO createIzinTuru(IzinTuruDTO izinTuruDTO) {
        log.info("Yeni izin türü oluşturuluyor: {}", izinTuruDTO.getAd());
        if (izinTuruDTO.getAd() == null || izinTuruDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("İzin türü adı boş olamaz.");
        }
        if (izinTuruRepository.existsByAd(izinTuruDTO.getAd())) {
            throw new IllegalArgumentException("Bu izin türü adı zaten mevcut: " + izinTuruDTO.getAd());
        }
        IzinTuru izinTuru = convertToEntity(izinTuruDTO);
        izinTuru.setId(null); // Yeni kayıt
        IzinTuru kaydedilmisIzinTuru = izinTuruRepository.save(izinTuru);
        log.info("İzin türü başarıyla oluşturuldu. ID: {}, Ad: {}", kaydedilmisIzinTuru.getId(), kaydedilmisIzinTuru.getAd());
        return convertToDTO(kaydedilmisIzinTuru);
    }

    @Override
    @Transactional
    public IzinTuruDTO updateIzinTuru(Integer id, IzinTuruDTO izinTuruDTO) {
        log.info("İzin Türü ID {} güncelleniyor. Yeni ad: {}", id, izinTuruDTO.getAd());
        IzinTuru mevcutIzinTuru = izinTuruRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek izin türü bulunamadı, ID: " + id));

        if (izinTuruDTO.getAd() == null || izinTuruDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("İzin türü adı boş olamaz.");
        }

        if (!mevcutIzinTuru.getAd().equalsIgnoreCase(izinTuruDTO.getAd()) && izinTuruRepository.existsByAd(izinTuruDTO.getAd())) {
            throw new IllegalArgumentException("Güncellenmek istenen izin türü adı ('" + izinTuruDTO.getAd() + "') zaten başka bir türde kullanılıyor.");
        }
        
        mevcutIzinTuru.setAd(izinTuruDTO.getAd());
        IzinTuru guncellenmisIzinTuru = izinTuruRepository.save(mevcutIzinTuru);
        log.info("İzin türü başarıyla güncellendi. ID: {}, Ad: {}", guncellenmisIzinTuru.getId(), guncellenmisIzinTuru.getAd());
        return convertToDTO(guncellenmisIzinTuru);
    }

    @Override
    @Transactional
    public void deleteIzinTuru(Integer id) {
        log.info("İzin Türü ID {} siliniyor.", id);
        IzinTuru izinTuru = izinTuruRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek izin türü bulunamadı, ID: " + id));

        // YENİ EKLENEN KONTROL: Bu izin türünün herhangi bir izin talebinde kullanılıp kullanılmadığı kontrol edilir.
        if (izinTalepRepository.existsByIzinTuru_Id(id)) {
            throw new IllegalStateException("Bu izin türü (ID: " + id + ") aktif izin taleplerinde kullanıldığı için silinemez.");
        }

        izinTuruRepository.delete(izinTuru);
        log.info("İzin türü başarıyla silindi. ID: {}", id);
    }
}