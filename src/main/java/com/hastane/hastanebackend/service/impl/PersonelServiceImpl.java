package com.hastane.hastanebackend.service.impl;

import com.hastane.hastanebackend.dto.PersonelDTO;
import com.hastane.hastanebackend.entity.Brans;
import com.hastane.hastanebackend.entity.Departman;
import com.hastane.hastanebackend.entity.DoktorDetay;
import com.hastane.hastanebackend.entity.Kullanici;
import com.hastane.hastanebackend.entity.Personel;
import com.hastane.hastanebackend.entity.Rol;
import com.hastane.hastanebackend.exception.ResourceNotFoundException;
import com.hastane.hastanebackend.repository.BransRepository;
import com.hastane.hastanebackend.repository.DepartmanRepository;
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.RolRepository;
import com.hastane.hastanebackend.service.PersonelService;
import org.slf4j.Logger; // Logger importu eklendi
import org.slf4j.LoggerFactory; // Logger importu eklendi
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonelServiceImpl implements PersonelService {

    private static final Logger logger = LoggerFactory.getLogger(PersonelServiceImpl.class); // Logger tanımlaması

    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final RolRepository rolRepository;
    private final DepartmanRepository departmanRepository;
    private final PasswordEncoder passwordEncoder;
    private final BransRepository bransRepository;

    public PersonelServiceImpl(PersonelRepository personelRepository,
                               KullaniciRepository kullaniciRepository,
                               RolRepository rolRepository,
                               DepartmanRepository departmanRepository,
                               PasswordEncoder passwordEncoder,
                               BransRepository bransRepository) {
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.rolRepository = rolRepository;
        this.departmanRepository = departmanRepository;
        this.passwordEncoder = passwordEncoder;
        this.bransRepository = bransRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personel> getAllPersoneller() {
        logger.info("Tüm personeller getiriliyor.");
        return personelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Personel> getPersonelById(Integer id) {
        logger.debug("Personel ID {} için detaylar getiriliyor.", id);
        return personelRepository.findById(id);
    }

    @Override
    @Transactional
    public Personel createPersonel(PersonelDTO dto) {
        logger.info("Yeni personel oluşturuluyor. Email: {}", dto.getEmail());
        if (kullaniciRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kullanılıyor: " + dto.getEmail());
        }
        if (!StringUtils.hasText(dto.getSifre())) {
            throw new IllegalArgumentException("Şifre boş bırakılamaz.");
        }
        if (dto.getRoller() == null || dto.getRoller().isEmpty()) {
            throw new IllegalArgumentException("Personele en az bir rol atanmalıdır.");
        }

        Kullanici yeniKullanici = new Kullanici();
        yeniKullanici.setEmail(dto.getEmail());
        yeniKullanici.setSifre(passwordEncoder.encode(dto.getSifre()));
        yeniKullanici.setAktifMi(true);

        Set<Rol> roller = dto.getRoller().stream()
                .map(rolAdi -> rolRepository.findByAd(rolAdi)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + rolAdi)))
                .collect(Collectors.toSet());
        yeniKullanici.setRoller(roller);

        Personel yeniPersonel = new Personel();
        yeniPersonel.setAd(dto.getAd());
        yeniPersonel.setSoyad(dto.getSoyad());
        yeniPersonel.setTelefon(dto.getTelefon());
        yeniPersonel.setKullanici(yeniKullanici); // Cascade ile Kullanici da kaydedilecek

        if (dto.getDepartmanId() != null) {
            Departman departman = departmanRepository.findById(dto.getDepartmanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departman bulunamadı, id: " + dto.getDepartmanId()));
            yeniPersonel.setDepartman(departman);
        }

        if (dto.getBransId() != null && roller.stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()))) {
            Brans brans = bransRepository.findById(dto.getBransId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branş bulunamadı, id: " + dto.getBransId()));
            DoktorDetay doktorDetay = new DoktorDetay();
            doktorDetay.setBrans(brans);
            // doktorDetay.setPersonel(yeniPersonel); // Bu satır Personel entity'sindeki setDoktorDetay içinde yönetilmeli
            yeniPersonel.setDoktorDetay(doktorDetay); // Bu, DoktorDetay'daki personel alanını da set etmeli
        }
        
        Personel kaydedilmisPersonel = personelRepository.save(yeniPersonel);
        logger.info("Personel başarıyla oluşturuldu. ID: {}", kaydedilmisPersonel.getId());
        return kaydedilmisPersonel;
    }

    @Override
    @Transactional
    public Personel updatePersonelWithDTO(Integer id, PersonelDTO dto) {
        logger.info("Personel ID {} güncelleniyor. Email: {}", id, dto.getEmail());
        Personel mevcutPersonel = personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek personel bulunamadı, id: " + id));

        Kullanici mevcutKullanici = mevcutPersonel.getKullanici();
        if (mevcutKullanici == null) {
            // Bu durum normalde olmamalı, her personelin bir kullanıcısı olmalı
            logger.error("Personel ID {} için ilişkili kullanıcı bulunamadı!", id);
            throw new IllegalStateException("Personelin ilişkili bir kullanıcı hesabı bulunmuyor. Personel ID: " + id);
        }

        if (StringUtils.hasText(dto.getAd())) mevcutPersonel.setAd(dto.getAd());
        if (StringUtils.hasText(dto.getSoyad())) mevcutPersonel.setSoyad(dto.getSoyad());
        mevcutPersonel.setTelefon(dto.getTelefon()); // null olabilir, sorun değil

        if (dto.getDepartmanId() != null) {
            if (dto.getDepartmanId().equals(0)) { // "Departman Seçiniz" gibi bir değerse
                 mevcutPersonel.setDepartman(null);
            } else {
                Departman yeniDepartman = departmanRepository.findById(dto.getDepartmanId())
                        .orElseThrow(() -> new ResourceNotFoundException("Departman bulunamadı, id: " + dto.getDepartmanId()));
                mevcutPersonel.setDepartman(yeniDepartman);
            }
        } else {
            mevcutPersonel.setDepartman(null); // Departman kaldırılıyorsa
        }

        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equalsIgnoreCase(mevcutKullanici.getEmail())) {
            if (kullaniciRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor: " + dto.getEmail());
            }
            mevcutKullanici.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getSifre())) {
            mevcutKullanici.setSifre(passwordEncoder.encode(dto.getSifre()));
        }

        if (dto.getRoller() != null && !dto.getRoller().isEmpty()) {
            Set<Rol> yeniRoller = dto.getRoller().stream()
                    .map(rolAdi -> rolRepository.findByAd(rolAdi)
                            .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + rolAdi)))
                    .collect(Collectors.toSet());
            mevcutKullanici.setRoller(yeniRoller);
        }
        
        boolean isDoktor = mevcutKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        
        if (isDoktor) {
            if (dto.getBransId() != null) {
                 if (dto.getBransId().equals(0)) { // "Branş Seçiniz" gibi bir değerse ve doktor rolü varsa, branşı null yap
                    if (mevcutPersonel.getDoktorDetay() != null) {
                        mevcutPersonel.setDoktorDetay(null); // orphanRemoval=true ise DoktorDetay silinir
                    }
                 } else {
                    Brans brans = bransRepository.findById(dto.getBransId())
                            .orElseThrow(() -> new ResourceNotFoundException("Branş bulunamadı, id: " + dto.getBransId()));
                    DoktorDetay doktorDetay = mevcutPersonel.getDoktorDetay();
                    if (doktorDetay == null) {
                        doktorDetay = new DoktorDetay();
                        // doktorDetay.setPersonel(mevcutPersonel); // Bu Personel entity'sindeki setDoktorDetay ile yönetilmeli
                    }
                    doktorDetay.setBrans(brans);
                    mevcutPersonel.setDoktorDetay(doktorDetay); // Bu DoktorDetay'daki personel alanını da set etmeli
                 }
            } else { // Doktor rolü var ama bransId gelmemiş veya null (Bu durum frontend'de engellenmeli)
                logger.warn("Doktor rolündeki personel (ID: {}) için branş ID'si belirtilmedi. Doktor detayı güncellenmiyor/kaldırılıyor.", id);
                if (mevcutPersonel.getDoktorDetay() != null) {
                     mevcutPersonel.setDoktorDetay(null);
                }
            }
        } else { // Doktor rolünde değilse, DoktorDetay olmamalı
            if (mevcutPersonel.getDoktorDetay() != null) {
                mevcutPersonel.setDoktorDetay(null); // orphanRemoval=true ise DoktorDetay silinir
            }
        }
        
        // Kullanici entity'si Personel üzerinden cascade ile güncellenmeyebilir, ayrıca save edilebilir.
        // Ancak Personel'deki @OneToOne(cascade = CascadeType.ALL) bunu yönetmeli.
        // kullaniciRepository.save(mevcutKullanici); // Eğer cascade ile güncellenmiyorsa
        Personel guncellenmisPersonel = personelRepository.save(mevcutPersonel);
        logger.info("Personel başarıyla güncellendi. ID: {}", guncellenmisPersonel.getId());
        return guncellenmisPersonel;
    }

    @Override
    @Transactional
    public void deletePersonel(Integer id) {
        logger.info("Personel ID {} siliniyor.", id);
        Personel personel = personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek personel bulunamadı, id: " + id));
        // Personel silindiğinde ilişkili Kullanici ve DoktorDetay (eğer varsa)
        // CascadeType.ALL ve orphanRemoval=true ayarları sayesinde otomatik silinmelidir.
        personelRepository.delete(personel);
        logger.info("Personel başarıyla silindi. ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personel> getPersonellerByRol(String rolAdi) {
        logger.info("{} rolündeki personeller getiriliyor.", rolAdi);
        return personelRepository.findByRolAdi(rolAdi);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personel> getDoktorlarByBrans(Integer bransId) {
        logger.info("Branş ID {} için doktorlar getiriliyor.", bransId);
        Brans brans = bransRepository.findById(bransId)
            .orElseThrow(() -> new ResourceNotFoundException("Branş bulunamadı: " + bransId));

        // Tüm doktorları çekip sonra branşa göre filtrele
        return personelRepository.findByRolAdi("ROLE_DOKTOR").stream()
                .filter(doktor -> doktor.getDoktorDetay() != null &&
                                   doktor.getDoktorDetay().getBrans() != null &&
                                   doktor.getDoktorDetay().getBrans().getId().equals(bransId))
                .collect(Collectors.toList());
    }
}