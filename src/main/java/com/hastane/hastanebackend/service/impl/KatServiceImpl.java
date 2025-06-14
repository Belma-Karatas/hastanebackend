package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.KatDTO;
import com.hastane.hastanebackend.entity.Kat;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KatRepository;
import com.hastane.hastanebackend.service.KatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KatServiceImpl implements KatService {

    private final KatRepository katRepository;

    @Autowired
    public KatServiceImpl(KatRepository katRepository) {
        this.katRepository = katRepository;
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private KatDTO convertToDTO(Kat kat) {
        if (kat == null) {
            return null;
        }
        return new KatDTO(kat.getId(), kat.getAd());
    }

    private Kat convertToEntity(KatDTO katDTO) {
        if (katDTO == null) {
            return null;
        }
        Kat kat = new Kat();
        kat.setId(katDTO.getId()); // Güncelleme için ID gerekebilir
        kat.setAd(katDTO.getAd());
        // 'odalar' alanı DTO'da olmadığı için burada set edilmiyor.
        // Oda ekleme/çıkarma işlemleri Kat entity'si üzerinden yönetilecek.
        return kat;
    }
    // --- Dönüşüm Metotları Sonu ---

    @Override
    @Transactional(readOnly = true)
    public List<KatDTO> getAllKatlar() {
        return katRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KatDTO> getKatById(Integer id) {
        return katRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public KatDTO createKat(KatDTO katDTO) {
        if (katDTO.getAd() == null || katDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("Kat adı boş olamaz.");
        }
        if (katRepository.existsByAd(katDTO.getAd())) {
            throw new IllegalArgumentException("Bu kat adı zaten mevcut: " + katDTO.getAd());
        }
        Kat kat = convertToEntity(katDTO);
        kat.setId(null); // Yeni kayıt oluştururken ID'nin null olduğundan emin ol
        Kat kaydedilmisKat = katRepository.save(kat);
        return convertToDTO(kaydedilmisKat);
    }

    @Override
    @Transactional
    public KatDTO updateKat(Integer id, KatDTO katDTO) {
        Kat mevcutKat = katRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kat bulunamadı, ID: " + id));

        if (katDTO.getAd() == null || katDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("Kat adı boş olamaz.");
        }

        // Eğer ad değişmişse ve yeni ad başkası tarafından kullanılıyorsa hata ver.
        if (!mevcutKat.getAd().equalsIgnoreCase(katDTO.getAd()) && katRepository.existsByAd(katDTO.getAd())) {
            throw new IllegalArgumentException("Güncellenmek istenen kat adı ('" + katDTO.getAd() + "') zaten başka bir katta kullanılıyor.");
        }
        
        mevcutKat.setAd(katDTO.getAd());
        Kat guncellenmisKat = katRepository.save(mevcutKat);
        return convertToDTO(guncellenmisKat);
    }

    @Override
    @Transactional
    public void deleteKat(Integer id) {
        Kat kat = katRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek kat bulunamadı, ID: " + id));
        
        // Kat entity'sindeki cascade ayarları (CascadeType.ALL ve orphanRemoval=true odalar için)
        // bu kat silindiğinde ilişkili odaların da silinmesini sağlayacaktır.
        // Eğer odaların silinmesi istenmiyorsa veya başka bir işlem yapılması gerekiyorsa
        // (örn: odaları başka bir kata atamak), bu mantık burada ele alınmalıdır.
        // Şimdilik cascade'e güveniyoruz.
        katRepository.delete(kat);
    }
}