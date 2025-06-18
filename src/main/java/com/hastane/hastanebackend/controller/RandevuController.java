package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel; // EKLENDİ (PersonelRepository için)
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository; // EKLENDİ
import com.hastane.hastanebackend.service.RandevuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/randevular")
public class RandevuController {

    private static final Logger logger = LoggerFactory.getLogger(RandevuController.class);

    private final RandevuService randevuService;
    private final KullaniciRepository kullaniciRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository; // EKLENDİ

    public RandevuController(RandevuService randevuService,
                             KullaniciRepository kullaniciRepository,
                             HastaRepository hastaRepository,
                             PersonelRepository personelRepository) { // personelRepository eklendi
        this.randevuService = randevuService;
        this.kullaniciRepository = kullaniciRepository;
        this.hastaRepository = hastaRepository;
        this.personelRepository = personelRepository; // EKLENDİ
    }

    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("getAktifKullaniciId çağrıldı ancak aktif kullanıcı bulunamadı veya kimlik doğrulanmamış.");
            // Daha spesifik bir exception fırlatmak daha iyi olabilir, örn: AuthenticationCredentialsNotFoundException
            // Ancak şimdilik IllegalStateException veya ResourceNotFoundException da iş görebilir.
            throw new ResourceNotFoundException("Bu işlem için kimlik doğrulaması gerekmektedir veya aktif kullanıcı bulunamadı.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    logger.error("Aktif kullanıcı '{}' veritabanında bulunamadı.", userDetails.getUsername());
                    return new ResourceNotFoundException("Aktif kullanıcı veritabanında bulunamadı: " + userDetails.getUsername());
                });
        return kullanici.getId();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HASTA', 'DOKTOR', 'ADMIN')")
    public ResponseEntity<?> randevuOlustur(@Valid @RequestBody RandevuOlusturDTO randevuOlusturDTO) {
        logger.info("POST /api/randevular çağrıldı. DTO: {}", randevuOlusturDTO);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            RandevuGoruntuleDTO olusturulanRandevu = randevuService.randevuOlustur(randevuOlusturDTO, talepEdenKullaniciId);
            logger.info("Randevu başarıyla oluşturuldu. ID: {}", olusturulanRandevu.getId());
            return new ResponseEntity<>(olusturulanRandevu, HttpStatus.CREATED);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            logger.warn("Randevu oluşturma hatası (client error): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Randevu oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu oluşturulurken bir hata oluştu.");
        }
    }

    @GetMapping("/{randevuId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RandevuGoruntuleDTO> getRandevuById(@PathVariable Integer randevuId) {
        logger.debug("GET /api/randevular/{} çağrıldı.", randevuId);
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        Optional<RandevuGoruntuleDTO> randevuDTO = randevuService.getRandevuById(randevuId, talepEdenKullaniciId);
        return randevuDTO.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @GetMapping("/hasta/mevcut")
    @PreAuthorize("hasRole('HASTA')")
    public ResponseEntity<List<RandevuGoruntuleDTO>> getMyRandevular() {
        logger.info("GET /api/randevular/hasta/mevcut çağrıldı.");
        try {
            Integer aktifKullaniciId = getAktifKullaniciId();
            Hasta hasta = hastaRepository.findByKullanici_Id(aktifKullaniciId)
                .orElseThrow(() -> {
                    logger.warn("Kullanıcı ID {} için hasta profili bulunamadı.", aktifKullaniciId);
                    return new ResourceNotFoundException("Bu kullanıcıya ait hasta profili bulunamadı.");
                });

            List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByHastaId(hasta.getId(), aktifKullaniciId);
            return ResponseEntity.ok(randevular);
        } catch (ResourceNotFoundException e) {
            logger.warn("Mevcut hastanın randevuları getirilirken kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Mevcut hastanın randevuları getirilirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // DOKTORUN TÜM RANDEVULARINI GETİRMEK İÇİN YENİ ENDPOINT
    @GetMapping("/doktor/tum")
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<List<RandevuGoruntuleDTO>> getDoktorunTumRandevulari(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        logger.info("GET /api/randevular/doktor/tum çağrıldı. Doktor email: {}", email);
        
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Giriş yapmış kullanıcı bulunamadı: " + email));
        
        Personel doktor = personelRepository.findByKullanici_Id(kullanici.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Doktor profili bulunamadı, Kullanıcı ID: " + kullanici.getId()));

        List<RandevuGoruntuleDTO> randevular = randevuService.getTumRandevularByDoktorId(doktor.getId());
        return ResponseEntity.ok(randevular);
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RandevuGoruntuleDTO>> getRandevularByHastaId(@PathVariable Integer hastaId) {
        logger.debug("GET /api/randevular/hasta/{} çağrıldı.", hastaId);
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByHastaId(hastaId, talepEdenKullaniciId);
        return ResponseEntity.ok(randevular);
    }

    @GetMapping("/doktor/{doktorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RandevuGoruntuleDTO>> getRandevularByDoktorIdAndGun(
            @PathVariable Integer doktorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate gun) {
        logger.debug("GET /api/randevular/doktor/{}?gun={} çağrıldı.", doktorId, gun);
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByDoktorIdAndGun(doktorId, gun, talepEdenKullaniciId);
        return ResponseEntity.ok(randevular);
    }

    @PutMapping("/{randevuId}/durum")
    @PreAuthorize("hasAnyRole('DOKTOR', 'ADMIN')")
    public ResponseEntity<?> randevuDurumGuncelle(
            @PathVariable Integer randevuId,
            @RequestParam String yeniDurum) { 
        logger.info("PUT /api/randevular/{}/durum çağrıldı. Yeni Durum: {}", randevuId, yeniDurum);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            // Not: randevuDurumGuncelle metodu yeniDurum'u direkt String olarak alıyor, bu yüzden body'e gerek yok.
            // Eğer body'de { "yeniDurum": "TAMAMLANDI" } gibi bir JSON bekliyorsanız @RequestBody kullanmalısınız.
            RandevuGoruntuleDTO guncellenmisRandevu = randevuService.randevuDurumGuncelle(randevuId, yeniDurum, talepEdenKullaniciId);
            logger.info("Randevu (ID: {}) durumu başarıyla '{}' olarak güncellendi.", randevuId, yeniDurum);
            return ResponseEntity.ok(guncellenmisRandevu);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            logger.warn("Randevu durumu güncelleme hatası (client error): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Randevu (ID: {}) durumu güncellenirken beklenmedik bir hata oluştu:", randevuId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu durumu güncellenirken bir hata oluştu.");
        }
    }

    @PutMapping("/{randevuId}/iptal")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> randevuIptalEt(@PathVariable Integer randevuId) {
        logger.info("PUT /api/randevular/{}/iptal çağrıldı.", randevuId);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            RandevuGoruntuleDTO iptalEdilenRandevu = randevuService.randevuIptalEt(randevuId, talepEdenKullaniciId);
            logger.info("Randevu (ID: {}) başarıyla iptal edildi.", randevuId);
            return ResponseEntity.ok(iptalEdilenRandevu);
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            logger.warn("Randevu iptal etme hatası (client error): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Randevu (ID: {}) iptal edilirken beklenmedik bir hata oluştu:", randevuId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu iptal edilirken bir hata oluştu.");
        }
    }
}