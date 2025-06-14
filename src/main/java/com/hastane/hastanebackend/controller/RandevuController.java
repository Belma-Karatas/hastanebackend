package com.hastane.hastanebackend.controller;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
import com.hastane.hastanebackend.service.RandevuService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.hastane.hastanebackend.entity.Kullanici; // Kullanici entity'sini import et
import com.hastane.hastanebackend.repository.KullaniciRepository; // KullaniciRepository'yi import et
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/randevular")
public class RandevuController {

    private final RandevuService randevuService;
    private final KullaniciRepository kullaniciRepository; // Kullanici ID'sini almak için

    public RandevuController(RandevuService randevuService, KullaniciRepository kullaniciRepository) {
        this.randevuService = randevuService;
        this.kullaniciRepository = kullaniciRepository;
    }

    // Helper metot: Aktif kullanıcı ID'sini alır
    private Integer getAktifKullaniciId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Kullanıcı girişi yapılmamışsa veya anonimse uygun bir hata fırlat veya null dön.
            // Şimdilik basit bir hata fırlatalım.
            throw new IllegalStateException("Aktif kullanıcı bulunamadı veya kimlik doğrulanmamış.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // UserDetails'den Kullanici entity'sine ulaşıp ID'yi almamız gerekiyor.
        // UserDetailsImpl'de username olarak email saklanıyor.
        Kullanici kullanici = kullaniciRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Aktif kullanıcı veritabanında bulunamadı: " + userDetails.getUsername()));
        return kullanici.getId();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HASTA', 'DOKTOR', 'ADMIN')") // Hasta, Doktor veya Admin randevu oluşturabilir
    public ResponseEntity<?> randevuOlustur(@Valid @RequestBody RandevuOlusturDTO randevuOlusturDTO) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            // Servis katmanında, eğer talep eden HASTA ise dto.getHastaId()'nin talepEdenKullaniciId ile eşleştiği kontrol edilebilir.
            // Veya DTO'dan hastaId alınmayıp direkt talepEdenKullaniciId'nin hasta profili kullanılır.
            // Şimdilik DTO'dan gelen hastaId'yi kullanıyoruz, servis katmanı yetkiyi daha detaylı kontrol edebilir.
            RandevuGoruntuleDTO olusturulanRandevu = randevuService.randevuOlustur(randevuOlusturDTO, talepEdenKullaniciId);
            return new ResponseEntity<>(olusturulanRandevu, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Daha genel hata yönetimi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu oluşturulurken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/{randevuId}")
    @PreAuthorize("isAuthenticated()") // Sadece kimlik doğrulamış kullanıcılar (yetki kontrolü serviste)
    public ResponseEntity<RandevuGoruntuleDTO> getRandevuById(@PathVariable Integer randevuId) {
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        Optional<RandevuGoruntuleDTO> randevuDTO = randevuService.getRandevuById(randevuId, talepEdenKullaniciId);
        return randevuDTO.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/hasta/{hastaId}")
    @PreAuthorize("isAuthenticated()") // Yetki serviste
    public ResponseEntity<List<RandevuGoruntuleDTO>> getRandevularByHastaId(@PathVariable Integer hastaId) {
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByHastaId(hastaId, talepEdenKullaniciId);
        return ResponseEntity.ok(randevular);
    }

    @GetMapping("/doktor/{doktorId}")
    @PreAuthorize("isAuthenticated()") // Yetki serviste
    public ResponseEntity<List<RandevuGoruntuleDTO>> getRandevularByDoktorIdAndGun(
            @PathVariable Integer doktorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate gun) {
        Integer talepEdenKullaniciId = getAktifKullaniciId();
        List<RandevuGoruntuleDTO> randevular = randevuService.getRandevularByDoktorIdAndGun(doktorId, gun, talepEdenKullaniciId);
        return ResponseEntity.ok(randevular);
    }

    @PutMapping("/{randevuId}/durum")
    @PreAuthorize("hasAnyRole('DOKTOR', 'ADMIN')") // Sadece Doktor veya Admin durumu güncelleyebilir
    public ResponseEntity<?> randevuDurumGuncelle(
            @PathVariable Integer randevuId,
            @RequestParam String yeniDurum) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            RandevuGoruntuleDTO guncellenmisRandevu = randevuService.randevuDurumGuncelle(randevuId, yeniDurum, talepEdenKullaniciId);
            return ResponseEntity.ok(guncellenmisRandevu);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu durumu güncellenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @PutMapping("/{randevuId}/iptal")
    @PreAuthorize("isAuthenticated()") // Yetki serviste (ilgili hasta, doktor veya admin)
    public ResponseEntity<?> randevuIptalEt(@PathVariable Integer randevuId) {
        try {
            Integer talepEdenKullaniciId = getAktifKullaniciId();
            RandevuGoruntuleDTO iptalEdilenRandevu = randevuService.randevuIptalEt(randevuId, talepEdenKullaniciId);
            return ResponseEntity.ok(iptalEdilenRandevu);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Randevu iptal edilirken bir hata oluştu: " + e.getMessage());
        }
    }
}