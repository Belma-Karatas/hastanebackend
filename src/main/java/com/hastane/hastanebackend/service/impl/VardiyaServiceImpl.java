package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.VardiyaDTO;
import com.hastane.hastanebackend.entity.Vardiya;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.VardiyaRepository;
// PersonelVardiyaRepository, deleteVardiya metodunda kontrol için ileride eklenecek.
// import com.hastane.hastanebackend.repository.PersonelVardiyaRepository;
import com.hastane.hastanebackend.service.VardiyaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VardiyaServiceImpl implements VardiyaService {

    private static final Logger log = LoggerFactory.getLogger(VardiyaServiceImpl.class);

    private final VardiyaRepository vardiyaRepository;
    // private final PersonelVardiyaRepository personelVardiyaRepository; // İleride eklenecek

    @Autowired
    public VardiyaServiceImpl(VardiyaRepository vardiyaRepository
                             /*, PersonelVardiyaRepository personelVardiyaRepository */) { // İleride eklenecek
        this.vardiyaRepository = vardiyaRepository;
        // this.personelVardiyaRepository = personelVardiyaRepository; // İleride eklenecek
    }

    // --- DTO ve Entity Dönüşüm Metotları ---
    private VardiyaDTO convertToDTO(Vardiya vardiya) {
        if (vardiya == null) {
            return null;
        }
        return new VardiyaDTO(vardiya.getId(), vardiya.getAd(), vardiya.getBaslangicSaati(), vardiya.getBitisSaati());
    }

    private Vardiya convertToEntity(VardiyaDTO vardiyaDTO) {
        if (vardiyaDTO == null) {
            return null;
        }
        Vardiya vardiya = new Vardiya();
        vardiya.setId(vardiyaDTO.getId()); // Güncelleme için
        vardiya.setAd(vardiyaDTO.getAd());
        vardiya.setBaslangicSaati(vardiyaDTO.getBaslangicSaati());
        vardiya.setBitisSaati(vardiyaDTO.getBitisSaati());
        return vardiya;
    }
    // --- Dönüşüm Metotları Sonu ---

    @Override
    @Transactional(readOnly = true)
    public List<VardiyaDTO> getAllVardiyalar() {
        log.info("Tüm vardiyalar getiriliyor.");
        return vardiyaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VardiyaDTO> getVardiyaById(Integer id) {
        log.info("Vardiya ID {} için detaylar getiriliyor.", id);
        return vardiyaRepository.findById(id)
                .map(this::convertToDTO);
    }

    private void validateVardiyaSaatleri(LocalTime baslangic, LocalTime bitis) {
        // Gece vardiyası (bitiş saati başlangıç saatinden önce veya eşitse ertesi gün biter)
        // Örneğin: Başlangıç 17:00, Bitiş 08:00. Bu geçerli.
        // Örneğin: Başlangıç 08:00, Bitiş 17:00. Bu geçerli.
        // Örneğin: Başlangıç 17:00, Bitiş 16:00. Bu geçersiz (aynı gün içinde bitiş başlangıçtan önce).
        // Bu basit kontrol, aynı gün içindeki mantıksızlığı yakalar.
        // Gece yarısını aşan vardiyalar için (örn: 22:00 - 06:00), bitisSaati < baslangicSaati durumu geçerlidir.
        // Burada sadece aynı gün içinde bitişin başlangıçtan önce olmamasını kontrol edebiliriz
        // veya daha karmaşık bir süre hesaplaması yapabiliriz.
        // Şimdilik, başlangıcın bitişten sonra olamayacağı (aynı gün mantığıyla) basit bir kontrol yapalım.
        // if (baslangic.isAfter(bitis) && !bitis.equals(LocalTime.MIDNIGHT)) {
        //     // Bu kontrol gece yarısını aşan vardiyaları yanlışlıkla engelleyebilir.
        //     // Daha iyi bir kontrol: Vardiya süresi pozitif mi?
        // }
        // Şimdilik saatlerin null olup olmadığı DTO'da @NotNull ile kontrol ediliyor.
        // Ekstra bir mantık hatası kontrolü (örn: süre çok kısa/uzun) eklenebilir.
    }


    @Override
    @Transactional
    public VardiyaDTO createVardiya(VardiyaDTO vardiyaDTO) {
        log.info("Yeni vardiya oluşturuluyor: {}", vardiyaDTO.getAd());
        if (vardiyaDTO.getAd() == null || vardiyaDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("Vardiya adı boş olamaz.");
        }
        if (vardiyaRepository.existsByAd(vardiyaDTO.getAd())) {
            throw new IllegalArgumentException("Bu vardiya adı zaten mevcut: " + vardiyaDTO.getAd());
        }
        validateVardiyaSaatleri(vardiyaDTO.getBaslangicSaati(), vardiyaDTO.getBitisSaati());

        Vardiya vardiya = convertToEntity(vardiyaDTO);
        vardiya.setId(null); // Yeni kayıt
        Vardiya kaydedilmisVardiya = vardiyaRepository.save(vardiya);
        log.info("Vardiya başarıyla oluşturuldu. ID: {}, Ad: {}", kaydedilmisVardiya.getId(), kaydedilmisVardiya.getAd());
        return convertToDTO(kaydedilmisVardiya);
    }

    @Override
    @Transactional
    public VardiyaDTO updateVardiya(Integer id, VardiyaDTO vardiyaDTO) {
        log.info("Vardiya ID {} güncelleniyor. Yeni ad: {}", id, vardiyaDTO.getAd());
        Vardiya mevcutVardiya = vardiyaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek vardiya bulunamadı, ID: " + id));

        if (vardiyaDTO.getAd() == null || vardiyaDTO.getAd().trim().isEmpty()) {
            throw new IllegalArgumentException("Vardiya adı boş olamaz.");
        }
        validateVardiyaSaatleri(vardiyaDTO.getBaslangicSaati(), vardiyaDTO.getBitisSaati());

        if (!mevcutVardiya.getAd().equalsIgnoreCase(vardiyaDTO.getAd()) && vardiyaRepository.existsByAd(vardiyaDTO.getAd())) {
            throw new IllegalArgumentException("Güncellenmek istenen vardiya adı ('" + vardiyaDTO.getAd() + "') zaten başka bir vardiyada kullanılıyor.");
        }
        
        mevcutVardiya.setAd(vardiyaDTO.getAd());
        mevcutVardiya.setBaslangicSaati(vardiyaDTO.getBaslangicSaati());
        mevcutVardiya.setBitisSaati(vardiyaDTO.getBitisSaati());
        
        Vardiya guncellenmisVardiya = vardiyaRepository.save(mevcutVardiya);
        log.info("Vardiya başarıyla güncellendi. ID: {}, Ad: {}", guncellenmisVardiya.getId(), guncellenmisVardiya.getAd());
        return convertToDTO(guncellenmisVardiya);
    }

    @Override
    @Transactional
    public void deleteVardiya(Integer id) {
        log.info("Vardiya ID {} siliniyor.", id);
        Vardiya vardiya = vardiyaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek vardiya bulunamadı, ID: " + id));

        // TODO: PersonelVardiyaRepository oluşturulduktan sonra, bu vardiyanın
        // herhangi bir personel vardiya atamasında kullanılıp kullanılmadığını kontrol et.
        // if (personelVardiyaRepository.existsByVardiya_Id(id)) {
        //     throw new IllegalStateException("Bu vardiya (ID: " + id + ") aktif personel vardiya atamalarında kullanıldığı için silinemez.");
        // }

        vardiyaRepository.delete(vardiya);
        log.info("Vardiya başarıyla silindi. ID: {}", id);
    }
}