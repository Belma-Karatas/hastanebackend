package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.MuayeneGoruntuleDTO;
import com.hastane.hastanebackend.dto.MuayeneOlusturDTO;
import com.hastane.hastanebackend.entity.*; // Hasta, Kullanici, Personel, Randevu, Muayene
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.*; // Gerekli tüm repository'ler
import com.hastane.hastanebackend.service.MuayeneService;
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

    private final MuayeneRepository muayeneRepository;
    private final RandevuRepository randevuRepository;
    private final HastaRepository hastaRepository;
    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;

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
        Kullanici aktifKullanici = kullaniciRepository.findById(doktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Aktif kullanıcı bulunamadı, ID: " + doktorKullaniciId));

        boolean isDoktorRole = aktifKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if (!isDoktorRole) {
            throw new AccessDeniedException("Sadece doktorlar muayene kaydı oluşturabilir.");
        }

        Personel doktor = personelRepository.findByKullanici_Id(doktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor profili bulunamadı, Kullanıcı ID: " + doktorKullaniciId));

        Hasta hasta = hastaRepository.findById(dto.getHastaId())
                .orElseThrow(() -> new ResourceNotFoundException("Hasta bulunamadı, ID: " + dto.getHastaId()));

        Randevu randevu = null;
        if (dto.getRandevuId() != null) {
            randevu = randevuRepository.findById(dto.getRandevuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Randevu bulunamadı, ID: " + dto.getRandevuId()));
            if (!randevu.getHasta().getId().equals(hasta.getId()) || !randevu.getDoktor().getId().equals(doktor.getId())) {
                throw new IllegalArgumentException("Randevu bilgileri muayene yapılacak hasta veya doktor ile eşleşmiyor.");
            }
            if (muayeneRepository.findByRandevu_Id(dto.getRandevuId()).isPresent()) {
                throw new IllegalArgumentException("Bu randevu için zaten bir muayene kaydı bulunmaktadır.");
            }
        }

        Muayene muayene = new Muayene();
        muayene.setRandevu(randevu);
        muayene.setHasta(hasta);
        muayene.setDoktor(doktor);
        muayene.setMuayeneTarihiSaati(dto.getMuayeneTarihiSaati());
        muayene.setHikaye(dto.getHikaye());
        muayene.setTani(dto.getTani());
        muayene.setTedaviNotlari(dto.getTedaviNotlari());

        Muayene kaydedilmisMuayene = muayeneRepository.save(muayene);
        return convertToGoruntuleDTO(kaydedilmisMuayene);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MuayeneGoruntuleDTO> getMuayeneById(Integer muayeneId, Integer talepEdenKullaniciId) {
        Muayene muayene = muayeneRepository.findById(muayeneId)
                .orElseThrow(() -> new ResourceNotFoundException("Muayene bulunamadı, ID: " + muayeneId));
        checkMuayeneAccess(muayene, talepEdenKullaniciId, "görüntüleme");
        return Optional.of(convertToGoruntuleDTO(muayene));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MuayeneGoruntuleDTO> getMuayenelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        // Hastanın varlığını kontrol etmeye gerek yok, findByHasta_IdOrderByMuayeneTarihiSaatiDesc boş liste döndürür.
        // Ancak yetki kontrolü için talep edenin hasta olup olmadığını kontrol edeceğiz.
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + talepEdenKullaniciId));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        
        Optional<Hasta> hastaProfiliTalepEden = hastaRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliHasta = hastaProfiliTalepEden.isPresent() && hastaProfiliTalepEden.get().getId().equals(hastaId);

        // Doktorların bir hastanın belirli muayenelerine erişimi olabilir (örn: kendi yaptığı muayeneler)
        // Bu senaryo için daha detaylı bir kontrol eklenebilir. Şimdilik sadece Admin ve ilgili hasta.
        if (!isAdmin && !isIlgiliHasta) {
            throw new AccessDeniedException("Bu hastanın muayenelerini görüntüleme yetkiniz yok.");
        }

        List<Muayene> muayeneler = muayeneRepository.findByHasta_IdOrderByMuayeneTarihiSaatiDesc(hastaId);
        return muayeneler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MuayeneGoruntuleDTO> getMuayenelerByDoktorIdAndGun(Integer doktorPersonelId, LocalDate gun, Integer talepEdenKullaniciId) {
        // Doktorun varlığını kontrol etmeye gerek yok, repository boş liste döndürür.
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + talepEdenKullaniciId));
        
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        
        Optional<Personel> personelProfiliTalepEden = personelRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliDoktor = personelProfiliTalepEden.isPresent() && personelProfiliTalepEden.get().getId().equals(doktorPersonelId);

        if (!isAdmin && !isIlgiliDoktor) {
            throw new AccessDeniedException("Bu doktorun muayenelerini görüntüleme yetkiniz yok.");
        }
        
        LocalDateTime gunBaslangic = gun.atStartOfDay();
        LocalDateTime gunBitis = gun.atTime(23, 59, 59);
        List<Muayene> muayeneler = muayeneRepository.findByDoktor_IdAndMuayeneTarihiSaatiBetween(doktorPersonelId, gunBaslangic, gunBitis);
        return muayeneler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MuayeneGoruntuleDTO muayeneGuncelle(Integer muayeneId, MuayeneOlusturDTO dto, Integer doktorKullaniciId) {
        Muayene mevcutMuayene = muayeneRepository.findById(muayeneId)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek muayene bulunamadı, ID: " + muayeneId));

        Kullanici aktifKullanici = kullaniciRepository.findById(doktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Aktif kullanıcı bulunamadı, ID: " + doktorKullaniciId));
        
        // Sadece muayeneyi yapan doktor güncelleyebilir (veya bir admin)
        boolean isAdmin = aktifKullanici.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isMuayeneyiYapanDoktor = mevcutMuayene.getDoktor().getKullanici().getId().equals(doktorKullaniciId);

        if (!isMuayeneyiYapanDoktor && !isAdmin) { // Admin her şeyi güncelleyebilir varsayımı
             throw new AccessDeniedException("Sadece muayeneyi oluşturan doktor veya admin bu kaydı güncelleyebilir.");
        }
        // Eğer admin'in sadece belirli koşullarda güncellemesi isteniyorsa, bu kontrol detaylandırılabilir.

        if (dto.getHikaye() != null) mevcutMuayene.setHikaye(dto.getHikaye());
        if (dto.getTani() != null) mevcutMuayene.setTani(dto.getTani());
        if (dto.getTedaviNotlari() != null) mevcutMuayene.setTedaviNotlari(dto.getTedaviNotlari());
        if (dto.getMuayeneTarihiSaati() != null) mevcutMuayene.setMuayeneTarihiSaati(dto.getMuayeneTarihiSaati());
        // Hasta, doktor, randevu gibi alanlar genellikle muayene güncellemede değiştirilmez.

        Muayene guncellenmisMuayene = muayeneRepository.save(mevcutMuayene);
        return convertToGoruntuleDTO(guncellenmisMuayene);
    }

    private MuayeneGoruntuleDTO convertToGoruntuleDTO(Muayene muayene) {
        MuayeneGoruntuleDTO.MuayeneGoruntuleDTOBuilder builder = MuayeneGoruntuleDTO.builder()
                .id(muayene.getId())
                .muayeneTarihiSaati(muayene.getMuayeneTarihiSaati())
                .hikaye(muayene.getHikaye())
                .tani(muayene.getTani())
                .tedaviNotlari(muayene.getTedaviNotlari())
                .olusturulmaTarihi(muayene.getOlusturulmaTarihi())
                .hastaId(muayene.getHasta().getId())
                .hastaAdiSoyadi(muayene.getHasta().getAd() + " " + muayene.getHasta().getSoyad())
                .doktorId(muayene.getDoktor().getId())
                .doktorAdiSoyadi(muayene.getDoktor().getAd() + " " + muayene.getDoktor().getSoyad());

        if (muayene.getRandevu() != null) {
            builder.randevuId(muayene.getRandevu().getId());
        }

        if (muayene.getDoktor().getDoktorDetay() != null && muayene.getDoktor().getDoktorDetay().getBrans() != null) {
            builder.doktorBransAdi(muayene.getDoktor().getDoktorDetay().getBrans().getAd());
        }
        return builder.build();
    }
    
    private void checkMuayeneAccess(Muayene muayene, Integer talepEdenKullaniciId, String action) {
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + talepEdenKullaniciId));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(r -> r.getAd().equals("ROLE_ADMIN"));
        
        Optional<Personel> personelProfiliTalepEden = personelRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliDoktor = personelProfiliTalepEden.isPresent() && personelProfiliTalepEden.get().getId().equals(muayene.getDoktor().getId());

        Optional<Hasta> hastaProfiliTalepEden = hastaRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliHasta = hastaProfiliTalepEden.isPresent() && hastaProfiliTalepEden.get().getId().equals(muayene.getHasta().getId());

        if (action.equals("görüntüleme")) {
            if (!isAdmin && !isIlgiliDoktor && !isIlgiliHasta) {
                throw new AccessDeniedException("Bu muayene kaydını görüntüleme yetkiniz yok.");
            }
        }
        // 'güncelleme' gibi diğer eylemler için yetki kontrolü doğrudan ilgili metot içinde yapılıyor,
        // çünkü kurallar daha spesifik olabilir (örn: sadece muayeneyi yapan doktor güncelleyebilir).
    }
}