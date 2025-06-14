package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.PersonelVardiyaGoruntuleDTO;
import com.hastane.hastanebackend.dto.PersonelVardiyaOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.PersonelVardiya;
import com.hastane.hastanebackend.entity.Vardiya;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.PersonelVardiyaRepository;
import com.hastane.hastanebackend.repository.VardiyaRepository;
import com.hastane.hastanebackend.service.PersonelVardiyaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonelVardiyaServiceImpl implements PersonelVardiyaService {

    private static final Logger log = LoggerFactory.getLogger(PersonelVardiyaServiceImpl.class);

    private final PersonelVardiyaRepository personelVardiyaRepository;
    private final PersonelRepository personelRepository;
    private final VardiyaRepository vardiyaRepository;
    private final KullaniciRepository kullaniciRepository;

    // Vardiya saatleri için sabitler (daha iyi bir yol application.properties veya DB olabilir)
    private static final LocalTime DOKTOR_BASLANGIC = LocalTime.of(9, 0);
    private static final LocalTime DOKTOR_BITIS = LocalTime.of(17, 0);
    private static final LocalTime HEMSIRE_GUNDUZ_BASLANGIC = LocalTime.of(8, 0);
    private static final LocalTime HEMSIRE_GUNDUZ_BITIS = LocalTime.of(17, 0);
    private static final LocalTime HEMSIRE_GECE_BASLANGIC = LocalTime.of(17, 0);
    private static final LocalTime HEMSIRE_GECE_BITIS = LocalTime.of(8, 0); // Ertesi gün


    @Autowired
    public PersonelVardiyaServiceImpl(PersonelVardiyaRepository personelVardiyaRepository,
                                      PersonelRepository personelRepository,
                                      VardiyaRepository vardiyaRepository,
                                      KullaniciRepository kullaniciRepository) {
        this.personelVardiyaRepository = personelVardiyaRepository;
        this.personelRepository = personelRepository;
        this.vardiyaRepository = vardiyaRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public PersonelVardiyaGoruntuleDTO vardiyaAta(PersonelVardiyaOlusturDTO dto, Integer yapanKullaniciId) {
        log.info("Personele vardiya atanıyor. Personel ID: {}, Vardiya ID: {}, Tarih: {}, Yapan Kullanıcı ID: {}",
                dto.getPersonelId(), dto.getVardiyaId(), dto.getTarih(), yapanKullaniciId);

        checkAdminYetkisi(yapanKullaniciId, "personel vardiya atama");

        Personel personel = personelRepository.findById(dto.getPersonelId())
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı, ID: " + dto.getPersonelId()));

        Vardiya vardiya = vardiyaRepository.findById(dto.getVardiyaId())
                .orElseThrow(() -> new ResourceNotFoundException("Vardiya tanımı bulunamadı, ID: " + dto.getVardiyaId()));

        // Personelin o tarihte zaten bir vardiyası var mı kontrol et
        if (personelVardiyaRepository.existsByPersonel_IdAndTarih(personel.getId(), dto.getTarih())) {
            throw new IllegalArgumentException("Personel (ID: " + personel.getId() + ") " + dto.getTarih() +
                                               " tarihinde zaten bir vardiyaya atanmış.");
        }

        // Vardiya atama kurallarını uygula
        boolean isDoktor = personel.getKullanici().getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        boolean isHemsire = personel.getKullanici().getRoller().stream().anyMatch(rol -> "ROLE_HEMSIRE".equals(rol.getAd()));

        if (isDoktor) {
            if (!(vardiya.getBaslangicSaati().equals(DOKTOR_BASLANGIC) && vardiya.getBitisSaati().equals(DOKTOR_BITIS))) {
                throw new IllegalArgumentException("Doktorlar sadece " + DOKTOR_BASLANGIC + "-" + DOKTOR_BITIS + " vardiyasına atanabilir.");
            }
        } else if (isHemsire) {
            boolean isGunduzVardiyasi = vardiya.getBaslangicSaati().equals(HEMSIRE_GUNDUZ_BASLANGIC) &&
                                       vardiya.getBitisSaati().equals(HEMSIRE_GUNDUZ_BITIS);
            boolean isGeceVardiyasi = vardiya.getBaslangicSaati().equals(HEMSIRE_GECE_BASLANGIC) &&
                                      vardiya.getBitisSaati().equals(HEMSIRE_GECE_BITIS);
            if (!isGunduzVardiyasi && !isGeceVardiyasi) {
                throw new IllegalArgumentException("Hemşireler sadece ( " + HEMSIRE_GUNDUZ_BASLANGIC + "-" + HEMSIRE_GUNDUZ_BITIS +
                                                   " ) veya ( " + HEMSIRE_GECE_BASLANGIC + "-" + HEMSIRE_GECE_BITIS + " ) vardiyalarına atanabilir.");
            }
        } else {
            // Diğer roller için bir kural yoksa veya hata verilecekse burası düzenlenir.
            // Şimdilik diğer rollerin vardiya atamasına izin verilmediğini varsayalım.
            throw new IllegalArgumentException("Sadece doktor veya hemşire rolündeki personele vardiya atanabilir.");
        }


        PersonelVardiya personelVardiya = new PersonelVardiya();
        personelVardiya.setPersonel(personel);
        personelVardiya.setVardiya(vardiya);
        personelVardiya.setTarih(dto.getTarih());
        // atamaTarihi @CreationTimestamp ile otomatik

        PersonelVardiya kaydedilmisAtama = personelVardiyaRepository.save(personelVardiya);
        log.info("Personele (ID: {}) vardiya (ID: {}) {} tarihi için başarıyla atandı. Atama ID: {}",
                personel.getId(), vardiya.getId(), dto.getTarih(), kaydedilmisAtama.getId());
        return convertToGoruntuleDTO(kaydedilmisAtama);
    }

    @Override
    @Transactional
    public void vardiyaAtamasiniKaldir(Integer personelVardiyaId, Integer yapanKullaniciId) {
        log.info("Personel vardiya ataması (ID: {}) kaldırılıyor. Yapan Kullanıcı ID: {}", personelVardiyaId, yapanKullaniciId);
        checkAdminYetkisi(yapanKullaniciId, "personel vardiya atamasını kaldırma");

        PersonelVardiya atama = personelVardiyaRepository.findById(personelVardiyaId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel vardiya ataması bulunamadı, ID: " + personelVardiyaId));
        
        // İleride: Atamanın tarihi geçmişse silinemez gibi bir kural eklenebilir.
        personelVardiyaRepository.delete(atama);
        log.info("Personel vardiya ataması (ID: {}) başarıyla kaldırıldı.", personelVardiyaId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonelVardiyaGoruntuleDTO> getPersonelVardiyaById(Integer personelVardiyaId, Integer talepEdenKullaniciId) {
        log.debug("getPersonelVardiyaById çağrıldı. Atama ID: {}, Talep Eden Kullanıcı ID: {}", personelVardiyaId, talepEdenKullaniciId);
        return personelVardiyaRepository.findById(personelVardiyaId)
                .map(pv -> {
                    checkVardiyaGoruntulemeYetkisi(pv.getPersonel().getKullanici().getId(), talepEdenKullaniciId);
                    return convertToGoruntuleDTO(pv);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelVardiyaGoruntuleDTO> getVardiyalarByPersonelId(Integer personelId, Integer talepEdenKullaniciId) {
        log.debug("getVardiyalarByPersonelId çağrıldı. Personel ID: {}, Talep Eden Kullanıcı ID: {}", personelId, talepEdenKullaniciId);
        Personel personel = personelRepository.findById(personelId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı, ID: " + personelId));
        checkVardiyaGoruntulemeYetkisi(personel.getKullanici().getId(), talepEdenKullaniciId);

        return personelVardiyaRepository.findByPersonel_IdOrderByTarihDesc(personelId).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelVardiyaGoruntuleDTO> getVardiyalarByTarih(LocalDate tarih, Integer talepEdenKullaniciId) {
        log.debug("getVardiyalarByTarih çağrıldı. Tarih: {}, Talep Eden Kullanıcı ID: {}", tarih, talepEdenKullaniciId);
        // Bu listeyi sadece ADMIN veya YONETICI görebilir.
        checkGenelListeGoruntulemeYetkisi(talepEdenKullaniciId, tarih + " tarihli vardiyaları");

        return personelVardiyaRepository.findByTarihOrderByPersonel_AdAsc(tarih).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelVardiyaGoruntuleDTO> getPersonelVardiyalariByTarihAraligi(
            Integer personelId, LocalDate baslangicTarihi, LocalDate bitisTarihi, Integer talepEdenKullaniciId) {
        log.debug("getPersonelVardiyalariByTarihAraligi çağrıldı. Personel ID: {}, Başlangıç: {}, Bitiş: {}, Talep Eden: {}",
                personelId, baslangicTarihi, bitisTarihi, talepEdenKullaniciId);
        Personel personel = personelRepository.findById(personelId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel bulunamadı, ID: " + personelId));
        checkVardiyaGoruntulemeYetkisi(personel.getKullanici().getId(), talepEdenKullaniciId);

        return personelVardiyaRepository.findByPersonel_IdAndTarihBetweenOrderByTarihAsc(personelId, baslangicTarihi, bitisTarihi).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonelVardiyaGoruntuleDTO> getMyYaklasanVardiyalar(Integer talepEdenKullaniciId) {
        log.debug("getMyYaklasanVardiyalar çağrıldı. Talep Eden Kullanıcı ID: {}", talepEdenKullaniciId);
        Personel personel = personelRepository.findByKullanici_Id(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Personel profili bulunamadı. Kullanıcı ID: " + talepEdenKullaniciId));
        
        LocalDate bugun = LocalDate.now();
        LocalDate birHaftaSonra = bugun.plusWeeks(1);

        return personelVardiyaRepository.findByPersonel_IdAndTarihBetweenOrderByTarihAsc(personel.getId(), bugun, birHaftaSonra).stream()
                .map(this::convertToGoruntuleDTO)
                .collect(Collectors.toList());
    }


    // --- Helper Metotlar ---
    private PersonelVardiyaGoruntuleDTO convertToGoruntuleDTO(PersonelVardiya pv) {
        if (pv == null) return null;
        return PersonelVardiyaGoruntuleDTO.builder()
                .id(pv.getId())
                .personelId(pv.getPersonel().getId())
                .personelAdiSoyadi(pv.getPersonel().getAd() + " " + pv.getPersonel().getSoyad())
                .vardiyaId(pv.getVardiya().getId())
                .vardiyaAdi(pv.getVardiya().getAd())
                .vardiyaBaslangicSaati(pv.getVardiya().getBaslangicSaati())
                .vardiyaBitisSaati(pv.getVardiya().getBitisSaati())
                .tarih(pv.getTarih())
                .atamaTarihi(pv.getAtamaTarihi())
                .build();
    }

    private void checkAdminYetkisi(Integer kullaniciId, String islemTuru) {
        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("İşlemi yapan kullanıcı bulunamadı."));
        boolean isAdmin = kullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        if (!isAdmin) {
            throw new AccessDeniedException("Kullanıcının '" + islemTuru + "' işlemi için yetkisi yok (Sadece ADMIN).");
        }
    }
    
    private void checkVardiyaGoruntulemeYetkisi(Integer hedeflenenPersonelKullaniciId, Integer talepEdenKullaniciId) {
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isKendiVardiyasi = hedeflenenPersonelKullaniciId.equals(talepEdenKullaniciId);

        if (!isAdmin && !isKendiVardiyasi) {
            throw new AccessDeniedException("Bu personelin vardiya bilgilerini görüntüleme yetkiniz yok.");
        }
    }

    private void checkGenelListeGoruntulemeYetkisi(Integer talepEdenKullaniciId, String listeTuru) {
         Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
            .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı."));
        boolean yetkili = talepEden.getRoller().stream()
            .anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN") || rol.getAd().equals("ROLE_YONETICI")); // YONETICI de görebilir
        if (!yetkili) {
            throw new AccessDeniedException("Kullanıcının '" + listeTuru + "' listesini görüntüleme yetkisi yok.");
        }
    }
}