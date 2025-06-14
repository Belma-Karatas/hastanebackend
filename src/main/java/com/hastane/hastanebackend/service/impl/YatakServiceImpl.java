package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.YatakDTO;
import com.hastane.hastanebackend.entity.Oda;
import com.hastane.hastanebackend.entity.Yatak;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.OdaRepository;
import com.hastane.hastanebackend.repository.YatakRepository;
import com.hastane.hastanebackend.service.YatakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class YatakServiceImpl implements YatakService {

    private static final Logger log = LoggerFactory.getLogger(YatakServiceImpl.class);

    private final YatakRepository yatakRepository;
    private final OdaRepository odaRepository; // Oda bilgisini çekmek ve doğrulamak için

    @Autowired
    public YatakServiceImpl(YatakRepository yatakRepository, OdaRepository odaRepository) {
        this.yatakRepository = yatakRepository;
        this.odaRepository = odaRepository;
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private YatakDTO convertToDTO(Yatak yatak) {
        if (yatak == null) {
            return null;
        }
        return YatakDTO.builder()
                .id(yatak.getId())
                .yatakNumarasi(yatak.getYatakNumarasi())
                .doluMu(yatak.isDoluMu())
                .odaId(yatak.getOda().getId())
                .odaNumarasiOdaDto(yatak.getOda().getOdaNumarasi())
                .katAdi(yatak.getOda().getKat().getAd())
                .build();
    }

    // DTO'dan Entity'ye dönüşüm, Oda nesnesini ayrıca alır.
    private Yatak convertToEntity(YatakDTO yatakDTO, Oda oda) {
        if (yatakDTO == null) {
            return null;
        }
        Yatak yatak = new Yatak();
        yatak.setId(yatakDTO.getId()); // Güncelleme için
        yatak.setYatakNumarasi(yatakDTO.getYatakNumarasi());
        yatak.setDoluMu(yatakDTO.getDoluMu() != null ? yatakDTO.getDoluMu() : false); // Null ise false ata
        yatak.setOda(oda);
        return yatak;
    }
    // --- Dönüşüm Metotları Sonu ---

    @Override
    @Transactional(readOnly = true)
    public List<YatakDTO> getAllYataklar() {
        log.info("Tüm yataklar getiriliyor.");
        return yatakRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<YatakDTO> getYataklarByOdaId(Integer odaId) {
        log.info("Oda ID {} için yataklar getiriliyor.", odaId);
        if (!odaRepository.existsById(odaId)) {
            throw new ResourceNotFoundException("Oda bulunamadı, ID: " + odaId);
        }
        return yatakRepository.findByOda_Id(odaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<YatakDTO> getYatakById(Integer id) {
        log.info("Yatak ID {} için detaylar getiriliyor.", id);
        return yatakRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public YatakDTO createYatak(YatakDTO yatakDTO) {
        log.info("Yeni yatak oluşturuluyor. Oda ID: {}, Yatak No: {}", yatakDTO.getOdaId(), yatakDTO.getYatakNumarasi());
        Oda oda = odaRepository.findById(yatakDTO.getOdaId())
                .orElseThrow(() -> new ResourceNotFoundException("Yatak oluşturulacak oda bulunamadı, Oda ID: " + yatakDTO.getOdaId()));

        if (yatakRepository.existsByOda_IdAndYatakNumarasi(oda.getId(), yatakDTO.getYatakNumarasi())) {
            throw new IllegalArgumentException("'" + oda.getOdaNumarasi() + "' numaralı odada '" + yatakDTO.getYatakNumarasi() + "' numaralı yatak zaten mevcut.");
        }

        Yatak yatak = convertToEntity(yatakDTO, oda);
        yatak.setId(null); // Yeni kayıt için ID null olmalı
        Yatak kaydedilmisYatak = yatakRepository.save(yatak);
        log.info("Yatak başarıyla oluşturuldu. ID: {}, Numara: {}", kaydedilmisYatak.getId(), kaydedilmisYatak.getYatakNumarasi());
        return convertToDTO(kaydedilmisYatak);
    }

    @Override
    @Transactional
    public YatakDTO updateYatak(Integer id, YatakDTO yatakDTO) {
        log.info("Yatak ID {} güncelleniyor. Yeni Yatak No: {}", id, yatakDTO.getYatakNumarasi());
        Yatak mevcutYatak = yatakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek yatak bulunamadı, ID: " + id));

        Oda hedefOda;
        // Eğer DTO'da odaId belirtilmişse ve mevcut yatağın odasından farklıysa, yeni odayı al
        if (yatakDTO.getOdaId() != null && !mevcutYatak.getOda().getId().equals(yatakDTO.getOdaId())) {
            // İş kuralı: Dolu bir yatağın odası değiştirilemez.
            if (mevcutYatak.isDoluMu()) {
                throw new IllegalStateException("Dolu bir yatağın odası değiştirilemez. Yatak ID: " + id);
            }
            hedefOda = odaRepository.findById(yatakDTO.getOdaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yatak için belirtilen yeni oda bulunamadı, Oda ID: " + yatakDTO.getOdaId()));
            mevcutYatak.setOda(hedefOda);
        } else {
            hedefOda = mevcutYatak.getOda(); // Mevcut odayı kullan
        }

        // Yatak numarası değişmişse veya oda değişmişse benzersizlik kontrolü yap
        if ((yatakDTO.getYatakNumarasi() != null && !mevcutYatak.getYatakNumarasi().equalsIgnoreCase(yatakDTO.getYatakNumarasi())) ||
            (yatakDTO.getOdaId() != null && !mevcutYatak.getOda().getId().equals(yatakDTO.getOdaId()))) {
            
            String kontrolEdilecekYatakNumarasi = (yatakDTO.getYatakNumarasi() != null) ? yatakDTO.getYatakNumarasi() : mevcutYatak.getYatakNumarasi();
            
            Optional<Yatak> cakisanYatak = yatakRepository.findByOda_IdAndYatakNumarasi(hedefOda.getId(), kontrolEdilecekYatakNumarasi);
            if (cakisanYatak.isPresent() && !cakisanYatak.get().getId().equals(mevcutYatak.getId())) { // Başka bir yatakla çakışıyorsa
                 throw new IllegalArgumentException("'" + hedefOda.getOdaNumarasi() + "' numaralı odada '" + kontrolEdilecekYatakNumarasi + "' numaralı yatak zaten mevcut.");
            }
        }
        
        if (yatakDTO.getYatakNumarasi() != null) {
            mevcutYatak.setYatakNumarasi(yatakDTO.getYatakNumarasi());
        }
        // Doluluk durumu genellikle bu genel update metoduyla değil, özel bir metotla (updateYatakDolulukDurumu) güncellenir.
        // Ama DTO'da geliyorsa ve izin veriliyorsa güncellenebilir.
        if (yatakDTO.getDoluMu() != null) {
             // İş kuralı: Yatak doluysa ve boşaltılmaya çalışılıyorsa Yatis işlemi gerekir.
             // Ya da yatak boşsa ve doldurulmaya çalışılıyorsa Yatis işlemi gerekir.
             // Bu metot sadece ADMIN tarafından temel yatak bilgilerini güncellemek için kullanılmalı.
             // Doluluk durumu Yatis servisi tarafından yönetilmeli.
             // Şimdilik direkt güncellemeyi yoruma alıyorum, ayrı metotta yapılacak.
             // mevcutYatak.setDoluMu(yatakDTO.getDoluMu());
             log.warn("Yatak doluluk durumu bu genel güncelleme metoduyla değiştirilmemeli. ID: {}", id);
        }
        
        Yatak guncellenmisYatak = yatakRepository.save(mevcutYatak);
        log.info("Yatak başarıyla güncellendi. ID: {}, Numara: {}", guncellenmisYatak.getId(), guncellenmisYatak.getYatakNumarasi());
        return convertToDTO(guncellenmisYatak);
    }

    @Override
    @Transactional
    public YatakDTO updateYatakDolulukDurumu(Integer yatakId, boolean doluMu) {
        log.info("Yatak ID {} doluluk durumu güncelleniyor: {}", yatakId, doluMu);
        Yatak yatak = yatakRepository.findById(yatakId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatak bulunamadı, ID: " + yatakId));
        
        // İş kuralı: Yatak zaten istenen durumdaysa bir şey yapma veya hata ver.
        if (yatak.isDoluMu() == doluMu) {
            log.warn("Yatak ID {} zaten istenen doluluk durumunda ({})", yatakId, doluMu);
            // return convertToDTO(yatak); // Veya exception fırlatılabilir.
        }
        
        // TODO: Eğer yatak 'dolu' yapılıyorsa, bir Yatis kaydı ile ilişkilendirilmelidir.
        // Eğer yatak 'boş' yapılıyorsa, ilişkili Yatis kaydı sonlandırılmalıdır.
        // Bu mantık YatisService içinde olmalı. Bu metot sadece yatağın flag'ini değiştirir.
        // Daha gelişmiş bir sistemde bu metot YatisService tarafından çağrılabilir.
        yatak.setDoluMu(doluMu);
        Yatak guncellenmisYatak = yatakRepository.save(yatak);
        log.info("Yatak ID {} doluluk durumu başarıyla güncellendi: {}", yatakId, doluMu);
        return convertToDTO(guncellenmisYatak);
    }

    @Override
    @Transactional
    public void deleteYatak(Integer id) {
        log.info("Yatak ID {} siliniyor.", id);
        Yatak yatak = yatakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek yatak bulunamadı, ID: " + id));

        // İş kuralı: Dolu bir yatak silinemez.
        if (yatak.isDoluMu()) {
            throw new IllegalStateException("Dolu bir yatak silinemez. Lütfen önce yatıştaki hastayı taburcu edin. Yatak ID: " + id);
        }
        // TODO: Yatis entity'si eklendiğinde, yatağın aktif bir yatışla ilişkili olup olmadığı kontrol edilmeli.
        // if (yatisRepository.existsByYatak_IdAndCikisTarihiIsNull(id)) { throw ... }

        yatakRepository.delete(yatak);
        log.info("Yatak başarıyla silindi. ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<YatakDTO> getBosYataklarByOdaId(Integer odaId) {
        log.info("Oda ID {} için boş yataklar getiriliyor.", odaId);
        if (!odaRepository.existsById(odaId)) {
            throw new ResourceNotFoundException("Oda bulunamadı, ID: " + odaId);
        }
        return yatakRepository.findByOda_IdAndDoluMuFalse(odaId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<YatakDTO> getTumBosYataklar() {
        log.info("Tüm boş yataklar getiriliyor.");
        return yatakRepository.findByDoluMuFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}