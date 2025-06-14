package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.ReceteGoruntuleDTO;
import com.hastane.hastanebackend.dto.ReceteIlacDetayDTO;
import com.hastane.hastanebackend.dto.ReceteOlusturDTO;
// ReceteIlacEkleDTO'yu ReceteIlacDetayDTO ile aynı kabul ettik, gerekirse ayrı oluşturulabilir.
// import com.hastane.hastanebackend.dto.ReceteIlacEkleDTO;

import com.hastane.hastanebackend.entity.*; // Tüm entity'ler
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.*; // Gerekli tüm repository'ler
import com.hastane.hastanebackend.service.ReceteService;

import org.slf4j.Logger; // Loglama için
import org.slf4j.LoggerFactory; // Loglama için
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReceteServiceImpl implements ReceteService {

    private static final Logger log = LoggerFactory.getLogger(ReceteServiceImpl.class);

    private final ReceteRepository receteRepository;
    private final MuayeneRepository muayeneRepository;
    private final IlacRepository ilacRepository;
    private final ReceteIlacRepository receteIlacRepository;
    private final KullaniciRepository kullaniciRepository;
    private final PersonelRepository personelRepository; // Doktoru doğrulamak için
    private final HastaRepository hastaRepository; // Hastayı doğrulamak için

    @Autowired
    public ReceteServiceImpl(ReceteRepository receteRepository,
                             MuayeneRepository muayeneRepository,
                             IlacRepository ilacRepository,
                             ReceteIlacRepository receteIlacRepository,
                             KullaniciRepository kullaniciRepository,
                             PersonelRepository personelRepository,
                             HastaRepository hastaRepository) {
        this.receteRepository = receteRepository;
        this.muayeneRepository = muayeneRepository;
        this.ilacRepository = ilacRepository;
        this.receteIlacRepository = receteIlacRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.personelRepository = personelRepository;
        this.hastaRepository = hastaRepository;
    }

    @Override
    @Transactional
    public ReceteGoruntuleDTO createRecete(ReceteOlusturDTO dto, Integer doktorKullaniciId) {
        log.info("createRecete çağrıldı. Doktor Kullanıcı ID: {}, Muayene ID: {}", doktorKullaniciId, dto.getMuayeneId());

        Kullanici aktifKullanici = kullaniciRepository.findById(doktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Aktif kullanıcı bulunamadı, ID: " + doktorKullaniciId));

        // Kullanıcının DOKTOR rolüne sahip olup olmadığını kontrol et
        boolean isDoktor = aktifKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        if (!isDoktor) {
            log.warn("Yetkisiz reçete oluşturma denemesi. Kullanıcı ID: {}", doktorKullaniciId);
            throw new AccessDeniedException("Sadece doktorlar reçete oluşturabilir.");
        }

        // Doktorun personel profilini al
        Personel doktor = personelRepository.findByKullanici_Id(doktorKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Doktor profili bulunamadı, Kullanıcı ID: " + doktorKullaniciId));

        Muayene muayene = muayeneRepository.findById(dto.getMuayeneId())
                .orElseThrow(() -> new ResourceNotFoundException("Muayene bulunamadı, ID: " + dto.getMuayeneId()));

        // Muayenenin bu doktora ait olup olmadığını kontrol et
        if (!muayene.getDoktor().getId().equals(doktor.getId())) {
            log.warn("Doktor, kendisine ait olmayan bir muayene için reçete oluşturmaya çalıştı. Doktor ID: {}, Muayene ID: {}", doktor.getId(), muayene.getId());
            throw new AccessDeniedException("Bu muayene için reçete oluşturma yetkiniz yok.");
        }

        // Bir muayeneye birden fazla reçete yazılmasını engelle (isteğe bağlı bir iş kuralı)
        if (receteRepository.existsByMuayene_Id(muayene.getId())) {
             log.warn("Muayeneye (ID: {}) zaten bir reçete yazılmış.", muayene.getId());
             throw new IllegalArgumentException("Bu muayene için zaten bir reçete oluşturulmuş.");
        }

        Recete recete = new Recete();
        recete.setMuayene(muayene);
        recete.setReceteTarihi(dto.getReceteTarihi());
        // olusturulmaZamani @CreationTimestamp ile otomatik set edilecek

        // İlaçları ReceteIlac olarak oluşturup Recete'ye ekle
        for (ReceteIlacDetayDTO ilacDetayDTO : dto.getIlaclar()) {
            Ilac ilac = ilacRepository.findById(ilacDetayDTO.getIlacId())
                    .orElseThrow(() -> new ResourceNotFoundException("İlaç bulunamadı, ID: " + ilacDetayDTO.getIlacId()));
            
            ReceteIlac receteIlac = new ReceteIlac();
            receteIlac.setIlac(ilac);
            receteIlac.setKullanimSekli(ilacDetayDTO.getKullanimSekli());
            // receteIlac.setRecete(recete); // Bu satır Recete'deki addReceteIlac içinde yapılıyor
            recete.addReceteIlac(receteIlac); // İki yönlü ilişkiyi kurar
        }

        Recete kaydedilmisRecete = receteRepository.save(recete);
        log.info("Reçete başarıyla oluşturuldu. Reçete ID: {}", kaydedilmisRecete.getId());
        return convertToGoruntuleDTO(kaydedilmisRecete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReceteGoruntuleDTO> getReceteById(Integer receteId, Integer talepEdenKullaniciId) {
        log.debug("getReceteById çağrıldı. Reçete ID: {}, Talep Eden Kullanıcı ID: {}", receteId, talepEdenKullaniciId);
        Recete recete = receteRepository.findById(receteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reçete bulunamadı, ID: " + receteId));

        checkReceteAccess(recete, talepEdenKullaniciId, "görüntüleme");
        return Optional.of(convertToGoruntuleDTO(recete));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceteGoruntuleDTO> getRecetelerByMuayeneId(Integer muayeneId, Integer talepEdenKullaniciId) {
        log.debug("getRecetelerByMuayeneId çağrıldı. Muayene ID: {}, Talep Eden Kullanıcı ID: {}", muayeneId, talepEdenKullaniciId);
        Muayene muayene = muayeneRepository.findById(muayeneId)
                .orElseThrow(() -> new ResourceNotFoundException("Muayene bulunamadı, ID: " + muayeneId));
        
        // Muayeneye ait reçeteyi (veya reçeteleri) görüntülerken de yetki kontrolü yapılmalı.
        // Muayeneyi görebilen kişi, reçetesini de görebilmeli.
        checkReceteAccess(muayene.getReceteler().stream().findFirst().orElse(null), talepEdenKullaniciId, "muayene üzerinden görüntüleme");

        List<Recete> receteler = receteRepository.findByMuayene_IdOrderByReceteTarihiDesc(muayeneId);
        return receteler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceteGoruntuleDTO> getRecetelerByHastaId(Integer hastaId, Integer talepEdenKullaniciId) {
        log.debug("getRecetelerByHastaId çağrıldı. Hasta ID: {}, Talep Eden Kullanıcı ID: {}", hastaId, talepEdenKullaniciId);
        if (!hastaRepository.existsById(hastaId)) {
            throw new ResourceNotFoundException("Hasta bulunamadı, ID: " + hastaId);
        }
        // Yetki kontrolü: Sadece ilgili hasta veya ADMIN görebilir
        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı, ID: " + talepEdenKullaniciId));
        
        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        Optional<Hasta> hastaProfiliTalepEden = hastaRepository.findByKullanici_Id(talepEdenKullaniciId);
        boolean isIlgiliHasta = hastaProfiliTalepEden.isPresent() && hastaProfiliTalepEden.get().getId().equals(hastaId);

        if (!isAdmin && !isIlgiliHasta) {
            log.warn("Yetkisiz reçete listeleme denemesi (hasta bazlı). Talep Eden ID: {}, Hedef Hasta ID: {}", talepEdenKullaniciId, hastaId);
            throw new AccessDeniedException("Bu hastanın reçetelerini görüntüleme yetkiniz yok.");
        }

        List<Recete> receteler = receteRepository.findByMuayene_Hasta_IdOrderByReceteTarihiDesc(hastaId);
        return receteler.stream().map(this::convertToGoruntuleDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReceteGoruntuleDTO addIlacToRecete(Integer receteId, ReceteIlacDetayDTO receteIlacEkleDTO, Integer doktorKullaniciId) {
        log.info("addIlacToRecete çağrıldı. Reçete ID: {}, İlaç ID: {}, Doktor Kullanıcı ID: {}", receteId, receteIlacEkleDTO.getIlacId(), doktorKullaniciId);
        Recete recete = receteRepository.findById(receteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reçete bulunamadı, ID: " + receteId));

        checkReceteAccess(recete, doktorKullaniciId, "ilaç ekleme (sadece yazan doktor)"); // Sadece yazan doktor ekleyebilir

        Ilac ilac = ilacRepository.findById(receteIlacEkleDTO.getIlacId())
                .orElseThrow(() -> new ResourceNotFoundException("İlaç bulunamadı, ID: " + receteIlacEkleDTO.getIlacId()));

        // Aynı ilacın reçetede olup olmadığını kontrol et
        boolean ilacZatenVar = recete.getReceteIlaclari().stream()
                                  .anyMatch(ri -> ri.getIlac().getId().equals(ilac.getId()));
        if (ilacZatenVar) {
            log.warn("İlaç (ID: {}) zaten reçetede (ID: {}) mevcut.", ilac.getId(), receteId);
            throw new IllegalArgumentException("Bu ilaç zaten reçetede mevcut.");
        }

        ReceteIlac yeniReceteIlac = new ReceteIlac();
        yeniReceteIlac.setIlac(ilac);
        yeniReceteIlac.setKullanimSekli(receteIlacEkleDTO.getKullanimSekli());
        recete.addReceteIlac(yeniReceteIlac); // Bu metot receteIlac.setRecete(this) de yapar.

        Recete guncellenmisRecete = receteRepository.save(recete); // Cascade ile ReceteIlac da kaydedilir.
        log.info("İlaç (ID: {}) reçeteye (ID: {}) başarıyla eklendi.", ilac.getId(), receteId);
        return convertToGoruntuleDTO(guncellenmisRecete);
    }

    @Override
    @Transactional
    public ReceteGoruntuleDTO removeIlacFromRecete(Integer receteId, Integer receteIlacId, Integer doktorKullaniciId) {
        log.info("removeIlacFromRecete çağrıldı. Reçete ID: {}, Reçeteİlaç ID: {}, Doktor Kullanıcı ID: {}", receteId, receteIlacId, doktorKullaniciId);
        Recete recete = receteRepository.findById(receteId)
                .orElseThrow(() -> new ResourceNotFoundException("Reçete bulunamadı, ID: " + receteId));

        checkReceteAccess(recete, doktorKullaniciId, "ilaç çıkarma (sadece yazan doktor)"); // Sadece yazan doktor çıkarabilir

        ReceteIlac silinecekReceteIlac = receteIlacRepository.findById(receteIlacId)
                .orElseThrow(() -> new ResourceNotFoundException("Reçeteden silinecek ilaç kaydı bulunamadı, ReceteIlac ID: " + receteIlacId));

        // Silinecek ReceteIlac'ın gerçekten bu reçeteye ait olup olmadığını kontrol et
        if (!silinecekReceteIlac.getRecete().getId().equals(recete.getId())) {
            log.warn("Silinmek istenen ReceteIlac (ID: {}) bu reçeteye (ID: {}) ait değil.", receteIlacId, receteId);
            throw new IllegalArgumentException("Silinmek istenen ilaç bu reçeteye ait değil.");
        }

        recete.removeReceteIlac(silinecekReceteIlac); // Bu metot receteIlac.setRecete(null) yapar.
                                                    // orphanRemoval=true olduğu için ReceteIlac kaydı DB'den silinir.
        Recete guncellenmisRecete = receteRepository.save(recete);
        log.info("İlaç kaydı (ReceteIlac ID: {}) reçeteden (ID: {}) başarıyla silindi.", receteIlacId, receteId);
        return convertToGoruntuleDTO(guncellenmisRecete);
    }

    // Helper metot: Recete entity'sini ReceteGoruntuleDTO'ya dönüştürür
    private ReceteGoruntuleDTO convertToGoruntuleDTO(Recete recete) {
        if (recete == null) return null;

        List<ReceteIlacDetayDTO> ilacDetaylari = recete.getReceteIlaclari().stream()
                .map(ri -> new ReceteIlacDetayDTO(
                        ri.getIlac().getId(),
                        ri.getKullanimSekli(),
                        ri.getIlac().getAd(), // ilacAdi
                        ri.getId()           // receteIlacId
                ))
                .collect(Collectors.toList());

        String doktorBransAdi = null;
        if (recete.getMuayene().getDoktor().getDoktorDetay() != null &&
            recete.getMuayene().getDoktor().getDoktorDetay().getBrans() != null) {
            doktorBransAdi = recete.getMuayene().getDoktor().getDoktorDetay().getBrans().getAd();
        }

        return ReceteGoruntuleDTO.builder()
                .id(recete.getId())
                .receteTarihi(recete.getReceteTarihi())
                .olusturulmaZamani(recete.getOlusturulmaZamani())
                .muayeneId(recete.getMuayene().getId())
                .hastaId(recete.getMuayene().getHasta().getId())
                .hastaAdiSoyadi(recete.getMuayene().getHasta().getAd() + " " + recete.getMuayene().getHasta().getSoyad())
                .doktorId(recete.getMuayene().getDoktor().getId())
                .doktorAdiSoyadi(recete.getMuayene().getDoktor().getAd() + " " + recete.getMuayene().getDoktor().getSoyad())
                .doktorBransAdi(doktorBransAdi)
                .ilaclar(ilacDetaylari)
                .build();
    }

    // Helper metot: Reçeteye erişim yetkisini kontrol eder
    private void checkReceteAccess(Recete recete, Integer talepEdenKullaniciId, String actionType) {
        if (recete == null && actionType.contains("muayene üzerinden")) {
            // Muayeneye ait reçete yoksa, ve muayene sahibi yetkiliyse, sorun yok.
            // Bu durum getRecetelerByMuayeneId'de muayene bulunduktan sonra çağrılır.
            // Bu metot muayenenin reçetesi üzerinden değil, genel yetki üzerinden kontrol yapmalı.
            // Şimdilik basitçe, eğer reçete null ise ve muayene üzerinden geliyorsa,
            // muayene sahibinin (doktor/hasta) yetkisi zaten dışarıda kontrol edilmiş olmalı.
            // Daha robust bir yapı için muayene nesnesini de parametre alabilir.
            return; 
        }
        if (recete == null) {
             throw new ResourceNotFoundException("Kontrol edilecek reçete bulunamadı.");
        }


        Kullanici talepEden = kullaniciRepository.findById(talepEdenKullaniciId)
                .orElseThrow(() -> new ResourceNotFoundException("Talep eden kullanıcı bulunamadı, ID: " + talepEdenKullaniciId));

        boolean isAdmin = talepEden.getRoller().stream().anyMatch(rol -> "ROLE_ADMIN".equals(rol.getAd()));
        boolean isIlgiliHasta = recete.getMuayene().getHasta().getKullanici() != null &&
                                recete.getMuayene().getHasta().getKullanici().getId().equals(talepEdenKullaniciId);
        boolean isReçeteyiYazanDoktor = recete.getMuayene().getDoktor().getKullanici() != null &&
                                     recete.getMuayene().getDoktor().getKullanici().getId().equals(talepEdenKullaniciId);

        if (actionType.equals("görüntüleme") || actionType.contains("muayene üzerinden görüntüleme")) {
            if (!isAdmin && !isIlgiliHasta && !isReçeteyiYazanDoktor) {
                log.warn("Yetkisiz reçete görüntüleme denemesi. Talep Eden ID: {}, Reçete ID: {}", talepEdenKullaniciId, recete.getId());
                throw new AccessDeniedException("Bu reçeteyi görüntüleme yetkiniz yok.");
            }
        } else if (actionType.contains("sadece yazan doktor")) { // İlaç ekleme/çıkarma
            if (!isReçeteyiYazanDoktor && !isAdmin) { // Admin de yapabilsin mi? Şimdilik evet.
                log.warn("Reçeteye (ID: {}) yetkisiz müdahale denemesi. Talep Eden ID: {}", recete.getId(), talepEdenKullaniciId);
                throw new AccessDeniedException("Sadece reçeteyi yazan doktor veya admin bu işlemi yapabilir.");
            }
        }
        // Diğer actionType'lar için ek kontroller eklenebilir.
    }
}