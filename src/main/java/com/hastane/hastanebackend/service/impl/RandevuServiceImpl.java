package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.RandevuGoruntuleDTO;
import com.hastane.hastanebackend.dto.RandevuOlusturDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.Randevu;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.RandevuRepository;
import com.hastane.hastanebackend.service.RandevuService;
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
public class RandevuServiceImpl implements RandevuService {

    private static final Logger logger = LoggerFactory.getLogger(RandevuServiceImpl.class);

    private final RandevuRepository randevuRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;

    @Autowired
    public RandevuServiceImpl(RandevuRepository randevuRepository,
                              HastaRepository hastaRepository,
                              PersonelRepository personelRepository,
                              KullaniciRepository kullaniciRepository) {
        this.randevuRepository = randevuRepository;
        this.hastaRepository = hastaRepository;
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    @Transactional
    public RandevuGoruntuleDTO randevuOlustur(RandevuOlusturDTO dto, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + dto.getHastaId()));

        Personel doktor = personelRepository.findById(dto.getDoktorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, ID: " + dto.getDoktorId()));

        // TODO: Doktorun ROLE_DOKTOR olup olmadığını kontrol et (Personel entity'sindeki Kullanici üzerinden)
        boolean isDoktorRole = doktor.getKullanici().getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if (!isDoktorRole) {
             logger.warn("Randevu oluşturma: Belirtilen personel (ID: {}) doktor rolünde değil.", doktor.getId());
             throw new IllegalArgumentException("Randevu sadece doktorlara atanabilir.");
        }
        
        // TODO: Randevu çakışması kontrolü
        Optional<Randevu> doktorRandevuCakismasi = randevuRepository.findByDoktor_IdAndRandevuTarihiSaati(doktor.getId(), dto.getRandevuTarihiSaati());
        if (doktorRandevuCakismasi.isPresent()) {
            throw new IllegalArgumentException("Doktorun bu saatte zaten bir randevusu bulunmaktadır.");
        }
        Optional<Randevu> hastaRandevuCakismasi = randevuRepository.findByHasta_IdAndRandevuTarihiSaati(hasta.getId(), dto.getRandevuTarihiSaati());
        if (hastaRandevuCakismasi.isPresent()) {
            throw new IllegalArgumentException("Hastanın bu saatte zaten bir randevusu bulunmaktadır.");
        }
        // TODO: Doktorun çalışma saatleri içinde mi kontrolü

        Randevu randevu = new Randevu();
        randevu.setHasta(hasta);
        randevu.setDoktor(doktor);
        randevu.setRandevuTarihiSaati(dto.getRandevuTarihiSaati());
        randevu.setDurum("PLANLANDI"); 

        Randevu kaydedilmisRandevu = randevuRepository.save(randevu);
        return convertToGoruntuleDTO(kaydedilmisRandevu);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RandevuGoruntuleDTO> getRandevuById(Integer randevuId, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Randevu randevu = randevuRepository.findById(randevuId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı, ID: " + randevuId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek yapan kullanıcı bulunamadı."));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliHasta = randevu.getHasta().getKullanici() != null && randevu.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isIlgiliDoktor = randevu.getDoktor().getKullanici() != null && randevu.getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliHasta && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu randevuyu görüntüleme yetkiniz yok.");
        }

        return Optional.of(convertToGoruntuleDTO(randevu));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RandevuGoruntuleDTO> getRandevularByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Hasta hasta = hastaRepository.findById(hastaId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + hastaId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek yapan kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliHasta = hasta.getKullanici() != null && hasta.getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliHasta) {
            throw new AccessDeniedException("Bu hastanın randevularını görüntüleme yetkiniz yok.");
        }

