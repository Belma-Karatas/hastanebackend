// src/main/java/com/hastane/hastanebackend/service/impl/YatakServiceImpl.java
package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.YatakDTO;
import com.hastane.hastanebackend.entity.Oda;
import com.hastane.hastanebackend.entity.Yatak; // Yatak entity'sini import ettiğinizden emin olun
import com.hastane.hastanebackend.entity.Yatis; // Yatis entity'sini import edin
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.OdaRepository;
import com.hastane.hastanebackend.repository.YatakRepository;
// YatisRepository'ye de ihtiyacımız olabilir, eğer yatak.getAktifYatis() her zaman dolu gelmiyorsa.
// Ancak Yatak entity'sindeki @OneToOne(mappedBy = "yatak", fetch = FetchType.LAZY) private Yatis aktifYatis;
// ilişkisi varsa ve doğru yönetiliyorsa, yatak üzerinden aktif yatışa ulaşabilmeliyiz.
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
    private final OdaRepository odaRepository;

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
        YatakDTO.YatakDTOBuilder builder = YatakDTO.builder()
                .id(yatak.getId())
                .yatakNumarasi(yatak.getYatakNumarasi())
                .doluMu(yatak.isDoluMu())
                .odaId(yatak.getOda().getId())
                .odaNumarasiOdaDto(yatak.getOda().getOdaNumarasi())
                .katAdi(yatak.getOda().getKat().getAd());

        // Yatak doluysa ve aktif bir yatış bilgisi varsa, DTO'ya ekle
        if (yatak.isDoluMu()) {
            Yatis aktifYatis = yatak.getAktifYatis(); // Yatak entity'sindeki ilişkiden aktif yatışı al
            if (aktifYatis != null) {
                builder.aktifYatisId(aktifYatis.getId());
                if (aktifYatis.getHasta() != null) {
                    builder.yatanHastaAdiSoyadi(aktifYatis.getHasta().getAd() + " " + aktifYatis.getHasta().getSoyad());
                } else {
                    builder.yatanHastaAdiSoyadi("Bilinmeyen Hasta (Veri Eksik)");
                }
            } else {
                // Yatak dolu ama aktif yatış bilgisi entity'de yoksa (bu bir veri tutarsızlığı olabilir)
                builder.yatanHastaAdiSoyadi("Bilinmiyor (Aktif Yatış Yok)");
                log.warn("Yatak ID {} dolu olarak işaretlenmiş ancak ilişkili aktif yatış bilgisi bulunamadı.", yatak.getId());
            }
        } else {
            builder.yatanHastaAdiSoyadi(null); // Boş yatakta yatan hasta olmaz
            builder.aktifYatisId(null);      // Boş yatakta aktif yatış ID'si olmaz
        }

        return builder.build();
    }

    // DTO'dan Entity'ye dönüşüm, Oda nesnesini ayrıca alır. (Bu metot aynı kalabilir)
    private Yatak convertToEntity(YatakDTO yatakDTO, Oda oda) {
        if (yatakDTO == null) {
            return null;
        }
        Yatak yatak = new Yatak();
        yatak.setId(yatakDTO.getId()); // Güncelleme için
        yatak.setYatakNumarasi(yatakDTO.getYatakNumarasi());
        // Yeni yatak oluşturulurken veya güncellenirken doluMu durumu YatakDTO'dan gelir.
        // createYatak içinde varsayılan olarak false set ediliyor, updateYatak içinde ise dokunulmuyor.
        // updateYatakDolulukDurumu metodu bu alanı ayrıca güncelliyor.
        yatak.setDoluMu(yatakDTO.getDoluMu() != null ? yatakDTO.getDoluMu() : false);
        yatak.setOda(oda);
        return yatak;
    }
    // --- Dönüşüm Metotları Sonu ---

    // ... (getAllYataklar, getYataklarByOdaId, getYatakById metotları aynı kalacak) ...
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
        // Yeni yatak oluşturulurken doluMu durumu frontend'den YatakDTO içinde gelmeli (veya varsayılan olarak false).
        // convertToEntity metodu yatakDTO.getDoluMu() null ise false atıyor.
        Yatak yatak = convertToEntity(yatakDTO, oda);
        yatak.setId(null); 
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
        if (yatakDTO.getOdaId() != null && !mevcutYatak.getOda().getId().equals(yatakDTO.getOdaId())) {
            if (mevcutYatak.isDoluMu()) {
                throw new IllegalStateException("Dolu bir yatağın odası değiştirilemez. Yatak ID: " + id);
            }
            hedefOda = odaRepository.findById(yatakDTO.getOdaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yatak için belirtilen yeni oda bulunamadı, Oda ID: " + yatakDTO.getOdaId()));
            mevcutYatak.setOda(hedefOda);
        } else {
            hedefOda = mevcutYatak.getOda();
        }

        if ((yatakDTO.getYatakNumarasi() != null && !mevcutYatak.getYatakNumarasi().equalsIgnoreCase(yatakDTO.getYatakNumarasi())) ||
            (yatakDTO.getOdaId() != null && !mevcutYatak.getOda().getId().equals(yatakDTO.getOdaId()))) {
            String kontrolEdilecekYatakNumarasi = (yatakDTO.getYatakNumarasi() != null) ? yatakDTO.getYatakNumarasi() : mevcutYatak.getYatakNumarasi();
            Optional<Yatak> cakisanYatak = yatakRepository.findByOda_IdAndYatakNumarasi(hedefOda.getId(), kontrolEdilecekYatakNumarasi);
            if (cakisanYatak.isPresent() && !cakisanYatak.get().getId().equals(mevcutYatak.getId())) {
                 throw new IllegalArgumentException("'" + hedefOda.getOdaNumarasi() + "' numaralı odada '" + kontrolEdilecekYatakNumarasi + "' numaralı yatak zaten mevcut.");
            }
        }
        
        if (yatakDTO.getYatakNumarasi() != null) {
            mevcutYatak.setYatakNumarasi(yatakDTO.getYatakNumarasi());
        }
        // doluMu durumu burada güncellenmiyor, updateYatakDolulukDurumu metodu bu iş için var.
        // Eğer yatakDTO.getDoluMu() dolu geliyorsa ve adminin bu yetkisi varsa, o zaman güncellenebilir,
        // ancak YatisService ile senkronizasyon önemlidir. Şimdilik bu genel update'te doluMu'ya dokunmuyoruz.
        // Sadece YatakDTO'dan gelen doluMu değerini, convertToEntity içindeki gibi null kontrolüyle atayabiliriz
        // eğer yatakDTO'da doluMu değeri her zaman dolu geliyorsa.
        // Ancak en güvenlisi, doluMu durumunu sadece updateYatakDolulukDurumu ile değiştirmek.
        // Bu yüzden convertToEntity'deki gibi yatakDTO.getDoluMu() null değilse onu kullanırız.
        if (yatakDTO.getDoluMu() != null) {
            // Bu satır, YatakDTO'da doluMu gelirse onu set eder. Ancak YatisService ile çakışabilir.
            // mevcutYatak.setDoluMu(yatakDTO.getDoluMu()); 
            log.info("updateYatak çağrısında YatakDTO.doluMu: {} (Yatak ID: {})", yatakDTO.getDoluMu(), id);
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
        
        if (yatak.isDoluMu() == doluMu) {
            log.warn("Yatak ID {} zaten istenen doluluk durumunda ({})", yatakId, doluMu);
            // return convertToDTO(yatak); // Aynı durumda ise bir şey yapmadan dön
        }
        
        yatak.setDoluMu(doluMu);
        // Eğer yatak boşaltılıyorsa, ilişkili aktif yatışı da null yapmalıyız
        // Bu, YatisService.hastaTaburcuEt içinde yapılmalı.
        // Burada sadece yatağın flag'ini güncelliyoruz.
        // YatisService bu metodu çağırırken yatak.setAktifYatis(null) yapabilir.
        if (!doluMu && yatak.getAktifYatis() != null) {
            log.info("Yatak ID {} boşaltılıyor, aktif yatış ilişkisi YatisService tarafından yönetilmeli.", yatakId);
            // yatak.setAktifYatis(null); // Bu satır YatisService.hastaTaburcuEt içinde olmalı
        }

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

        if (yatak.isDoluMu()) {
            throw new IllegalStateException("Dolu bir yatak silinemez. Lütfen önce yatıştaki hastayı taburcu edin. Yatak ID: " + id);
        }
        // TODO: Yatak silinirken ilişkili (geçmiş) yatış kayıtları ne olacak? Bu iş kuralına göre belirlenmeli.
        // Şimdilik sadece yatağı siliyoruz.

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