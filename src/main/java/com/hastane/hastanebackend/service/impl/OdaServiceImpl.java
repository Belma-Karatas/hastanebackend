package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.OdaDTO;
import com.hastane.hastanebackend.entity.Kat;
import com.hastane.hastanebackend.entity.Oda;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KatRepository;
import com.hastane.hastanebackend.repository.OdaRepository;
import com.hastane.hastanebackend.service.OdaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OdaServiceImpl implements OdaService {

    private static final Logger log = LoggerFactory.getLogger(OdaServiceImpl.class);

    private final OdaRepository odaRepository;
    private final KatRepository katRepository; // Kat bilgisini çekmek ve doğrulamak için

    @Autowired
    public OdaServiceImpl(OdaRepository odaRepository, KatRepository katRepository) {
        this.odaRepository = odaRepository;
        this.katRepository = katRepository;
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private OdaDTO convertToDTO(Oda oda) {
        if (oda == null) {
            return null;
        }
        return OdaDTO.builder()
                .id(oda.getId())
                .odaNumarasi(oda.getOdaNumarasi())
                .kapasite(oda.getKapasite())
                .katId(oda.getKat().getId())
                .katAdi(oda.getKat().getAd()) // Kat adını da ekliyoruz
                .build();
    }

    // Bu metot DTO'dan Entity'ye dönüşüm yapar, ancak Kat nesnesini ayrıca alır.
    // Çünkü DTO'da sadece katId var, Kat nesnesini repository'den çekmemiz gerekiyor.
    private Oda convertToEntity(OdaDTO odaDTO, Kat kat) {
        if (odaDTO == null) {
            return null;
        }
        Oda oda = new Oda();
        oda.setId(odaDTO.getId()); // Güncelleme için ID gerekebilir
        oda.setOdaNumarasi(odaDTO.getOdaNumarasi());
        oda.setKapasite(odaDTO.getKapasite());
        oda.setKat(kat); // Kat nesnesini set ediyoruz
        // 'yataklar' alanı DTO'da olmadığı için burada set edilmiyor.
        return oda;
    }
    // --- Dönüşüm Metotları Sonu ---


    @Override
    @Transactional(readOnly = true)
    public List<OdaDTO> getAllOdalar() {
        log.info("Tüm odalar getiriliyor.");
        return odaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OdaDTO> getOdalarByKatId(Integer katId) {
        log.info("Kat ID {} için odalar getiriliyor.", katId);
        if (!katRepository.existsById(katId)) {
            throw new ResourceNotFoundException("Kat bulunamadı, ID: " + katId);
        }
        return odaRepository.findByKat_Id(katId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OdaDTO> getOdaById(Integer id) {
        log.info("Oda ID {} için detaylar getiriliyor.", id);
        return odaRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public OdaDTO createOda(OdaDTO odaDTO) {
        log.info("Yeni oda oluşturuluyor: {}", odaDTO.getOdaNumarasi());
        Kat kat = katRepository.findById(odaDTO.getKatId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda oluşturulacak kat bulunamadı, Kat ID: " + odaDTO.getKatId()));

        if (odaRepository.existsByKat_IdAndOdaNumarasi(kat.getId(), odaDTO.getOdaNumarasi())) {
            throw new IllegalArgumentException("'" + kat.getAd() + "' katında '" + odaDTO.getOdaNumarasi() + "' numaralı oda zaten mevcut.");
        }

        Oda oda = convertToEntity(odaDTO, kat);
        oda.setId(null); // Yeni kayıt için ID null olmalı
        Oda kaydedilmisOda = odaRepository.save(oda);
        log.info("Oda başarıyla oluşturuldu. ID: {}, Numara: {}", kaydedilmisOda.getId(), kaydedilmisOda.getOdaNumarasi());
        return convertToDTO(kaydedilmisOda);
    }

    @Override
    @Transactional
    public OdaDTO updateOda(Integer id, OdaDTO odaDTO) {
        log.info("Oda ID {} güncelleniyor. Yeni numara: {}", id, odaDTO.getOdaNumarasi());
        Oda mevcutOda = odaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek oda bulunamadı, ID: " + id));

        Kat kat;
        // Eğer DTO'da katId belirtilmişse ve mevcut odanın katından farklıysa, yeni katı al
        if (odaDTO.getKatId() != null && !mevcutOda.getKat().getId().equals(odaDTO.getKatId())) {
            kat = katRepository.findById(odaDTO.getKatId())
                    .orElseThrow(() -> new ResourceNotFoundException("Oda için belirtilen yeni kat bulunamadı, Kat ID: " + odaDTO.getKatId()));
            mevcutOda.setKat(kat);
        } else {
            kat = mevcutOda.getKat(); // Mevcut katı kullan
        }

        // Oda numarası değişmişse veya kat değişmişse benzersizlik kontrolü yap
        if ((odaDTO.getOdaNumarasi() != null && !mevcutOda.getOdaNumarasi().equalsIgnoreCase(odaDTO.getOdaNumarasi())) ||
            (odaDTO.getKatId() != null && !mevcutOda.getKat().getId().equals(odaDTO.getKatId()))) {
            
            String kontrolEdilecekOdaNumarasi = (odaDTO.getOdaNumarasi() != null) ? odaDTO.getOdaNumarasi() : mevcutOda.getOdaNumarasi();
            
            Optional<Oda> cakisanOda = odaRepository.findByKat_IdAndOdaNumarasi(kat.getId(), kontrolEdilecekOdaNumarasi);
            if (cakisanOda.isPresent() && !cakisanOda.get().getId().equals(mevcutOda.getId())) { // Başka bir odayla çakışıyorsa
                 throw new IllegalArgumentException("'" + kat.getAd() + "' katında '" + kontrolEdilecekOdaNumarasi + "' numaralı oda zaten mevcut.");
            }
        }
        
        if (odaDTO.getOdaNumarasi() != null) {
            mevcutOda.setOdaNumarasi(odaDTO.getOdaNumarasi());
        }
        if (odaDTO.getKapasite() != null) {
            mevcutOda.setKapasite(odaDTO.getKapasite());
        }
        
        Oda guncellenmisOda = odaRepository.save(mevcutOda);
        log.info("Oda başarıyla güncellendi. ID: {}, Numara: {}", guncellenmisOda.getId(), guncellenmisOda.getOdaNumarasi());
        return convertToDTO(guncellenmisOda);
    }

    @Override
    @Transactional
    public void deleteOda(Integer id) {
        log.info("Oda ID {} siliniyor.", id);
        Oda oda = odaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek oda bulunamadı, ID: " + id));

        // Oda entity'sindeki cascade ayarları (CascadeType.ALL ve orphanRemoval=true yataklar için)
        // bu oda silindiğinde ilişkili yatakların da silinmesini sağlayacaktır.
        // Eğer yatakların silinmesi istenmiyorsa veya dolu bir oda silinemez gibi kurallar varsa,
        // bu mantık burada (veya YatakService'te) ele alınmalıdır.
        // Örneğin: if (yatakRepository.existsByOda_IdAndDoluMuIsTrue(id)) { throw ... }
        odaRepository.delete(oda);
        log.info("Oda başarıyla silindi. ID: {}", id);
    }
}