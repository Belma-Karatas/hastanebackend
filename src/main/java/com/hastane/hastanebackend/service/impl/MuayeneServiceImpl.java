package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.MuayeneGoruntuleDTO;
import com.hastane.hastanebackend.dto.MuayeneOlusturDTO;
import com.hastane.hastanebackend.entity.*;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.*;
import com.hastane.hastanebackend.service.MuayeneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MuayeneServiceImpl implements MuayeneService {

    private static final Logger logger = LoggerFactory.getLogger(MuayeneServiceImpl.class);

    private final MuayeneRepository muayeneRepository;
    private final RandevuRepository randevuRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public MuayeneServiceImpl(MuayeneRepository muayeneRepository,
                              RandevuRepository randevuRepository,
                              HastaRepository hastaRepository,
                              PersonelRepository personelRepository,
                              KullaniciRepository kullaniciRepository) {
        this.muayeneRepository = muayeneRepository;
        this.randevuRepository = randevuRepository;
        this.hastaRepository = hastaRepository;
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public MuayeneGoruntuleDTO muayeneOlustur(MuayeneOlusturDTO dto, Integer doktorKullaniciId) {
        logger.info("Muayene oluşturma isteği alındı. Doktor Kullanıcı ID: {}, Randevu ID (varsa): {}", doktorKullaniciId, dto.getRandevuId());
        Kullanici aktifKullanici = kullaniciRepository.findById(doktorKullaniciId)
                .orElseThrow(() -> {
                    logger.error("Muayene oluşturma: Aktif kullanıcı bulunamadı, ID: {}", doktorKullaniciId);
                    return new ResourceNotFoundException("Aktif kullanıcı bulunamadı, ID: " + doktorKullaniciId);
                });

        boolean isDoktorRole = aktifKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if (!isDoktorRole) {
            logger.warn("Muayene oluşturma: Yetkisiz deneme. Kullanıcı ID: {} (Doktor rolü yok)", doktorKullaniciId);
            throw new AccessDeniedException("Sadece doktorlar muayene kaydı oluşturabilir.");
        }

        Personel doktor = personelRepository.findByKullanici_Id(doktorKullaniciId)
                .orElseThrow(() -> {
                     logger.error("Muayene oluşturma: Doktor profili bulunamadı, Kullanıcı ID: {}", doktorKullaniciId);
                    return new ResourceNotFoundException("Doktor profili bulunamadı, Kullanıcı ID: " + doktorKullaniciId);
                });

        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> {
                    logger.error("Muayene oluşturma: Hasta bulunamadı, ID: {}", dto.getHastaId());
                    return new ResourceNotFoundException("Hasta bulunamadı, ID: " + dto.getHastaId());
                });

        Randevu randevu = null;
        if (dto.getRandevuId() != null) {
            randevu = randevuRepository.findById(dto.getRandevuId())
                    .orElseThrow(() -> {
                        logger.error("Muayene oluşturma: Randevu bulunamadı, ID: {}", dto.getRandevuId());
                        return new ResourceNotFoundException("Randevu bulunamadı, ID: " + dto.getRandevuId());
                    });
            if (!randevu.getHasta().getId().equals(hasta.getId()) || !randevu.getDoktor().getId().equals(doktor.getId())) {
                logger.warn("Muayene oluşturma: Randevu bilgileri (Hasta ID: {}, Doktor ID: {}) muayene yapılacak hasta/doktor ile eşleşmiyor (Hasta ID: {}, Doktor ID: {}).",
                    randevu.getHasta().getId(), randevu.getDoktor().getId(), hasta.getId(), doktor.getId());
                throw new IllegalArgumentException("Randevu bilgileri muayene yapılacak hasta veya doktor ile eşleşmiyor.");
            }
            if (muayeneRepository.findByRandevu_Id(dto.getRandevuId()).isPresent()) {
                logger.warn("Muayene oluşturma: Randevu ID {} için zaten bir muayene kaydı var.", dto.getRandevuId());
                throw new IllegalArgumentException("Bu randevu için zaten bir muayene kaydı bulunmaktadır.");
            }
        }

        Muayene muayene = new Muayene();
        muayene.setRandevu(randevu);
        muayene.setHasta(hasta);
        muayene.setDoktor(doktor);
        muayene.setMuayeneTarihiSaati(dto.getMuayeneTarihiSaati() != null ? dto.getMuayeneTarihiSaati() : LocalDateTime.now());
        muayene.setHikaye(dto.getHikaye());
        muayene.setTani(dto.getTani());
        muayene.setTedaviNotlari(dto.getTedaviNotlari());

        Muayene kaydedilmisMuayene = muayeneRepository.save(muayene);
        logger.info("Muayene başarıyla oluşturuldu. Muayene ID: {}", kaydedilmisMuayene.getId());
        return convertToGoruntuleDTO(kaydedilmisMuayene);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MuayeneGoruntuleDTO> getMuayeneById(Integer muayeneId, Integer talepEdenKullaniciId) {
        logger.debug("getMuayeneById çağrıldı. Muayene ID: {}, Talep Eden Kullanıcı ID: {}", muayeneId, talepEdenKullaniciId);
        
        Muayene muayene = muayeneRepository.findById(muayeneId)
                .orElseThrow(() -> new ResourceNotFoundException("Muayene bulunamadı, ID: " + muayeneId));
        
        // Yetki kontrolü doğrudan burada yapılıyor
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı: " + talepEdenKullaniciId));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliDoktor = muayene.getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isIlgiliHasta = muayene.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliDoktor && !isIlgiliHasta) {
            logger.warn("getMuayeneById: Yetkisiz erişim denemesi. Muayene ID: {}, Talep Eden Kullanıcı ID: {}", muayeneId, talepEdenKullaniciId);
            throw new AccessDeniedException("Bu muayene kaydını görüntüleme yetkiniz yok.");
        }
        
        return Optional.of(convertToGoruntuleDTO(muayene));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MuayeneGoruntuleDTO> findDtoByRandevuId(Integer randevuId, Integer talepEdenKullaniciId) {
        logger.debug("findDtoByRandevuId çağrıldı. Randevu ID: {}, Talep Eden Kullanıcı ID: {}", randevuId, talepEdenKullaniciId);
        
        Randevu randevu = randevuRepository.findById(randevuId)
            .orElseThrow(() -> new ResourceNotFoundException("İlişkili randevu bulunamadı, ID: " + randevuId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı. Kullanıcı ID: " + talepEdenKullaniciId));
        
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliDoktor = randevu.getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isIlgiliHasta = randevu.getHasta().getKullanici().getId().equals(talepEdenKullaniciId); 

        if (!isAdmin && !isIlgiliDoktor && !isIlgiliHasta) {
            logger.warn("findDtoByRandevuId: Yetkisiz erişim denemesi. Randevu ID: {}, Talep Eden Kullanıcı ID: {}", randevuId, talepEdenKullaniciId);
            throw new AccessDeniedException("Bu randevuya ait muayene bilgilerini görüntüleme yetkiniz yok.");
        }
        
        return muayeneRepository.findByRandevu_Id(randevuId)
                                .map(this::convertToGoruntuleDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MuayeneGoruntuleDTO> getMuayenelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        logger.debug("getMuayenelerByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        Hasta hasta = hastaRepository.findById(hastaId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + hastaId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı: " + talepEdenKullaniciId));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliHasta = hasta.getKullanici() != null && hasta.getKullanici().getId().equals(talepEdenKullaniciId);
        
        if (!isAdmin && !isIlgiliHasta) {
            logger.warn("getMuayenelerByHastaId: Yetkisiz erişim. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
            throw new AccessDeniedException("Bu hastanın muayenelerini görüntüleme yetkiniz yok.");
        }

        List<Muayene> muayeneler = muayeneRepository.findByHasta_IdOrderByMuayeneTarihiSaatiDesc(hastaId);
        return muayeneler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MuayeneGoruntuleDTO> getMuayenelerByDoktorIdAndGun(Integer doktorPersonelId, LocalDate gun, Integer talepEdenKullaniciId) {
        logger.debug("getMuayenelerByDoktorIdAndGun çağrıldı. Doktor Personel ID: {}, Gün: {}, Talep Eden Kullanıcı ID: {}", doktorPersonelId, gun, talepEdenKullaniciId);
        Personel doktor = personelRepository.findById(doktorPersonelId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, Personel ID: " + doktorPersonelId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı: " + talepEdenKullaniciId));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        
        boolean isIlgiliDoktor = doktor.getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliDoktor) {
             logger.warn("getMuayenelerByDoktorIdAndGun: Yetkisiz erişim. Hedef Doktor Personel ID: {}, Talep Eden Kullanıcı ID: {}", doktorPersonelId, talepEdenKullaniciId);
            throw new AccessDeniedException("Bu doktorun randevularını/muayenelerini görüntüleme yetkiniz yok.");
        }
        
        LocalDateTime gunBaslangic = gun.atStartOfDay();
        LocalDateTime gunBitis = gun.atTime(23, 59, 59);
        List<Muayene> muayeneler = muayeneRepository.findByDoktor_IdAndMuayeneTarihiSaatiBetween(doktorPersonelId, gunBaslangic, gunBitis);
        return muayeneler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MuayeneGoruntuleDTO muayeneGuncelle(Integer muayeneId, MuayeneOlusturDTO dto, Integer doktorKullaniciId) {
        logger.info("Muayene güncelleme isteği. Muayene ID: {}, Doktor Kullanıcı ID: {}", muayeneId, doktorKullaniciId);
        Muayene mevcutMuayene = muayeneRepository.findById(muayeneId)
                .orElseThrow(() -> {
                    logger.error("Muayene güncelleme: Muayene bulunamadı, ID: {}", muayeneId);
                    return new ResourceNotFoundException("Güncellenecek muayene bulunamadı, ID: " + muayeneId);
                });

        Kullanici aktifKullanici = kullaniciRepository.findById(doktorKullaniciId)
                .orElseThrow(() -> {
                    logger.error("Muayene güncelleme: Aktif kullanıcı bulunamadı, ID: {}", doktorKullaniciId);
                    return new ResourceNotFoundException("Aktif kullanıcı bulunamadı, ID: " + doktorKullaniciId);
                });
        
        boolean isAdmin = aktifKullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isMuayeneyiYapanDoktor = mevcutMuayene.getDoktor().getKullanici().getId().equals(doktorKullaniciId);

        if (!isMuayeneyiYapanDoktor && !isAdmin) {
            logger.warn("Muayene güncelleme: Yetkisiz deneme. Muayene ID: {}, Talep Eden Kullanıcı ID: {}", muayeneId, doktorKullaniciId);
             throw new AccessDeniedException("Sadece muayeneyi oluşturan doktor veya admin bu kaydı güncelleyebilir.");
        }

        if (dto.getHikaye() != null) mevcutMuayene.setHikaye(dto.getHikaye());
        if (dto.getTani() != null) mevcutMuayene.setTani(dto.getTani());
        if (dto.getTedaviNotlari() != null) mevcutMuayene.setTedaviNotlari(dto.getTedaviNotlari());
        if (dto.getMuayeneTarihiSaati() != null) mevcutMuayene.setMuayeneTarihiSaati(dto.getMuayeneTarihiSaati());
        
        if (dto.getHastaId() != null && !dto.getHastaId().equals(mevcutMuayene.getHasta().getId())) {
            logger.warn("Muayene güncelleme: Hasta ID'si değiştirilmeye çalışıldı. Bu işlem desteklenmiyor. Muayene ID: {}", muayeneId);
        }

        Muayene guncellenmisMuayene = muayeneRepository.save(mevcutMuayene);
        logger.info("Muayene (ID: {}) başarıyla güncellendi.", guncellenmisMuayene.getId());
        return convertToGoruntuleDTO(guncellenmisMuayene);
    }

    private MuayeneGoruntuleDTO convertToGoruntuleDTO(Muayene muayene) {
        if (muayene == null) return null;
        
        MuayeneGoruntuleDTO.MuayeneGoruntuleDTOBuilder builder = MuayeneGoruntuleDTO.builder()
                .id(muayene.getId())
                .muayeneTarihiSaati(muayene.getMuayeneTarihiSaati())
                .hikaye(muayene.getHikaye())
                .tani(muayene.getTani())
                .tedaviNotlari(muayene.getTedaviNotlari())
                .olusturulmaTarihi(muayene.getOlusturulmaTarihi());

        if (muayene.getHasta() != null) {
            builder.hastaId(muayene.getHasta().getId())
                   .hastaAdiSoyadi(muayene.getHasta().getAd() + " " + muayene.getHasta().getSoyad());
        }
        
        if (muayene.getDoktor() != null) {
            builder.doktorId(muayene.getDoktor().getId())
                   .doktorAdiSoyadi(muayene.getDoktor().getAd() + " " + muayene.getDoktor().getSoyad());
            if (muayene.getDoktor().getDoktorDetay() != null && muayene.getDoktor().getDoktorDetay().getBrans() != null) {
                builder.doktorBransAdi(muayene.getDoktor().getDoktorDetay().getBrans().getAd());
            }
        }

        if (muayene.getRandevu() != null) {
            builder.randevuId(muayene.getRandevu().getId());
        }
        return builder.build();
    }
}