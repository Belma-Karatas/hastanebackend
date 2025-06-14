package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.AtanmisHemsireDTO; // Yeni DTO için import
import com.hastane.hastanebackend.dto.HemsireAtaDTO;    // Yeni DTO için import
import com.hastane.hastanebackend.dto.YatisGoruntuleDTO;
import com.hastane.hastanebackend.dto.YatisOlusturDTO;
import com.hastane.hastanebackend.entity.*;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.*;
import com.hastane.hastanebackend.service.YatakService;
import com.hastane.hastanebackend.service.YatisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections; // Boş liste için
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class YatisServiceImpl implements YatisService {

    private static final Logger log = LoggerFactory.getLogger(YatisServiceImpl.class);

    private final YatisRepository yatisRepository;
    private final HastaRepository hastaRepository;
    private final YatakRepository yatakRepository;
    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final YatakService yatakService;
    private final YatisHemsireAtamaRepository yatisHemsireAtamaRepository; // YENİ EKLENDİ
    private final RolRepository rolRepository; // Rol kontrolü için eklendi

    @Autowired
    public YatisServiceImpl(YatisRepository yatisRepository,
                            HastaRepository hastaRepository,
                            YatakRepository yatakRepository,
                            PersonelRepository personelRepository,
                            KullaniciRepository kullaniciRepository,
                            YatakService yatakService,
                            YatisHemsireAtamaRepository yatisHemsireAtamaRepository, // YENİ EKLENDİ
                            RolRepository rolRepository) { // Eklendi
        this.yatisRepository = yatisRepository;
        this.hastaRepository = hastaRepository;
        this.yatakRepository = yatakRepository;
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.yatakService = yatakService;
        this.yatisHemsireAtamaRepository = yatisHemsireAtamaRepository; // YENİ EKLENDİ
        this.rolRepository = rolRepository; // Eklendi
    }

    // ... (hastaYatisiYap, hastaTaburcuEt ve diğer get metotları aynı kalacak)
    // Önceki mesajdaki gibi olacaklar, buraya tekrar kopyalamıyorum.

    @Override
    @Transactional
    public YatisGoruntuleDTO hastaYatisiYap(YatisOlusturDTO dto, Integer yapanKullaniciId) {
        log.info("Hasta yatışı yapılıyor. Hasta ID: {}, Yatak ID: {}, Yapan Kullanıcı ID: {}", dto.getHastaId(), dto.getYatakId(), yapanKullaniciId);
        checkYatisIslemYetkisi(yapanKullaniciId, "yatış yapma", null); // Yatış için belirli bir Yatis nesnesi yok

        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + dto.getHastaId()));

        Yatak yatak = yatakRepository.findById(dto.getYatakId())
                .orElseThrow(() -> new ResourceNotFoundException("Yatak bulunamadı, ID: " + dto.getYatakId()));

        Personel sorumluDoktor = personelRepository.findById(dto.getSorumluDoktorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sorumlu doktor bulunamadı, Personel ID: " + dto.getSorumluDoktorId()));
        
        boolean isDoktorRole = sorumluDoktor.getKullanici().getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if(!isDoktorRole){
             throw new IllegalArgumentException("Belirtilen personel ID ("+dto.getSorumluDoktorId()+") bir doktora ait değil.");
        }

        Optional<Yatis> aktifYatisKontrol = yatisRepository.findByHasta_IdAndCikisTarihiIsNull(hasta.getId());
        if (aktifYatisKontrol.isPresent()) {
            throw new IllegalStateException("Hastanın (ID: " + hasta.getId() + ") zaten aktif bir yatışı bulunmaktadır (Yatış ID: " + aktifYatisKontrol.get().getId() + ").");
        }

        if (yatak.isDoluMu()) {
            throw new IllegalStateException("Seçilen yatak (ID: " + yatak.getId() + ") dolu.");
        }

        Yatis yatis = new Yatis();
        yatis.setHasta(hasta);
        yatis.setYatak(yatak);
        yatis.setSorumluDoktor(sorumluDoktor);
        yatis.setYatisNedeni(dto.getYatisNedeni());

        Yatis kaydedilmisYatis = yatisRepository.save(yatis);
        yatakService.updateYatakDolulukDurumu(yatak.getId(), true);
        log.info("Hasta (ID: {}) yatağa (ID: {}) başarıyla yatırıldı. Yatış ID: {}", hasta.getId(), yatak.getId(), kaydedilmisYatis.getId());

        return convertToGoruntuleDTO(kaydedilmisYatis);
    }

    @Override
    @Transactional
    public YatisGoruntuleDTO hastaTaburcuEt(Integer yatisId, Integer yapanKullaniciId) {
        log.info("Hasta taburcu ediliyor. Yatış ID: {}, Yapan Kullanıcı ID: {}", yatisId, yapanKullaniciId);
        Yatis yatis = yatisRepository.findById(yatisId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatış kaydı bulunamadı, ID: " + yatisId));
        checkYatisIslemYetkisi(yapanKullaniciId, "taburcu etme", yatis);


        if (yatis.getCikisTarihi() != null) {
            throw new IllegalStateException("Bu yatış (ID: " + yatisId + ") zaten " + yatis.getCikisTarihi() + " tarihinde taburcu edilmiş.");
        }

        yatis.setCikisTarihi(LocalDateTime.now());
        Yatis guncellenmisYatis = yatisRepository.save(yatis);

        if (guncellenmisYatis.getYatak() != null) {
            yatakService.updateYatakDolulukDurumu(guncellenmisYatis.getYatak().getId(), false);
            log.info("Hasta (ID: {}) yataktan (ID: {}) başarıyla taburcu edildi. Yatış ID: {}", guncellenmisYatis.getHasta().getId(), guncellenmisYatis.getYatak().getId(), guncellenmisYatis.getId());
        } else {
             log.warn("Taburcu edilen yatışın (ID: {}) bir yatakla ilişkisi bulunamadı. Yatak durumu güncellenemedi.", yatisId);
        }
        return convertToGoruntuleDTO(guncellenmisYatis);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<YatisGoruntuleDTO> getYatisById(Integer yatisId, Integer talepEdenKullaniciId) {
        log.debug("getYatisById çağrıldı. Yatış ID: {}, Talep Eden Kullanıcı ID: {}", yatisId, talepEdenKullaniciId);
        return yatisRepository.findById(yatisId)
                .map(yatis -> {
                    checkYatisGoruntulemeYetkisi(yatis, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(yatis);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<YatisGoruntuleDTO> getTumYatislarByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        // ... (Bu metot aynı kalacak, yetki kontrolü içinde)
        log.debug("getTumYatislarByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        Optional<Hasta> hastaProfili = hastaRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliHasta = hastaProfili.isPresent() && hastaProfili.get().getId().equals(hastaId);

        if (!isAdmin && !isIlgiliHasta) {
            throw new AccessDeniedException("Bu hastanın yatış bilgilerini görme yetkiniz yok.");
        }

        return yatisRepository.findByHasta_IdOrderByGirisTarihiDesc(hastaId).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<YatisGoruntuleDTO> getAktifYatisByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        // ... (Bu metot aynı kalacak, yetki kontrolü içinde)
         log.debug("getAktifYatisByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        return yatisRepository.findByHasta_IdAndCikisTarihiIsNull(hastaId)
                .map(yatis -> {
                    checkYatisGoruntulemeYetkisi(yatis, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(yatis);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<YatisGoruntuleDTO> getAktifYatisByYatakId(Integer yatakId, Integer talepEdenKullaniciId) {
        // ... (Bu metot aynı kalacak, yetki kontrolü içinde)
        log.debug("getAktifYatisByYatakId çağrıldı. Yatak ID: {}, Talep Eden Kullanıcı ID: {}", yatakId, talepEdenKullaniciId);
        return yatisRepository.findByYatak_IdAndCikisTarihiIsNull(yatakId)
                .map(yatis -> {
                    checkYatisGoruntulemeYetkisi(yatis, talepEdenKullaniciId);
                    return convertToGoruntuleDTO(yatis);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<YatisGoruntuleDTO> getTumAktifYatislar(Integer talepEdenKullaniciId) {
        // ... (Bu metot aynı kalacak, yetki kontrolü içinde)
        log.debug("getTumAktifYatislar çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "tüm aktif yatışları");

        return yatisRepository.findByCikisTarihiIsNullOrderByGirisTarihiDesc().stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }


    // --- YENİ EKLENEN METOTLAR ---
    @Override
    @Transactional
    public YatisGoruntuleDTO hemsireAta(Integer yatisId, HemsireAtaDTO hemsireAtaDTO, Integer yapanKullaniciId) {
        log.info("Yatış ID {} için hemşire {} atanıyor. Yapan Kullanıcı ID: {}", yatisId, hemsireAtaDTO.getHemsirePersonelId(), yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "hemşire atama"); // Sadece ADMIN yapabilir

        Yatis yatis = yatisRepository.findById(yatisId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatış kaydı bulunamadı, ID: " + yatisId));

        Personel hemsire = personelRepository.findById(hemsireAtaDTO.getHemsirePersonelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hemşire personel bulunamadı, ID: " + hemsireAtaDTO.getHemsirePersonelId()));

        // Atanacak personelin HEMSIRE rolüne sahip olup olmadığını kontrol et
        boolean isHemsireRole = hemsire.getKullanici().getRoller().stream()
                                     .anyMatch(rol -> "ROLE_HEMSIRE".equals(rol.getAd()));
        if (!isHemsireRole) {
            throw new IllegalArgumentException("Atanmak istenen personel (ID: " + hemsire.getId() + ") hemşire rolünde değil.");
        }

        if (yatisHemsireAtamaRepository.existsByYatis_IdAndHemsire_Id(yatisId, hemsire.getId())) {
            throw new IllegalArgumentException("Bu hemşire (ID: " + hemsire.getId() + ") zaten bu yatışa (ID: " + yatisId + ") atanmış.");
        }

        YatisHemsireAtama atama = new YatisHemsireAtama();
        atama.setYatis(yatis);
        atama.setHemsire(hemsire);
        // atama.setAtamaTarihi(); // @CreationTimestamp ile otomatik

        yatis.addHemsireAtama(atama); // Entity'deki yardımcı metot
        yatisRepository.save(yatis); // Cascade ile YatisHemsireAtama da kaydedilir.
        log.info("Hemşire (Personel ID: {}) yatışa (ID: {}) başarıyla atandı.", hemsire.getId(), yatisId);

        return convertToGoruntuleDTO(yatis); // Güncellenmiş yatış DTO'sunu dön
    }

    @Override
    @Transactional
    public YatisGoruntuleDTO hemsireAtamasiniKaldir(Integer yatisId, Integer yatisHemsireAtamaId, Integer yapanKullaniciId) {
        log.info("Yatış ID {} için hemşire ataması (ID: {}) kaldırılıyor. Yapan Kullanıcı ID: {}", yatisId, yatisHemsireAtamaId, yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "hemşire atamasını kaldırma"); // Sadece ADMIN yapabilir

        Yatis yatis = yatisRepository.findById(yatisId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatış kaydı bulunamadı, ID: " + yatisId));

        YatisHemsireAtama atama = yatisHemsireAtamaRepository.findById(yatisHemsireAtamaId)
                .orElseThrow(() -> new ResourceNotFoundException("Kaldırılacak hemşire atama kaydı bulunamadı, ID: " + yatisHemsireAtamaId));

        if (!atama.getYatis().getId().equals(yatis.getId())) {
            throw new IllegalArgumentException("Kaldırılmak istenen hemşire ataması (ID: " + yatisHemsireAtamaId + ") bu yatışa (ID: " + yatisId + ") ait değil.");
        }

        yatis.removeHemsireAtama(atama); // Entity'deki yardımcı metot
        // orphanRemoval=true olduğu için YatisHemsireAtama kaydı Yatis kaydedildiğinde DB'den silinir.
        // Direkt yatisHemsireAtamaRepository.delete(atama); da çağrılabilir,
        // ama koleksiyondan çıkarmak daha JPA-vari bir yoldur.
        yatisRepository.save(yatis);
        // veya doğrudan: yatisHemsireAtamaRepository.delete(atama);
        log.info("Hemşire ataması (ID: {}) yatıştan (ID: {}) başarıyla kaldırıldı.", yatisHemsireAtamaId, yatisId);

        return convertToGoruntuleDTO(yatis); // Güncellenmiş yatış DTO'sunu dön
    }


    // --- Helper Metotlar ---
    private YatisGoruntuleDTO convertToGoruntuleDTO(Yatis yatis) {
        if (yatis == null) return null;

        String doktorBransAdi = null;
        if (yatis.getSorumluDoktor().getDoktorDetay() != null && yatis.getSorumluDoktor().getDoktorDetay().getBrans() != null) {
            doktorBransAdi = yatis.getSorumluDoktor().getDoktorDetay().getBrans().getAd();
        }

        List<AtanmisHemsireDTO> hemsireDTOList;
        if (yatis.getHemsireAtamalari() != null) {
            hemsireDTOList = yatis.getHemsireAtamalari().stream()
                .map(atama -> AtanmisHemsireDTO.builder()
                        .yatisHemsireAtamaId(atama.getId())
                        .hemsirePersonelId(atama.getHemsire().getId())
                        .hemsireAdiSoyadi(atama.getHemsire().getAd() + " " + atama.getHemsire().getSoyad())
                        .atamaTarihi(atama.getAtamaTarihi())
                        .build())
                .collect(Collectors.toList());
        } else {
            hemsireDTOList = Collections.emptyList();
        }


        return YatisGoruntuleDTO.builder()
                .id(yatis.getId())
                .hastaId(yatis.getHasta().getId())
                .hastaAdiSoyadi(yatis.getHasta().getAd() + " " + yatis.getHasta().getSoyad())
                .hastaTcKimlikNo(yatis.getHasta().getTcKimlikNo())
                .yatakId(yatis.getYatak().getId())
                .yatakNumarasi(yatis.getYatak().getYatakNumarasi())
                .odaNumarasi(yatis.getYatak().getOda().getOdaNumarasi())
                .katAdi(yatis.getYatak().getOda().getKat().getAd())
                .sorumluDoktorId(yatis.getSorumluDoktor().getId())
                .sorumluDoktorAdiSoyadi(yatis.getSorumluDoktor().getAd() + " " + yatis.getSorumluDoktor().getSoyad())
                .sorumluDoktorBransAdi(doktorBransAdi)
                .girisTarihi(yatis.getGirisTarihi())
                .cikisTarihi(yatis.getCikisTarihi())
                .yatisNedeni(yatis.getYatisNedeni())
                .hemsireler(hemsireDTOList) // YENİ EKLENDİ
                .build();
    }

    // Yetki kontrol metotları (ihtiyaca göre detaylandırılabilir)
    private void checkAdminYetkisi(Integer yapanKullaniciId, String islemTuru) {
        Kullanici yapanKullanici = kullaniciRepository.findById(yapanKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı."));
        boolean isAdmin = yapanKullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        if (!isAdmin) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok (Sadece ADMIN).");
        }
    }

    private void checkYatisIslemYetkisi(Integer yapanKullaniciId, String islemTuru, Yatis yatisContext) {
        Kullanici yapanKullanici = kullaniciRepository.findById(yapanKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı."));
        
        boolean yetkili = yapanKullanici.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || 
                             rol.getAd().equals("ROLE_DOKTOR") || 
                             rol.getAd().equals("ROLE_HEMSIRE") ||
                             rol.getAd().equals("ROLE_HASTA_KABUL")); // Hasta kabul de yatış yapabilir
        
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok.");
        }
        // Ekstra kontroller: örn, doktor sadece kendi hastasının yatışını/taburcusunu yapabilir vb.
        // Bu yatisContext üzerinden yapılabilir. Şimdilik genel rol kontrolü.
    }
    
    private void checkYatisGoruntulemeYetkisi(Yatis yatis, Integer talepEdenKullaniciId) {
        // ... (Bu metot aynı kalabilir veya detaylandırılabilir)
        if (yatis == null) return;

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isIlgiliHasta = yatis.getHasta().getKullanici() != null && yatis.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isSorumluDoktor = yatis.getSorumluDoktor().getKullanici() != null && yatis.getSorumluDoktor().getKullanici().getId().equals(talepEdenKullaniciId);
        
        // Yatışa atanmış hemşireler de görebilir mi?
        boolean isAtanmisHemsire = false;
        if (yatis.getHemsireAtamalari() != null) {
            isAtanmisHemsire = yatis.getHemsireAtamalari().stream()
                                .anyMatch(atama -> atama.getHemsire().getKullanici() != null && 
                                                  atama.getHemsire().getKullanici().getId().equals(talepEdenKullaniciId));
        }

        if (!isAdmin && !isIlgiliHasta && !isSorumluDoktor && !isAtanmisHemsire) {
            throw new AccessDeniedException("Bu yatış kaydını görüntüleme yetkiniz yok.");
        }
    }

    private void checkGenelListeGoruntulemeYetkisi(Integer talepEdenKullaniciId, String listeTuru) {
        // ... (Bu metot aynı kalabilir veya detaylandırılabilir)
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean yetkili = talepEden.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || rol.getAd().equals("ROLE_YONETICI") || rol.getAd().equals("ROLE_HASTA_KABUL"));
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + listeTuru + "' listesini görüntüleme yetkisi yok.");
        }
    }
}