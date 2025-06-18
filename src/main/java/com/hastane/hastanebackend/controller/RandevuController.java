package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
import com.hastane.hastanebackend.entity.Hasta; // Hasta entity importu
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.exception.ResourceNotFoundException; // ResourceNotFoundException importu
import com.hastane.hastanebackend.repository.HastaRepository; // HastaRepository importu
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.RandevuService;
import jakarta.validation.Valid;
import org.slf4j.Logger; // Logger importları
import org.slf4j.LoggerFactory; // Logger importları
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

    private static final Logger logger = LoggerFactory.getLogger(RandevuController.class); // Logger tanımlaması

    private final RandevuService randevuService;
    private final KullaniciRepository kullaniciRepository;
    private final HastaRepository hastaRepository; // HastaRepository bağımlılığı eklendi

    public RandevuController(RandevuService randevuService,
                             KullaniciRepository kullaniciRepository,
                             HastaRepository hastaRepository) { // Constructor'a eklendi
        this.randevuService = randevuService;
        this.kullaniciRepository = kullaniciRepository;
        this.hastaRepository = hastaRepository; // Atama yapıldı
    }

    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("getAktifKullaniciId çağrıldı ancak aktif kullanıcı bulunamadı veya kimlik doğrulanmamış.");
            throw new IllegalStateException("Aktif kullanıcı bulunamadı veya kimlik doğrulanmamış.");
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
    
    // YENİ EKLENEN ENDPOINT: Giriş yapmış hastanın kendi randevularını getirir
    @GetMapping("/hasta/mevcut")
    @PreAuthorize("hasRole('HASTA')") // Sadece HASTA rolüne sahip olanlar erişebilir
    public ResponseEntity<List<RandevuGoruntuleDTO>> getMyRandevular() {
        logger.info("GET /api/randevular/hasta/mevcut çağrıldı.");
        try {
            Integer aktifKullaniciId = getAktifKullaniciId();
            
            // Aktif kullanıcının Hasta profilini bul
            Hasta hasta = hastaRepository.findByKullanici_Id(aktifKullaniciId)
                .orElseThrow(() -> {
                    logger.warn("Kullanıcı ID {} için hasta profili bulunamadı.", aktifKullaniciId);
                    return new ResourceNotFoundException("Bu kullanıcıya ait hasta profili bulunamadı.");
                });

            List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByHastaId(hasta.getId(), aktifKullaniciId);
            return ResponseEntity.ok(randevular);
        } catch (ResourceNotFoundException e) {
            logger.warn("Mevcut hastanın randevuları getirilirken kaynak bulunamadı: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Veya uygun bir hata mesajı
        } catch (Exception e) {
            logger.error("Mevcut hastanın randevuları getirilirken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste daha detaylı yapılabilir
    public ResponseEntity<List<RandevuGoruntuleDTO>> getRandevularByHastaId(@PathVariable Integer hastaId) {
        logger.debug("GET /api/randevular/hasta/{} çağrıldı.", hastaId);
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByHastaId(hastaId, talepEdenKullaniciId);
        return ResponseEntity.ok(randevular);
    }

    @GetMapping("/doktor/{doktorId}")
    @PreAuthorize("isAuthenticated()") // Yetki kontrolü serviste daha detaylı yapılabilir
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
            @RequestParam String yeniDurum) { // DTO kullanmak daha iyi olabilir: @RequestBody RandevuDurumGuncelleDTO dto
        logger.info("PUT /api/randevular/{}/durum çağrıldı. Yeni Durum: {}", randevuId, yeniDurum);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
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
    @PreAuthorize("isAuthenticated()") // Yetki serviste daha detaylı
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