        List<Randevu> randevular = randevuRepository.findByHasta_IdOrderByRandevuTarihiSaatiDesc(hastaId);
        return randevular.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RandevuGoruntuleDTO> getRandevularByDoktorIdAndGun(Integer doktorId, LocalDate gun, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Personel doktor = personelRepository.findById(doktorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı, ID: " + doktorId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek yapan kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliDoktor = doktor.getKullanici() != null && doktor.getKullanici().getId().equals(talepEdenKullaniciId);
        
        if (!isAdmin && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu doktorun randevularını görüntüleme yetkiniz yok.");
        }

        LocalDateTime gunBaslangic = gun.atStartOfDay();
        LocalDateTime gunBitis = gun.atTime(23, 59, 59);
        List<Randevu> randevular = randevuRepository.findByDoktor_IdAndRandevuTarihiSaatiBetween(doktorId, gunBaslangic, gunBitis);
        return randevular.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    // **** YENİ EKLENEN METODUN IMPLEMENTASYONU ****
    @Override
    @Transactional(readOnly = true)
    public List<RandevuGoruntuleDTO> getTumRandevularByDoktorId(Integer doktorId) {
        logger.info("Doktor ID {} için tüm randevular getiriliyor.", doktorId);
        // Doktor var mı diye kontrol etmeye gerek yok, çünkü bu metot genellikle
        // controller'dan zaten doğrulanmış bir doktor ID'si ile çağrılır.
        // Ama istenirse eklenebilir:
        // personelRepository.findById(doktorId).orElseThrow(() -> new ResourceNotFoundException("Doktor bulunamadı: " + doktorId));
        
        List<Randevu> randevular = randevuRepository.findByDoktor_IdOrderByRandevuTarihiSaatiDesc(doktorId);
        return randevular.stream()
                         .map(this::convertToGoruntuleDTO)
                         .collect(Collectors.toList());
    }
    // **** YENİ METOT SONU ****

    @Override
    @Transactional
    public RandevuGoruntuleDTO randevuDurumGuncelle(Integer randevuId, String yeniDurum, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Randevu randevu = randevuRepository.findById(randevuId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı, ID: " + randevuId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek yapan kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliDoktor = randevu.getDoktor().getKullanici() != null && randevu.getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu randevunun durumunu güncelleme yetkiniz yok.");
        }

        // TODO: Geçerli durum geçişlerini kontrol et
        randevu.setDurum(yeniDurum);
        Randevu guncellenmisRandevu = randevuRepository.save(randevu);
        return convertToGoruntuleDTO(guncellenmisRandevu);
    }

    @Override
    @Transactional
    public RandevuGoruntuleDTO randevuIptalEt(Integer randevuId, Integer talepEdenKullaniciId) {
        // ... (mevcut kodunuz aynı kalacak)
        Randevu randevu = randevuRepository.findById(randevuId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı, ID: " + randevuId));

        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("İstek yapan kullanıcı bulunamadı."));
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> rol.getAd().equals("ROLE_ADMIN"));
        boolean isIlgiliHasta = randevu.getHasta().getKullanici() != null && randevu.getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isIlgiliDoktor = randevu.getDoktor().getKullanici() != null && randevu.getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);

        if (!isAdmin && !isIlgiliHasta && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu randevuyu iptal etme yetkiniz yok.");
        }

        // TODO: İptal etme koşullarını kontrol et
        if ("TAMAMLANDI".equals(randevu.getDurum())) {
            throw new IllegalArgumentException("Tamamlanmış bir randevu iptal edilemez.");
        }
        if ("IPTAL EDILDI".equals(randevu.getDurum())) {
            throw new IllegalArgumentException("Bu randevu zaten iptal edilmiş.");
        }
        randevu.setDurum("IPTAL EDILDI");
        Randevu iptalEdilmisRandevu = randevuRepository.save(randevu);
        return convertToGoruntuleDTO(iptalEdilmisRandevu);
    }

    private RandevuGoruntuleDTO convertToGoruntuleDTO(Randevu randevu) {
        // ... (mevcut convertToGoruntuleDTO metodunuz aynı kalacak)
        RandevuGoruntuleDTO dto = new RandevuGoruntuleDTO();
        dto.setId(randevu.getId());
        dto.setRandevuTarihiSaati(randevu.getRandevuTarihiSaati());
        dto.setDurum(randevu.getDurum());
        dto.setHastaId(randevu.getHasta().getId());
        dto.setDoktorId(randevu.getDoktor().getId());

        if (randevu.getHasta() != null) {
            dto.setHastaAdiSoyadi(randevu.getHasta().getAd() + " " + randevu.getHasta().getSoyad());
        }
        if (randevu.getDoktor() != null) {
            dto.setDoktorAdiSoyadi(randevu.getDoktor().getAd() + " " + randevu.getDoktor().getSoyad());
            if (randevu.getDoktor().getDoktorDetay() != null && randevu.getDoktor().getDoktorDetay().getBrans() != null) {
                dto.setDoktorBransAdi(randevu.getDoktor().getDoktorDetay().getBrans().getAd());
            }
        }
        return dto;
    }
}