package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.MuayeneGoruntuleDTO;
import com.hastane.hastanebackend.dto.MuayeneOlusturDTO;
import com.hastane.hastanebackend.entity.Kullanici; // Kullanici importu zaten vardı
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.service.MuayeneService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired eklendi (isteğe bağlı ama iyi pratik)
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/muayeneler")
public class MuayeneController {

    private static final Logger logger = LoggerFactory.getLogger(MuayeneController.class);
    private final MuayeneService muayeneService;
    private final KullaniciRepository kullaniciRepository;

    @Autowired // Constructor injection için eklendi (Spring'in yeni versiyonlarında zorunlu değil ama açıkça belirtmek iyi)
    public MuayeneController(MuayeneService muayeneService, KullaniciRepository kullaniciRepository) {
        this.muayeneService = muayeneService;
        this.kullaniciRepository = kullaniciRepository;
    }

    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            logger.warn("getAktifKullaniciId: Kimlik doğrulanmamış veya geçersiz principal.");
            throw new IllegalStateException("Bu işlem için kimlik doğrulaması gerekmektedir.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> {
                    logger.error("getAktifKullaniciId: Aktif kullanıcı veritabanında bulunamadı. Email: {}", userDetails.getUsername());
                    // ResourceNotFoundException fırlatmak daha uygun olabilir
                    return new ResourceNotFoundException("Aktif kullanıcı bilgileri alınamadı: " + userDetails.getUsername());
                });
        return kullanici.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<?> muayeneOlustur(@Valid @RequestBody MuayeneOlusturDTO muayeneOlusturDTO) {
        try {
            Integer doktorKullaniciId = getAktifKullaniciId();
            MuayeneGoruntuleDTO olusturulanMuayene = muayeneService.muayeneOlustur(muayeneOlusturDTO, doktorKullaniciId);
            return new ResponseEntity<>(olusturulanMuayene, HttpStatus.CREATED);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            logger.warn("Muayene oluşturma hatası (client error): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.warn("Muayene oluşturma yetki hatası: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Muayene oluşturulurken beklenmedik bir hata oluştu:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene kaydı oluşturulurken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/{muayeneId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMuayeneById(@PathVariable Integer muayeneId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            Optional<MuayeneGoruntuleDTO> muayeneDTO = muayeneService.getMuayeneById(muayeneId, talepEdenKullaniciId);
            return muayeneDTO.<ResponseEntity<?>>map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Belirtilen ID ile muayene bulunamadı."));
        } catch (AccessDeniedException e) {
            logger.warn("Muayene görüntüleme yetki hatası (ID: {}): {}", muayeneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) { // Eğer getAktifKullaniciId içinde fırlatılırsa
             logger.warn("Muayene (ID: {}) görüntülenirken talep eden kullanıcı bulunamadı: {}", muayeneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Muayene (ID: {}) getirilirken beklenmedik bir hata oluştu:", muayeneId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene bilgileri getirilirken sunucu hatası oluştu.");
        }
    }

    // YENİ EKLENEN ENDPOINT
    @GetMapping("/randevu/{randevuId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMuayeneByRandevuId(@PathVariable Integer randevuId) {
        logger.debug("GET /api/muayeneler/randevu/{} çağrıldı.", randevuId);
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            // Servis katmanında findByRandevuId metodu talepEdenKullaniciId'yi yetki için kullanabilir.
            Optional<MuayeneGoruntuleDTO> muayeneDTO = muayeneService.findDtoByRandevuId(randevuId, talepEdenKullaniciId);

            return muayeneDTO
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bu randevuya ait muayene kaydı bulunamadı."));
        } catch (AccessDeniedException e) {
            logger.warn("Muayene (randevu ID: {}) görüntüleme yetki hatası: {}", randevuId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
             logger.warn("Muayene (randevu ID: {}) görüntülenirken bir kaynak bulunamadı: {}", randevuId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Randevu ID {} için muayene getirilirken beklenmedik bir hata oluştu:", randevuId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene bilgileri getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMuayenelerByHastaId(@PathVariable Integer hastaId) {
         try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<MuayeneGoruntuleDTO> muayeneler = muayeneService.getMuayenelerByHastaId(hastaId, talepEdenKullaniciId);
            return ResponseEntity.ok(muayeneler);
        } catch (AccessDeniedException e) {
            logger.warn("Hastanın (ID: {}) muayenelerini görüntüleme yetki hatası: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
             logger.warn("Hastanın (ID: {}) muayeneleri görüntülenirken talep eden kullanıcı bulunamadı: {}", hastaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Hastanın (ID: {}) muayeneleri getirilirken beklenmedik bir hata oluştu:", hastaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene listesi getirilirken sunucu hatası oluştu.");
        }
    }

    @GetMapping("/doktor/{doktorPersonelId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMuayenelerByDoktorIdAndGun(
            @PathVariable Integer doktorPersonelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate gun) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            List<MuayeneGoruntuleDTO> muayeneler = muayeneService.getMuayenelerByDoktorIdAndGun(doktorPersonelId, gun, talepEdenKullaniciId);
            return ResponseEntity.ok(muayeneler);
        } catch (AccessDeniedException e) {
            logger.warn("Doktorun (Personel ID: {}) muayenelerini görüntüleme yetki hatası (Gün: {}): {}", doktorPersonelId, gun, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
             logger.warn("Doktorun (Personel ID: {}) muayeneleri görüntülenirken talep eden kullanıcı bulunamadı: {}", doktorPersonelId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
         catch (Exception e) {
            logger.error("Doktorun (Personel ID: {}) muayeneleri (Gün: {}) getirilirken beklenmedik bir hata oluştu:", doktorPersonelId, gun, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene listesi getirilirken sunucu hatası oluştu.");
        }
    }

    @PutMapping("/{muayeneId}")
    @PreAuthorize("hasRole('DOKTOR')")
    public ResponseEntity<?> muayeneGuncelle(
            @PathVariable Integer muayeneId,
            @Valid @RequestBody MuayeneOlusturDTO guncelMuayeneDTO) {
        try {
            Integer doktorKullaniciId = getAktifKullaniciId();
            MuayeneGoruntuleDTO guncellenmisMuayene = muayeneService.muayeneGuncelle(muayeneId, guncelMuayeneDTO, doktorKullaniciId);
            return ResponseEntity.ok(guncellenmisMuayene);
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            logger.warn("Muayene güncelleme hatası (client error, ID: {}): {}", muayeneId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.warn("Muayene güncelleme yetki hatası (ID: {}): {}", muayeneId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Muayene (ID: {}) güncellenirken beklenmedik bir hata oluştu:", muayeneId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Muayene güncellenirken sunucu hatası oluştu.");
        }
    }
}