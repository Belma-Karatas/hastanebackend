package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.entity.Ilac;
import com.hastane.hastanebackend.exception.ResourceNotFoundException; // Kendi exception'ımızı kullanıyoruz
import com.hastane.hastanebackend.repository.IlacRepository;
import com.hastane.hastanebackend.service.IlacService;
import org.springframework.beans.factory.annotation.Autowired; // Constructor injection için gerekli değil ama alışkanlık
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // İşlemler için

import java.util.List;
import java.util.Optional;

@Service // Bu sınıfın bir Spring servis bileşeni olduğunu belirtir
public class IlacServiceImpl implements IlacService {

    private final IlacRepository ilacRepository;

    @Autowired // Spring'in modern versiyonlarında constructor injection için bu anotasyon zorunlu değil
    public IlacServiceImpl(IlacRepository ilacRepository) {
        this.ilacRepository = ilacRepository;
    }

    @Override
    @Transactional(readOnly = true) // Veritabanından sadece okuma yapılacağı için
    public List<Ilac> getAllIlaclar() {
        return ilacRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ilac> getIlacById(Integer id) {
        return ilacRepository.findById(id);
    }

    @Override
    @Transactional // Veritabanında değişiklik yapılacağı için
    public Ilac createIlac(Ilac ilac) {
        // İlaç adının benzersiz olup olmadığını kontrol et
        if (ilacRepository.existsByAd(ilac.getAd())) {
            throw new IllegalArgumentException("Bu ilaç adı zaten mevcut: " + ilac.getAd());
        }
        return ilacRepository.save(ilac);
    }

    @Override
    @Transactional
    public Ilac updateIlac(Integer id, Ilac ilacDetails) {
        Ilac mevcutIlac = ilacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("İlaç bulunamadı, ID: " + id));

        // Güncellenen adın başka bir ilaca ait olup olmadığını kontrol et (opsiyonel ama iyi bir pratik)
        // Eğer ad değişmişse ve yeni ad başkası tarafından kullanılıyorsa hata ver.
        if (ilacDetails.getAd() != null && !mevcutIlac.getAd().equals(ilacDetails.getAd())) {
            if (ilacRepository.existsByAd(ilacDetails.getAd())) {
                throw new IllegalArgumentException("Güncellenmek istenen ilaç adı ('" + ilacDetails.getAd() + "') zaten başka bir ilaç tarafından kullanılıyor.");
            }
            mevcutIlac.setAd(ilacDetails.getAd());
        }
        
        // Aciklama alanı kaldırıldığı için burası da kalktı.
        // if (ilacDetails.getAciklama() != null) {
        //     mevcutIlac.setAciklama(ilacDetails.getAciklama());
        // }

        return ilacRepository.save(mevcutIlac);
    }

    @Override
    @Transactional
    public void deleteIlac(Integer id) {
        Ilac ilac = ilacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek ilaç bulunamadı, ID: " + id));
        
        // TODO: İlacın reçetelerde kullanılıp kullanılmadığını kontrol et.
        // Eğer kullanılıyorsa, silme işlemi engellenebilir veya farklı bir strateji izlenebilir.
        // Örnek: if (receteIlacRepository.existsByIlac_Id(id)) { throw new DataIntegrityViolationException(...) }
        // Şimdilik doğrudan siliyoruz.
        ilacRepository.delete(ilac);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ilac> searchIlacByAdKeyword(String adKeyword) {
        if (adKeyword == null || adKeyword.trim().isEmpty()) {
            return ilacRepository.findAll(); // Boş arama yapılırsa tüm ilaçları döndür
        }
        return ilacRepository.findByAdContainingIgnoreCase(adKeyword);
    }
}