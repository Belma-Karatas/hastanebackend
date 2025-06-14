package com.hastane.hastanebackend.service.impl;
import org.springframework.data.domain.Sort;
import com.hastane.hastanebackend.dto.AcilDurumKaydiGuncelleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiGoruntuleDTO;
import com.hastane.hastanebackend.dto.AcilDurumKaydiOlusturDTO;
import com.hastane.hastanebackend.entity.AcilDurumKaydi;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.AcilDurumKaydiRepository;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.service.AcilDurumKaydiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AcilDurumKaydiServiceImpl implements AcilDurumKaydiService {

    private static final Logger log = LoggerFactory.getLogger(AcilDurumKaydiServiceImpl.class);

    private final AcilDurumKaydiRepository acilDurumKaydiRepository;
    private final PersonelRepository personelRepository;
    private final HastaRepository hastaRepository;
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public AcilDurumKaydiServiceImpl(AcilDurumKaydiRepository acilDurumKaydiRepository,
                                     PersonelRepository personelRepository,
                                     HastaRepository hastaRepository,
                                     KullaniciRepository kullaniciRepository) {
        this.acilDurumKaydiRepository = acilDurumKaydiRepository;
        this.personelRepository = personelRepository;
        this.hastaRepository = hastaRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public AcilDurumKaydiGoruntuleDTO createAcilDurumKaydi(AcilDurumKaydiOlusturDTO dto, Integer tetikleyenHemsireKullaniciId) {
        log.info("Yeni acil durum kaydı oluşturuluyor. Tetikleyen Kullanıcı ID: {}", tetikleyenHemsireKullaniciId);

        Kullanici hemsireKullanici = kullaniciRepository.findById(tetikleyenHemsireKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Tetikleyen hemşirenin kullanıcı kaydı bulunamadı."));
        boolean isHemsire = hemsireKullanici.getRoller().stream().anyMatch(rol -> "ROLE_HEMSIRE".equals(rol.getAd()));
        if (!isHemsire) {
            throw new AccessDeniedException("Sadece hemşireler acil durum kaydı oluşturabilir.");
        }

        Personel tetikleyenPersonel = personelRepository.findByKullanici_Id(tetikleyenHemsireKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Acil durumu tetikleyen hemşire personel profili bulunamadı. Kullanıcı ID: " + tetikleyenHemsireKullaniciId));

        Hasta hasta = null;
        if (dto.getHastaId() != null) {
            hasta = hastaRepository.findById(dto.getHastaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Acil durumla ilişkili hasta bulunamadı, ID: " + dto.getHastaId()));
        }

        AcilDurumKaydi kayit = new AcilDurumKaydi();
        kayit.setAciklama(dto.getAciklama());
        kayit.setKonum(dto.getKonum());
        kayit.setOlayZamani(dto.getOlayZamani());
        kayit.setTetikleyenPersonel(tetikleyenPersonel);
        kayit.setHasta(hasta); // Null olabilir
        kayit.setDurum("AKTIF"); // Varsayılan durum

        AcilDurumKaydi kaydedilmisKayit = acilDurumKaydiRepository.save(kayit);
        log.info("Acil durum kaydı başarıyla oluşturuldu. Kayıt ID: {}", kaydedilmisKayit.getId());
        return convertToGoruntuleDTO(kaydedilmisKayit);
    }

    @Override
    @Transactional
    public AcilDurumKaydiGoruntuleDTO updateAcilDurumKaydiDurumu(Integer kayitId, AcilDurumKaydiGuncelleDTO guncelleDTO, Integer yapanKullaniciId) {
        log.info("Acil durum kaydı (ID: {}) durumu güncelleniyor. Yeni Durum: {}, Yapan Kullanıcı ID: {}", kayitId, guncelleDTO.getYeniDurum(), yapanKullaniciId);
        
        AcilDurumKaydi kayit = acilDurumKaydiRepository.findById(kayitId)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek acil durum kaydı bulunamadı, ID: " + kayitId));

        checkAcilDurumIslemYetkisi(yapanKullaniciId, "acil durum durumu güncelleme", kayit);

        // Durum geçiş mantığı (opsiyonel)
        // Örneğin: SONLANDIRILDI ise tekrar AKTIF yapılamaz gibi.
        if (kayit.getDurum().equals("SONLANDIRILDI") && !guncelleDTO.getYeniDurum().equals("SONLANDIRILDI")) {
            throw new IllegalArgumentException("Sonlandırılmış bir acil durumun durumu değiştirilemez.");
        }

        kayit.setDurum(guncelleDTO.getYeniDurum());
        // if (guncelleDTO.getMudahaleNotlari() != null) { kayit.setMudahaleNotlari(guncelleDTO.getMudahaleNotlari()); }
        
        AcilDurumKaydi guncellenmisKayit = acilDurumKaydiRepository.save(kayit);
        log.info("Acil durum kaydı (ID: {}) durumu başarıyla '{}' olarak güncellendi.", guncellenmisKayit.getId(), guncellenmisKayit.getDurum());
        return convertToGoruntuleDTO(guncellenmisKayit);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AcilDurumKaydiGoruntuleDTO> getAcilDurumKaydiById(Integer kayitId, Integer talepEdenKullaniciId) {
        log.debug("getAcilDurumKaydiById çağrıldı. Kayıt ID: {}, Talep Eden Kullanıcı ID: {}", kayitId, talepEdenKullaniciId);
        return acilDurumKaydiRepository.findById(kayitId)
                .map(kayit -> {
                    checkAcilDurumGoruntulemeYetkisi(kayit, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(kayit);
                });
    }

       @Override
    @Transactional(readOnly = true)
    public List<AcilDurumKaydiGoruntuleDTO> getAllAcilDurumKayitlari(Integer talepEdenKullaniciId) {
        log.debug("getAllAcilDurumKayitlari çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "tüm acil durum kayıtlarını");

        // DÜZELTİLMİŞ KISIM: findAll(Sort) kullanıldı
        List<AcilDurumKaydi> kayitlar = acilDurumKaydiRepository.findAll(Sort.by(Sort.Direction.DESC, "olayZamani"));
        
        return kayitlar.stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByDurum(String durum, Integer talepEdenKullaniciId) {
        log.debug("getAcilDurumKayitlariByDurum çağrıldı. Durum: {}, Talep Eden Kullanıcı ID: {}", durum, talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "duruma göre acil durum kayıtlarını");
        return acilDurumKaydiRepository.findByDurumOrderByOlayZamaniDesc(durum.toUpperCase()).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        log.debug("getAcilDurumKayitlariByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        // Yetki: ADMIN, ilgili hasta, ilgili hastanın doktoru, ilgili hastaya bakan hemşire
         List<AcilDurumKaydi> kayitlar = acilDurumKaydiRepository.findByHasta_IdOrderByOlayZamaniDesc(hastaId);
         kayitlar.forEach(kayit -> checkAcilDurumGoruntulemeYetkisi(kayit, talepEdenKullaniciId)); // Her kayıt için yetki kontrolü
        return kayitlar.stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcilDurumKaydiGoruntuleDTO> getAcilDurumKayitlariByTarih(LocalDate tarih, Integer talepEdenKullaniciId) {
        log.debug("getAcilDurumKayitlariByTarih çağrıldı. Tarih: {}, Talep Eden Kullanıcı ID: {}", tarih, talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, tarih + " tarihli acil durum kayıtlarını");
        LocalDateTime baslangic = tarih.atStartOfDay();
        LocalDateTime bitis = tarih.atTime(LocalTime.MAX);
        return acilDurumKaydiRepository.findByOlayZamaniBetweenOrderByOlayZamaniDesc(baslangic, bitis).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }


    // --- Helper Metotlar ---
    private AcilDurumKaydiGoruntuleDTO convertToGoruntuleDTO(AcilDurumKaydi kayit) {
        if (kayit == null) return null;

        AcilDurumKaydiGoruntuleDTO.AcilDurumKaydiGoruntuleDTOBuilder builder = AcilDurumKaydiGoruntuleDTO.builder()
                .id(kayit.getId())
                .aciklama(kayit.getAciklama())
                .konum(kayit.getKonum())
                .olayZamani(kayit.getOlayZamani())
                .durum(kayit.getDurum())
                .tetikleyenPersonelId(kayit.getTetikleyenPersonel().getId())
                .tetikleyenPersonelAdiSoyadi(kayit.getTetikleyenPersonel().getAd() + " " + kayit.getTetikleyenPersonel().getSoyad());

        if (kayit.getHasta() != null) {
            builder.hastaId(kayit.getHasta().getId());
            builder.hastaAdiSoyadi(kayit.getHasta().getAd() + " " + kayit.getHasta().getSoyad());
        }
        return builder.build();
    }

    private void checkAcilDurumIslemYetkisi(Integer yapanKullaniciId, String islemTuru, AcilDurumKaydi kayitContext) {
        Kullanici yapanKullanici = kullaniciRepository.findById(yapanKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı."));
        
        boolean yetkili = yapanKullanici.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || 
                             rol.getAd().equals("ROLE_DOKTOR") || // Doktorlar durumu güncelleyebilir
                             rol.getAd().equals("ROLE_HEMSIRE")); // Hemşireler de durumu güncelleyebilir (örn: MÜDAHALE EDİLİYOR)
        
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok.");
        }
        // Daha detaylı kontrol: örn; sadece olayı tetikleyen hemşire veya ilgili doktor durumu güncelleyebilir.
    }

    private void checkAcilDurumGoruntulemeYetkisi(AcilDurumKaydi kayit, Integer talepEdenKullaniciId) {
        if (kayit == null) return;

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isTetikleyenPersonel = kayit.getTetikleyenPersonel().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isIlgiliHasta = kayit.getHasta() != null && kayit.getHasta().getKullanici() != null &&
                                kayit.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        
        // Doktorlar tüm acil durumları görebilir mi, yoksa sadece kendi hastalarınınkini mi?
        // Şimdilik doktorların da görmesine izin verelim.
        boolean isDoktor = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));


        if (!isAdmin && !isTetikleyenPersonel && !isIlgiliHasta && !isDoktor) {
            throw new AccessDeniedException("Bu acil durum kaydını görüntüleme yetkiniz yok.");
        }
    }

    private void checkGenelListeGoruntulemeYetkisi(Integer talepEdenKullaniciId, String listeTuru) {
         Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean yetkili = talepEden.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || 
                             rol.getAd().equals("ROLE_YONETICI") ||
                             rol.getAd().equals("ROLE_DOKTOR") || // Doktorlar da genel listeyi görebilir
                             rol.getAd().equals("ROLE_HEMSIRE")); // Hemşireler de genel listeyi görebilir
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + listeTuru + "' listesini görüntüleme yetkisi yok.");
        }
    }
}