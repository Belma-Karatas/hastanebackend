package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.AtanmisHemsireDTO;
import com.hastane.hastanebackend.dto.HemsireAtaDTO;
import com.hastane.hastanebackend.dto.YatisGoruntuleDTO;
import com.hastane.hastanebackend.dto.YatisOlusturDTO;
import com.hastane.hastanebackend.entity.*;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.*;
// import com.hastane.hastanebackend.service.YatakService; // Eğer YatakService'e özel bir metot çağırmıyorsak kaldırılabilir.
import com.hastane.hastanebackend.service.YatisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Constructor injection için zorunlu değil ama kalabilir.
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
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
    // private final YatakService yatakService; // Yorum satırına alındı, direkt YatakRepository kullanılıyor.
    private final YatisHemsireAtamaRepository yatisHemsireAtamaRepository;
    private final RolRepository rolRepository; // Bu alan kullanılmıyorsa kaldırılabilir.

    // @Autowired // Constructor injection için zorunlu değil.
    public YatisServiceImpl(YatisRepository yatisRepository,
                            HastaRepository hastaRepository,
                            YatakRepository yatakRepository,
                            PersonelRepository personelRepository,
                            KullaniciRepository kullaniciRepository,
                            /*YatakService yatakService,*/
                            YatisHemsireAtamaRepository yatisHemsireAtamaRepository,
                            RolRepository rolRepository) { // Eğer rolRepository kullanılmayacaksa buradan da kaldırın.
        this.yatisRepository = yatisRepository;
        this.hastaRepository = hastaRepository;
        this.yatakRepository = yatakRepository;
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        // this.yatakService = yatakService;
        this.yatisHemsireAtamaRepository = yatisHemsireAtamaRepository;
        this.rolRepository = rolRepository; // Eğer kullanılmayacaksa bu satırı da kaldırın.
    }

    @Override
    @Transactional
    public YatisGoruntuleDTO hastaYatisiYap(YatisOlusturDTO dto, Integer yapanKullaniciId) {
        log.info("Hasta yatışı yapılıyor/kararı veriliyor. Hasta ID: {}, Yatak ID (varsa): {}, Sorumlu Dr ID: {}, Yapan Kullanıcı ID: {}",
            dto.getHastaId(), dto.getYatakId(), dto.getSorumluDoktorId(), yapanKullaniciId);

        Kullanici yapanKullanici = kullaniciRepository.findById(yapanKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı: " + yapanKullaniciId));
        
        boolean isYetkili = yapanKullanici.getRoller().stream()
            .anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()) || 
                             "ROLE_DOKTOR".equals(rol.getAd()) || 
                             "ROLE_HASTA_KABUL".equals(rol.getAd()));
        if (!isYetkili) {
            throw new AccessDeniedException("Bu işlemi (yatış/yatış kararı) yapma yetkiniz bulunmamaktadır.");
        }
        
        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + dto.getHastaId()));

        Personel sorumluDoktor = personelRepository.findById(dto.getSorumluDoktorId())
                .orElseThrow(() -> new ResourceNotFoundException("Sorumlu doktor bulunamadı, Personel ID: " + dto.getSorumluDoktorId()));
        
        boolean isDoktorRole = sorumluDoktor.getKullanici().getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if(!isDoktorRole){
             throw new IllegalArgumentException("Belirtilen sorumlu personel (ID: "+dto.getSorumluDoktorId()+") bir doktora ait değil.");
        }

        Optional<Yatis> mevcutAktifYatis = yatisRepository.findByHasta_IdAndCikisTarihiIsNull(dto.getHastaId());
        if (mevcutAktifYatis.isPresent()) {
            Yatis aktifYatis = mevcutAktifYatis.get();
            if (!"TABURCU".equalsIgnoreCase(aktifYatis.getDurum())) {
                 throw new IllegalStateException("Hastanın (ID: " + dto.getHastaId() + ") zaten \"" + aktifYatis.getDurum() + "\" durumunda bir yatış kaydı var (Yatış ID: " + aktifYatis.getId() + ").");
            }
        }

        Yatis yatis = new Yatis();
        yatis.setHasta(hasta);
        yatis.setSorumluDoktor(sorumluDoktor);
        yatis.setYatisNedeni(dto.getYatisNedeni());

        if (dto.getYatakId() != null) {
            log.info("Yatak atanarak yatış yapılıyor. Yatak ID: {}", dto.getYatakId());
            Yatak yatak = yatakRepository.findById(dto.getYatakId())
                    .orElseThrow(() -> new ResourceNotFoundException("Yatak bulunamadı, ID: " + dto.getYatakId()));

            if (yatak.isDoluMu()) {
                if (yatak.getAktifYatis() == null) {
                    log.warn("Tutarsızlık: Yatak ID {} dolu olarak işaretli ama aktif yatış bilgisi yok. Yine de yatış engelleniyor.", yatak.getId());
                }
                throw new IllegalStateException("Seçilen yatak (ID: " + yatak.getId() + ") dolu.");
            }
            yatis.setYatak(yatak);
            yatis.setDurum("AKTIF"); 

            Yatis kaydedilmisYatis = yatisRepository.save(yatis);

            yatak.setDoluMu(true);
            yatak.setAktifYatis(kaydedilmisYatis); 
            yatakRepository.save(yatak); 
            
            log.info("Hasta (ID: {}) yatağa (ID: {}) başarıyla yatırıldı. Yatış ID: {}", hasta.getId(), yatak.getId(), kaydedilmisYatis.getId());
            return convertToGoruntuleDTO(kaydedilmisYatis);

        } else { 
            log.info("Doktor tarafından yatış kararı veriliyor. Hasta ID: {}", dto.getHastaId());
            
            boolean isDoktorYapanKullanici = yapanKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
            if (!isDoktorYapanKullanici && !yapanKullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()))) { // Admin de yatak belirtmeden yatış kararı oluşturabilir mi? Şimdilik evet.
                 // throw new AccessDeniedException("Sadece doktorlar yatak belirtmeden yatış kararı verebilir.");
            }

            yatis.setYatak(null); 
            yatis.setDurum("YATAK BEKLIYOR"); 
            Yatis kaydedilmisYatis = yatisRepository.save(yatis);
            log.info("Hastanın (ID: {}) yatış kararı (Yatış ID: {}) kaydedildi. Durum: YATAK BEKLIYOR", hasta.getId(), kaydedilmisYatis.getId());
            return convertToGoruntuleDTO(kaydedilmisYatis);
        }
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
        if (!"AKTIF".equalsIgnoreCase(yatis.getDurum())) { 
            throw new IllegalStateException("Sadece 'AKTIF' durumundaki yatışlar taburcu edilebilir. Mevcut durum: " + yatis.getDurum());
        }

        yatis.setCikisTarihi(LocalDateTime.now());
        yatis.setDurum("TABURCU"); 
        Yatis guncellenmisYatis = yatisRepository.save(yatis);

        Yatak yatak = guncellenmisYatis.getYatak(); 
        if (yatak != null) {
            yatak.setDoluMu(false);
            yatak.setAktifYatis(null); 
            yatakRepository.save(yatak); 
            log.info("Hasta (ID: {}) yataktan (ID: {}) başarıyla taburcu edildi. Yatış ID: {}", guncellenmisYatis.getHasta().getId(), yatak.getId(), guncellenmisYatis.getId());
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
         log.debug("getAktifYatisByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        return yatisRepository.findByHasta_IdAndCikisTarihiIsNull(hastaId)
                .map(yatis -> {
                    if ("AKTIF".equalsIgnoreCase(yatis.getDurum()) || "YATAK BEKLIYOR".equalsIgnoreCase(yatis.getDurum())) {
                        checkYatisGoruntulemeYetkisi(yatis, talepEdenKullaniciId);
                        return convertToGoruntuleDTO(yatis);
                    }
                    return null;
                }).filter(java.util.Objects::nonNull);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<YatisGoruntuleDTO> getAktifYatisByYatakId(Integer yatakId, Integer talepEdenKullaniciId) {
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
        log.debug("getTumAktifYatislar çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, "tüm aktif yatışları");

        return yatisRepository.findByDurumAndCikisTarihiIsNullOrderByGirisTarihiDesc("AKTIF").stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    // --- YENİ METOT IMPLEMENTASYONLARI ---
    @Override
    @Transactional(readOnly = true)
    public List<YatisGoruntuleDTO> getTumYatislarByDurum(String durum, Integer talepEdenKullaniciId) {
        log.debug("{} durumundaki tüm yatışlar getiriliyor. Talep Eden ID: {}", durum, talepEdenKullaniciId);
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, durum + " durumundaki yatışları");
        return yatisRepository.findByDurumAndCikisTarihiIsNullOrderByGirisTarihiDesc(durum.toUpperCase())
                .stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public YatisGoruntuleDTO yatakAta(Integer yatisId, Integer yatakId, Integer yapanKullaniciId) {
        log.info("Yatış ID {} için Yatak ID {} atanıyor. Yapan Kullanıcı ID: {}", yatisId, yatakId, yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "yatışa yatak atama");

        Yatis yatis = yatisRepository.findById(yatisId)
            .orElseThrow(() -> new ResourceNotFoundException("Yatak atanacak yatış kaydı bulunamadı: " + yatisId));

        if (yatis.getYatak() != null) {
            throw new IllegalStateException("Bu yatışa (ID: " + yatisId + ") zaten bir yatak (ID: " + yatis.getYatak().getId() + ") atanmış.");
        }
        if (!"YATAK BEKLIYOR".equalsIgnoreCase(yatis.getDurum())) {
             throw new IllegalStateException("Sadece 'YATAK BEKLIYOR' durumundaki yatışlara yatak atanabilir. Mevcut durum: " + yatis.getDurum());
        }

        Yatak yatak = yatakRepository.findById(yatakId)
            .orElseThrow(() -> new ResourceNotFoundException("Atanacak yatak bulunamadı: " + yatakId));

        if (yatak.isDoluMu()) {
            throw new IllegalStateException("Seçilen yatak (ID: " + yatak.getId() + ") dolu.");
        }

        yatis.setYatak(yatak);
        yatis.setDurum("AKTIF"); 
        Yatis guncellenmisYatis = yatisRepository.save(yatis);

        yatak.setDoluMu(true);
        yatak.setAktifYatis(guncellenmisYatis);
        yatakRepository.save(yatak);

        log.info("Yatış ID {} için Yatak ID {} başarıyla atandı. Yatış durumu 'AKTIF' yapıldı.", yatisId, yatakId);
        return convertToGoruntuleDTO(guncellenmisYatis);
    }
    // --- YENİ METOT IMPLEMENTASYONLARI SONU ---

    @Override
    @Transactional
    public YatisGoruntuleDTO hemsireAta(Integer yatisId, HemsireAtaDTO hemsireAtaDTO, Integer yapanKullaniciId) {
        log.info("Yatış ID {} için hemşire {} atanıyor. Yapan Kullanıcı ID: {}", yatisId, hemsireAtaDTO.getHemsirePersonelId(), yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "hemşire atama"); 

        Yatis yatis = yatisRepository.findById(yatisId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatış kaydı bulunamadı, ID: " + yatisId));

        Personel hemsire = personelRepository.findById(hemsireAtaDTO.getHemsirePersonelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hemşire personel bulunamadı, ID: " + hemsireAtaDTO.getHemsirePersonelId()));

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

        yatis.addHemsireAtama(atama); 
        yatisRepository.save(yatis);
        log.info("Hemşire (Personel ID: {}) yatışa (ID: {}) başarıyla atandı.", hemsire.getId(), yatisId);

        return convertToGoruntuleDTO(yatis);
    }

    @Override
    @Transactional
    public YatisGoruntuleDTO hemsireAtamasiniKaldir(Integer yatisId, Integer yatisHemsireAtamaId, Integer yapanKullaniciId) {
        log.info("Yatış ID {} için hemşire ataması (ID: {}) kaldırılıyor. Yapan Kullanıcı ID: {}", yatisId, yatisHemsireAtamaId, yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "hemşire atamasını kaldırma"); 

        Yatis yatis = yatisRepository.findById(yatisId)
                .orElseThrow(() -> new ResourceNotFoundException("Yatış kaydı bulunamadı, ID: " + yatisId));

        YatisHemsireAtama atama = yatisHemsireAtamaRepository.findById(yatisHemsireAtamaId)
                .orElseThrow(() -> new ResourceNotFoundException("Kaldırılacak hemşire atama kaydı bulunamadı, ID: " + yatisHemsireAtamaId));

        if (!atama.getYatis().getId().equals(yatis.getId())) {
            throw new IllegalArgumentException("Kaldırılmak istenen hemşire ataması (ID: " + yatisHemsireAtamaId + ") bu yatışa (ID: " + yatisId + ") ait değil.");
        }

        yatis.removeHemsireAtama(atama); 
        yatisRepository.save(yatis);
        log.info("Hemşire ataması (ID: {}) yatıştan (ID: {}) başarıyla kaldırıldı.", yatisHemsireAtamaId, yatisId);

        return convertToGoruntuleDTO(yatis);
    }

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
                .yatakId(yatis.getYatak() != null ? yatis.getYatak().getId() : null)
                .yatakNumarasi(yatis.getYatak() != null ? yatis.getYatak().getYatakNumarasi() : "Atanmadı")
                .odaNumarasi(yatis.getYatak() != null ? yatis.getYatak().getOda().getOdaNumarasi() : "-")
                .katAdi(yatis.getYatak() != null ? yatis.getYatak().getOda().getKat().getAd() : "-")
                .sorumluDoktorId(yatis.getSorumluDoktor().getId())
                .sorumluDoktorAdiSoyadi(yatis.getSorumluDoktor().getAd() + " " + yatis.getSorumluDoktor().getSoyad())
                .sorumluDoktorBransAdi(doktorBransAdi)
                .girisTarihi(yatis.getGirisTarihi())
                .cikisTarihi(yatis.getCikisTarihi())
                .yatisNedeni(yatis.getYatisNedeni())
                // .durum(yatis.getDurum()) // DTO'ya durum alanı eklenebilir
                .hemsireler(hemsireDTOList)
                .build();
    }

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
                             rol.getAd().equals("ROLE_HASTA_KABUL")); 
        
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok.");
        }
    }
    
    private void checkYatisGoruntulemeYetkisi(Yatis yatis, Integer talepEdenKullaniciId) {
        if (yatis == null) return;

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isIlgiliHasta = yatis.getHasta().getKullanici() != null && yatis.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isSorumluDoktor = yatis.getSorumluDoktor().getKullanici() != null && yatis.getSorumluDoktor().getKullanici().getId().equals(talepEdenKullaniciId);
        
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
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean yetkili = talepEden.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || rol.getAd().equals("ROLE_YONETICI") || rol.getAd().equals("ROLE_HASTA_KABUL"));
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + listeTuru + "' listesini görüntüleme yetkisi yok.");
        }
    }
}