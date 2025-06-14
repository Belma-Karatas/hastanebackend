package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.DuyuruDTO;
import com.hastane.hastanebackend.entity.Duyuru;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.DuyuruRepository;
import com.hastane.hastanebackend.repository.PersonelRepository; // Yayınlayan personeli bulmak için
import com.hastane.hastanebackend.service.DuyuruService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // Yetki için
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DuyuruServiceImpl implements DuyuruService {

    private static final Logger log = LoggerFactory.getLogger(DuyuruServiceImpl.class);

    private final DuyuruRepository duyuruRepository;
    private final PersonelRepository personelRepository; // Yayınlayan Admin'in Personel kaydını bulmak için

    @Autowired
    public DuyuruServiceImpl(DuyuruRepository duyuruRepository, PersonelRepository personelRepository) {
        this.duyuruRepository = duyuruRepository;
        this.personelRepository = personelRepository;
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private DuyuruDTO convertToDTO(Duyuru duyuru) {
        if (duyuru == null) {
            return null;
        }
        DuyuruDTO.DuyuruDTOBuilder builder = DuyuruDTO.builder()
                .id(duyuru.getId())
                .baslik(duyuru.getBaslik())
                .icerik(duyuru.getIcerik())
                .yayinTarihi(duyuru.getYayinTarihi());

        if (duyuru.getYayinlayanPersonel() != null) {
            builder.yayinlayanPersonelId(duyuru.getYayinlayanPersonel().getId());
            builder.yayinlayanPersonelAdiSoyadi(duyuru.getYayinlayanPersonel().getAd() + " " + duyuru.getYayinlayanPersonel().getSoyad());
        }
        return builder.build();
    }

    // Bu DTO'dan Entity'ye dönüşümde 'yayinlayanPersonel' ayrıca set edilecek.
    private Duyuru convertToEntity(DuyuruDTO duyuruDTO) {
        if (duyuruDTO == null) {
            return null;
        }
        Duyuru duyuru = new Duyuru();
        duyuru.setId(duyuruDTO.getId()); // Güncelleme için
        duyuru.setBaslik(duyuruDTO.getBaslik());
        duyuru.setIcerik(duyuruDTO.getIcerik());
        // yayinTarihi ve yayinlayanPersonel servis metotlarında set edilecek
        return duyuru;
    }
    // --- Dönüşüm Metotları Sonu ---

    @Override
    @Transactional(readOnly = true)
    public List<DuyuruDTO> getAllDuyurular() {
        log.info("Tüm duyurular getiriliyor.");
        return duyuruRepository.findAllByOrderByYayinTarihiDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DuyuruDTO> getDuyuruById(Integer id) {
        log.info("Duyuru ID {} için detaylar getiriliyor.", id);
        return duyuruRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public DuyuruDTO createDuyuru(DuyuruDTO duyuruDTO, Integer adminKullaniciId) {
        log.info("Yeni duyuru oluşturuluyor. Başlık: {}, Yapan Admin Kullanıcı ID: {}", duyuruDTO.getBaslik(), adminKullaniciId);

        // İşlemi yapan adminin Personel kaydını bul
        Personel yayinlayanAdmin = personelRepository.findByKullanici_Id(adminKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Duyuruyu yayınlayan admin personel kaydı bulunamadı. Kullanıcı ID: " + adminKullaniciId));
        // Admin rol kontrolü controller'da @PreAuthorize ile yapıldığı için burada tekrar gerek yok, ama eklenebilir.

        Duyuru duyuru = convertToEntity(duyuruDTO);
        duyuru.setId(null); // Yeni kayıt
        duyuru.setYayinlayanPersonel(yayinlayanAdmin);
        // yayinTarihi @CreationTimestamp ile otomatik set edilecek

        Duyuru kaydedilmisDuyuru = duyuruRepository.save(duyuru);
        log.info("Duyuru başarıyla oluşturuldu. ID: {}, Başlık: {}", kaydedilmisDuyuru.getId(), kaydedilmisDuyuru.getBaslik());
        return convertToDTO(kaydedilmisDuyuru);
    }

    @Override
    @Transactional
    public DuyuruDTO updateDuyuru(Integer id, DuyuruDTO duyuruDTO, Integer adminKullaniciId) {
        log.info("Duyuru ID {} güncelleniyor. Yapan Admin Kullanıcı ID: {}", id, adminKullaniciId);
        Duyuru mevcutDuyuru = duyuruRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek duyuru bulunamadı, ID: " + id));

        // Yetki kontrolü: Sadece duyuruyu yayınlayan admin veya başka bir admin güncelleyebilir.
        // Bu daha detaylı bir yetki kontrolü gerektirir. Şimdilik controller'daki @PreAuthorize('hasRole("ADMIN")') yeterli.
        // Eğer "sadece kendi yayınladığını güncelleyebilir" kuralı istenirse:
        // if (!mevcutDuyuru.getYayinlayanPersonel().getKullanici().getId().equals(adminKullaniciId)) {
        //     // Başka bir admin mi kontrolü...
        //     throw new AccessDeniedException("Bu duyuruyu güncelleme yetkiniz yok.");
        // }

        mevcutDuyuru.setBaslik(duyuruDTO.getBaslik());
        mevcutDuyuru.setIcerik(duyuruDTO.getIcerik());
        // Yayinlayan personel ve yayın tarihi genellikle güncellenmez.
        // Eğer güncellenmesi gerekiyorsa, DTO'ya bu alanlar eklenmeli ve burada set edilmeli.

        Duyuru guncellenmisDuyuru = duyuruRepository.save(mevcutDuyuru);
        log.info("Duyuru başarıyla güncellendi. ID: {}", guncellenmisDuyuru.getId());
        return convertToDTO(guncellenmisDuyuru);
    }

    @Override
    @Transactional
    public void deleteDuyuru(Integer id, Integer adminKullaniciId) {
        log.info("Duyuru ID {} siliniyor. Yapan Admin Kullanıcı ID: {}", id, adminKullaniciId);
        Duyuru duyuru = duyuruRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek duyuru bulunamadı, ID: " + id));

        // Yetki kontrolü: Sadece duyuruyu yayınlayan admin veya başka bir admin silebilir.
        // Controller'daki @PreAuthorize('hasRole("ADMIN")') genel admin yetkisini kontrol eder.
        // if (!mevcutDuyuru.getYayinlayanPersonel().getKullanici().getId().equals(adminKullaniciId)) { ... }

        duyuruRepository.delete(duyuru);
        log.info("Duyuru başarıyla silindi. ID: {}", id);
    }
}