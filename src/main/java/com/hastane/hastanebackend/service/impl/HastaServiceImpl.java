package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.HastaKayitDTO;
import com.hastane.hastanebackend.entity.Hasta;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.HastaRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.RolRepository;
import com.hastane.hastanebackend.service.HastaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class HastaServiceImpl implements HastaService {

    private final HastaRepository hastaRepository;
    private final KullaniciRepository kullaniciRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public HastaServiceImpl(HastaRepository hastaRepository,
                            KullaniciRepository kullaniciRepository,
                            RolRepository rolRepository,
                            PasswordEncoder passwordEncoder) {
        this.hastaRepository = hastaRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Hasta createHasta(HastaKayitDTO dto) {
        if (kullaniciRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanılıyor: " + dto.getEmail());
        }
        if (dto.getTcKimlikNo() != null && hastaRepository.existsByTcKimlikNo(dto.getTcKimlikNo())) {
            throw new IllegalArgumentException("Bu TC Kimlik Numarası zaten kayıtlı: " + dto.getTcKimlikNo());
        }

        // 1. Kullanici oluştur
        Kullanici yeniKullanici = new Kullanici();
        yeniKullanici.setEmail(dto.getEmail());
        yeniKullanici.setSifre(passwordEncoder.encode(dto.getSifre()));
        yeniKullanici.setAktifMi(true); // Yeni kullanıcı varsayılan olarak aktif

        // Kullanıcıya "ROLE_HASTA" rolünü ata
        // Bu rolün veritabanında önceden eklenmiş olması gerekir!
        Rol hastaRol = rolRepository.findByAd("ROLE_HASTA")
                .orElseThrow(() -> new ResourceNotFoundException("ROLE_HASTA rolü bulunamadı. Lütfen önce bu rolü oluşturun."));
        Set<Rol> roller = new HashSet<>();
        roller.add(hastaRol);
        yeniKullanici.setRoller(roller);

        // Kullanici entity'sine ad, soyad, telefon set edelim (DTO'daki basit yapıya göre)
        // Bu alanların Kullanici entity'sinde olduğundan emin olun.
        // Eğer Kullanici entity'sinde bu alanlar yoksa, bu satırları kaldırın veya DTO'yu/entity'yi güncelleyin.
        // Kullanici entity'mizde ad/soyad yoktu, onları ekleyelim veya sadece Hasta'da tutalım.
        // Şimdilik Kullanici entity'mizde ad/soyad olmadığını varsayarak devam ediyorum.
        // Eğer Kullanici'ya ad/soyad eklerseniz, aşağıdaki satırları açabilirsiniz:
        // yeniKullanici.setAd(dto.getAd());
        // yeniKullanici.setSoyad(dto.getSoyad());
        // yeniKullanici.setTelefon(dto.getTelefon());


        // 2. Hasta oluştur
        Hasta yeniHasta = new Hasta();
        // Hasta entity'sine ad, soyad, telefon set edelim (DTO'daki basit yapıya göre)
        yeniHasta.setAd(dto.getAd());
        yeniHasta.setSoyad(dto.getSoyad());
       

        yeniHasta.setTcKimlikNo(dto.getTcKimlikNo());
        yeniHasta.setDogumTarihi(dto.getDogumTarihi());
        yeniHasta.setCinsiyet(dto.getCinsiyet());
        yeniHasta.setKullanici(yeniKullanici); // İlişkiyi kur

        // Önce Kullanici kaydedilmeli mi, yoksa cascade ile Hasta kaydedilince Kullanici da kaydedilir mi?
        // Hasta entity'sindeki @OneToOne ilişkisinde cascade = CascadeType.ALL olduğu için,
        // sadece yeniHasta'yı kaydetmek, ilişkili yeniKullanici'yı da kaydeder.
        return hastaRepository.save(yeniHasta);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hasta> getHastaById(Integer id) {
        return hastaRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hasta> getAllHastalar() {
        return hastaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hasta> getHastaByTcKimlikNo(String tcKimlikNo) {
        return hastaRepository.findByTcKimlikNo(tcKimlikNo);
    }
}