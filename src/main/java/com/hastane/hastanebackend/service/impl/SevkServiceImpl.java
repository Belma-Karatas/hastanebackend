package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.SevkGoruntuleDTO;
import com.hastane.hastanebackend.dto.SevkOlusturDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.Sevk;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.SevkRepository;
import com.hastane.hastanebackend.service.SevkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; // YENİ IMPORT
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SevkServiceImpl implements SevkService {

    private static final Logger log = LoggerFactory.getLogger(SevkServiceImpl.class);

    private final SevkRepository sevkRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public SevkServiceImpl(SevkRepository sevkRepository,
                           HastaRepository hastaRepository,
                           PersonelRepository personelRepository,
                           KullaniciRepository kullaniciRepository) {
        this.sevkRepository = sevkRepository;
        this.hastaRepository = hastaRepository;
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public SevkGoruntuleDTO createSevk(SevkOlusturDTO dto, Integer sevkEdenDoktorKullaniciId) {
        log.info("Yeni sevk oluşturuluyor. Hasta ID: {}, Yapan Doktor Kullanıcı ID: {}", dto.getHastaId(), sevkEdenDoktorKullaniciId);

        Kullanici doktorKullanici = kullaniciRepository.findById(sevkEdenDoktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Sevk eden doktorun kullanıcı kaydı bulunamadı."));
        boolean isDoktor = doktorKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if (!isDoktor) {
            throw new AccessDeniedException("Sadece doktorlar sevk işlemi yapabilir.");
        }

        Personel sevkEdenDoktor = personelRepository.findByKullanici_Id(sevkEdenDoktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Sevk eden doktorun personel profili bulunamadı. Kullanıcı ID: " + sevkEdenDoktorKullaniciId));

        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> new ResourceNotFoundException("Sevk edilecek hasta bulunamadı, ID: " + dto.getHastaId()));

        Sevk sevk = new Sevk();
        sevk.setHasta(hasta);
        sevk.setSevkEdenDoktor(sevkEdenDoktor);
        sevk.setSevkTarihi(dto.getSevkTarihi());
        sevk.setHedefKurum(dto.getHedefKurum());
        sevk.setHedefServis(dto.getHedefServis());
        sevk.setSevkNedeni(dto.getSevkNedeni());
        sevk.setDurum("PLANLANDI");

        Sevk kaydedilmisSevk = sevkRepository.save(sevk);
        log.info("Sevk başarıyla oluşturuldu. Sevk ID: {}", kaydedilmisSevk.getId());
        return convertToGoruntuleDTO(kaydedilmisSevk);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SevkGoruntuleDTO> getSevkById(Integer sevkId, Integer talepEdenKullaniciId) {
        log.debug("getSevkById çağrıldı. Sevk ID: {}, Talep Eden Kullanıcı ID: {}", sevkId, talepEdenKullaniciId);
        return sevkRepository.findById(sevkId)
                .map(sevk -> {
                    checkSevkGoruntulemeYetkisi(sevk, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(sevk);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<SevkGoruntuleDTO> getSevklerByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        log.debug("getSevklerByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        
        Optional<Hasta> hastaProfili = hastaRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliHasta = hastaProfili.isPresent() && hastaProfili.get().getId().equals(hastaId);

        if (!isAdmin && !isIlgiliHasta) {
            throw new AccessDeniedException("Bu hastanın sevklerini görüntüleme yetkiniz yok.");
        }

        return sevkRepository.findByHasta_IdOrderBySevkTarihiDesc(hastaId).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SevkGoruntuleDTO> getSevklerByDoktorId(Integer doktorPersonelId, Integer talepEdenKullaniciId) {
        log.debug("getSevklerByDoktorId çağrıldı. Doktor Personel ID: {}, Talep Eden Kullanıcı ID: {}", doktorPersonelId, talepEdenKullaniciId);
        Personel doktor = personelRepository.findById(doktorPersonelId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, Personel ID: " + doktorPersonelId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isIlgiliDoktor = doktor.getKullanici() != null && doktor.getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu doktorun sevklerini görüntüleme yetkiniz yok.");
        }
        
        return sevkRepository.findBySevkEdenDoktor_IdOrderBySevkTarihiDesc(doktorPersonelId).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SevkGoruntuleDTO> getSevklerByDurum(String durum, Integer talepEdenKullaniciId) {
        log.debug("getSevklerByDurum çağrıldı. Durum: {}, Talep Eden Kullanıcı ID: {}", durum, talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "duruma göre sevkleri");

        return sevkRepository.findByDurumOrderBySevkTarihiDesc(durum.toUpperCase()).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SevkGoruntuleDTO> getAllSevkler(Integer talepEdenKullaniciId) {
        log.debug("getAllSevkler çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "tüm sevkleri");

        // DÜZELTİLMİŞ KISIM: findAll(Sort) kullanıldı
        List<Sevk> sevkler = sevkRepository.findAll(Sort.by(Sort.Direction.DESC, "sevkTarihi"));
        
        return sevkler.stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    // --- Helper Metotlar ---
    private SevkGoruntuleDTO convertToGoruntuleDTO(Sevk sevk) {
        if (sevk == null) return null;
        return SevkGoruntuleDTO.builder()
                .id(sevk.getId())
                .hastaId(sevk.getHasta().getId())
                .hastaAdiSoyadi(sevk.getHasta().getAd() + " " + sevk.getHasta().getSoyad())
                .sevkEdenDoktorId(sevk.getSevkEdenDoktor().getId())
                .sevkEdenDoktorAdiSoyadi(sevk.getSevkEdenDoktor().getAd() + " " + sevk.getSevkEdenDoktor().getSoyad())
                .sevkTarihi(sevk.getSevkTarihi())
                .hedefKurum(sevk.getHedefKurum())
                .hedefServis(sevk.getHedefServis())
                .sevkNedeni(sevk.getSevkNedeni())
                .durum(sevk.getDurum())
                .build();
    }

    private void checkSevkGoruntulemeYetkisi(Sevk sevk, Integer talepEdenKullaniciId) {
        if (sevk == null) return;

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isIlgiliHasta = sevk.getHasta().getKullanici() != null && sevk.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isSevkEdenDoktor = sevk.getSevkEdenDoktor().getKullanici() != null && sevk.getSevkEdenDoktor().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliHasta && !isSevkEdenDoktor) {
            throw new AccessDeniedException("Bu sevk kaydını görüntüleme yetkiniz yok.");
        }
    }
    
    private void checkGenelListeGoruntulemeYetkisi(Integer talepEdenKullaniciId, String listeTuru) {
         Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean yetkili = talepEden.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || rol.getAd().equals("ROLE_YONETICI"));
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + listeTuru + "' listesini görüntüleme yetkisi yok.");
        }
    }
}