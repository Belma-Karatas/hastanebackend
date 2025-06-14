package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.IzinTalepDurumGuncelleDTO;
import com.hastane.hastanebackend.dto.IzinTalepGoruntuleDTO;
import com.hastane.hastanebackend.dto.IzinTalepOlusturDTO;
import com.hastane.hastanebackend.entity.IzinTalep;
import com.hastane.hastanebackend.entity.IzinTuru;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.IzinTalepRepository;
import com.hastane.hastanebackend.repository.IzinTuruRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.service.IzinTalepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IzinTalepServiceImpl implements IzinTalepService {

    private static final Logger log = LoggerFactory.getLogger(IzinTalepServiceImpl.class);

    private final IzinTalepRepository izinTalepRepository;
    private final PersonelRepository personelRepository;
    private final IzinTuruRepository izinTuruRepository;
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public IzinTalepServiceImpl(IzinTalepRepository izinTalepRepository,
                                PersonelRepository personelRepository,
                                IzinTuruRepository izinTuruRepository,
                                KullaniciRepository kullaniciRepository) {
        this.izinTalepRepository = izinTalepRepository;
        this.personelRepository = personelRepository;
        this.izinTuruRepository = izinTuruRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public IzinTalepGoruntuleDTO createIzinTalep(IzinTalepOlusturDTO dto, Integer talepEdenKullaniciId) {
        log.info("Yeni izin talebi oluşturuluyor. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);

        Personel talepEdenPersonel = personelRepository.findByKullanici_Id(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İzin talebinde bulunan personel profili bulunamadı. Kullanıcı ID: " + talepEdenKullaniciId));

        IzinTuru izinTuru = izinTuruRepository.findById(dto.getIzinTuruId())
                .orElseThrow(() -> new ResourceNotFoundException("İzin türü bulunamadı, ID: " + dto.getIzinTuruId()));

        if (dto.getBaslangicTarihi().isAfter(dto.getBitisTarihi())) {
            throw new IllegalArgumentException("Başlangıç tarihi, bitiş tarihinden sonra olamaz.");
        }

        long hesaplananGunSayisi = ChronoUnit.DAYS.between(dto.getBaslangicTarihi(), dto.getBitisTarihi()) + 1;
        if (dto.getGunSayisi() != hesaplananGunSayisi) {
            // Veya loglayıp dto.getGunSayisi() yerine hesaplananGunSayisi kullanılabilir.
            // Şimdilik hata fırlatalım.
            log.warn("DTO'dan gelen gün sayısı ({}) ile hesaplanan gün sayısı ({}) eşleşmiyor. Personel: {}", dto.getGunSayisi(), hesaplananGunSayisi, talepEdenPersonel.getId());
            // throw new IllegalArgumentException("Belirtilen gün sayısı, başlangıç ve bitiş tarihleriyle uyuşmuyor.");
            // Ya da DTO'daki gunSayisi'ni yok sayıp hesaplananı kullanabiliriz:
            // dto.setGunSayisi((int) hesaplananGunSayisi);
        }
        
        // Tarih çakışması kontrolü
        List<IzinTalep> cakisanIzinler = izinTalepRepository.findCakisanIzinler(
                talepEdenPersonel.getId(), dto.getBaslangicTarihi(), dto.getBitisTarihi());
        if (!cakisanIzinler.isEmpty()) {
            throw new IllegalArgumentException("Belirtilen tarih aralığında zaten onaylanmış veya bekleyen bir izniniz bulunmaktadır.");
        }


        IzinTalep izinTalep = new IzinTalep();
        izinTalep.setTalepEdenPersonel(talepEdenPersonel);
        izinTalep.setIzinTuru(izinTuru);
        izinTalep.setBaslangicTarihi(dto.getBaslangicTarihi());
        izinTalep.setBitisTarihi(dto.getBitisTarihi());
        izinTalep.setGunSayisi(dto.getGunSayisi()); // Ya da hesaplanan gün sayısı
        izinTalep.setAciklama(dto.getAciklama());
        izinTalep.setDurum("BEKLIYOR"); // Varsayılan durum
        // talepTarihi @CreationTimestamp ile otomatik set edilecek

        IzinTalep kaydedilmisTalep = izinTalepRepository.save(izinTalep);
        log.info("İzin talebi başarıyla oluşturuldu. Talep ID: {}", kaydedilmisTalep.getId());
        return convertToGoruntuleDTO(kaydedilmisTalep);
    }

    @Override
    @Transactional
    public IzinTalepGoruntuleDTO updateIzinTalepDurumu(Integer izinTalepId, IzinTalepDurumGuncelleDTO durumGuncelleDTO, Integer onaylayanKullaniciId) {
        log.info("İzin talebi durumu güncelleniyor. Talep ID: {}, Yeni Durum: {}, Onaylayan Kullanıcı ID: {}", izinTalepId, durumGuncelleDTO.getYeniDurum(), onaylayanKullaniciId);

        checkAdminYetkisi(onaylayanKullaniciId, "izin talebi durumu güncelleme"); // Sadece ADMIN yapabilir

        IzinTalep izinTalep = izinTalepRepository.findById(izinTalepId)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek izin talebi bulunamadı, ID: " + izinTalepId));

        if (!izinTalep.getDurum().equals("BEKLIYOR")) {
            throw new IllegalStateException("Sadece 'BEKLIYOR' durumundaki izin talepleri güncellenebilir. Mevcut durum: " + izinTalep.getDurum());
        }

        Personel onaylayanYonetici = personelRepository.findByKullanici_Id(onaylayanKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Onaylayan yönetici (personel profili) bulunamadı. Kullanıcı ID: " + onaylayanKullaniciId));

        izinTalep.setDurum(durumGuncelleDTO.getYeniDurum());
        izinTalep.setOnaylayanYonetici(onaylayanYonetici);
        izinTalep.setOnayTarihi(LocalDateTime.now());
        // TODO: Eğer reddedildiyse ve DTO'da yoneticiAciklamasi varsa onu da kaydet.

        IzinTalep guncellenmisTalep = izinTalepRepository.save(izinTalep);
        log.info("İzin talebi (ID: {}) durumu başarıyla '{}' olarak güncellendi.", guncellenmisTalep.getId(), guncellenmisTalep.getDurum());
        return convertToGoruntuleDTO(guncellenmisTalep);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IzinTalepGoruntuleDTO> getIzinTalepById(Integer izinTalepId, Integer talepEdenKullaniciId) {
        log.debug("getIzinTalepById çağrıldı. Talep ID: {}, Talep Eden Kullanıcı ID: {}", izinTalepId, talepEdenKullaniciId);
        return izinTalepRepository.findById(izinTalepId)
                .map(izinTalep -> {
                    checkIzinTalepGoruntulemeYetkisi(izinTalep, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(izinTalep);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<IzinTalepGoruntuleDTO> getIzinTalepleriByPersonel(Integer personelKullaniciId, Integer talepEdenKullaniciId) {
        log.debug("getIzinTalepleriByPersonel çağrıldı. Personel Kullanıcı ID: {}, Talep Eden Kullanıcı ID: {}", personelKullaniciId, talepEdenKullaniciId);

        Personel talepSahibiPersonel = personelRepository.findByKullanici_Id(personelKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı. Kullanıcı ID: " + personelKullaniciId));
        
        // Yetki: Ya admin istiyor ya da kişi kendi taleplerini istiyor
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));

        if (!isAdmin && !talepEdenKullaniciId.equals(personelKullaniciId)) {
            throw new AccessDeniedException("Bu personelin izin taleplerini görüntüleme yetkiniz yok.");
        }

        return izinTalepRepository.findByTalepEdenPersonel_IdOrderByTalepTarihiDesc(talepSahibiPersonel.getId()).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IzinTalepGoruntuleDTO> getIzinTalepleriByDurum(String durum, Integer adminKullaniciId) {
        log.debug("getIzinTalepleriByDurum çağrıldı. Durum: {}, Admin Kullanıcı ID: {}", durum, adminKullaniciId);
        checkAdminYetkisi(adminKullaniciId, "duruma göre izin taleplerini listeleme");
        return izinTalepRepository.findByDurumOrderByTalepTarihiDesc(durum).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IzinTalepGoruntuleDTO> getAllIzinTalepleri(Integer adminKullaniciId) {
        log.debug("getAllIzinTalepleri çağrıldı. Admin Kullanıcı ID: {}", adminKullaniciId);
        checkAdminYetkisi(adminKullaniciId, "tüm izin taleplerini listeleme");
        return izinTalepRepository.findAll().stream() // İsteğe bağlı olarak sıralama eklenebilir
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    // --- Helper Metotlar ---
    private IzinTalepGoruntuleDTO convertToGoruntuleDTO(IzinTalep izinTalep) {
        if (izinTalep == null) return null;

        IzinTalepGoruntuleDTO.IzinTalepGoruntuleDTOBuilder builder = IzinTalepGoruntuleDTO.builder()
                .id(izinTalep.getId())
                .talepEdenPersonelId(izinTalep.getTalepEdenPersonel().getId())
                .talepEdenPersonelAdiSoyadi(izinTalep.getTalepEdenPersonel().getAd() + " " + izinTalep.getTalepEdenPersonel().getSoyad())
                .izinTuruId(izinTalep.getIzinTuru().getId())
                .izinTuruAdi(izinTalep.getIzinTuru().getAd())
                .baslangicTarihi(izinTalep.getBaslangicTarihi())
                .bitisTarihi(izinTalep.getBitisTarihi())
                .gunSayisi(izinTalep.getGunSayisi())
                .aciklama(izinTalep.getAciklama())
                .talepTarihi(izinTalep.getTalepTarihi())
                .durum(izinTalep.getDurum())
                .onayTarihi(izinTalep.getOnayTarihi());

        if (izinTalep.getOnaylayanYonetici() != null) {
            builder.onaylayanYoneticiId(izinTalep.getOnaylayanYonetici().getId());
            builder.onaylayanYoneticiAdiSoyadi(izinTalep.getOnaylayanYonetici().getAd() + " " + izinTalep.getOnaylayanYonetici().getSoyad());
        }
        return builder.build();
    }
    
    private void checkAdminYetkisi(Integer kullaniciId, String islemTuru) {
        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı."));
        boolean isAdmin = kullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        if (!isAdmin) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok (Sadece ADMIN).");
        }
    }

    private void checkIzinTalepGoruntulemeYetkisi(IzinTalep izinTalep, Integer talepEdenKullaniciId) {
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isTalepSahibi = izinTalep.getTalepEdenPersonel().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isTalepSahibi) {
            throw new AccessDeniedException("Bu izin talebini görüntüleme yetkiniz yok.");
        }
    }
}