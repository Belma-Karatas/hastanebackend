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
// import com.hastane.hastanebackend.repository.DoktorDetayRepository; // Eğer DoktorDetay için ayrı repo varsa
import com.hastane.hastanebackend.repository.KullaniciRepository;
import com.hastane.hastanebackend.repository.PersonelRepository;
import com.hastane.hastanebackend.repository.RolRepository;
import com.hastane.hastanebackend.service.PersonelService;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired constructor injection için gerekli değil ama bazen kullanılır
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

    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final RolRepository rolRepository;
    private final DepartmanRepository departmanRepository;
    private final PasswordEncoder passwordEncoder; // Bu alan zaten tanımlı
    private final BransRepository bransRepository;

    // Constructor Injection (Spring bu bağımlılıkları otomatik olarak sağlar)
    // @Autowired anotasyonu constructor üzerinde genellikle gerekli değildir Spring'in modern versiyonlarında
    public PersonelServiceImpl(PersonelRepository personelRepository,
                               KullaniciRepository kullaniciRepository,
                               RolRepository rolRepository,
                               DepartmanRepository departmanRepository,
                               PasswordEncoder passwordEncoder, // PasswordEncoder burada enjekte ediliyor
                               BransRepository bransRepository) {
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.rolRepository = rolRepository;
        this.departmanRepository = departmanRepository;
        this.passwordEncoder = passwordEncoder; // Enjekte edilen PasswordEncoder atanıyor
        this.bransRepository = bransRepository;

        
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personel> getAllPersoneller() {
        return personelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Personel> getPersonelById(Integer id) {
        return personelRepository.findById(id);
    }

    @Override
    @Transactional
    public Personel createPersonel(PersonelDTO dto) {
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
        yeniKullanici.setSifre(passwordEncoder.encode(dto.getSifre())); // Şifre burada hash'leniyor
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
        yeniPersonel.setKullanici(yeniKullanici);

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
            doktorDetay.setPersonel(yeniPersonel);
            yeniPersonel.setDoktorDetay(doktorDetay);
        }
        
        return personelRepository.save(yeniPersonel);
    }

    @Override
    @Transactional
    public Personel updatePersonelWithDTO(Integer id, PersonelDTO dto) {
        Personel mevcutPersonel = personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek personel bulunamadı, id: " + id));

        Kullanici mevcutKullanici = mevcutPersonel.getKullanici();
        if (mevcutKullanici == null) {
            throw new IllegalStateException("Personelin ilişkili bir kullanıcı hesabı bulunmuyor. Personel ID: " + id);
        }

        if (StringUtils.hasText(dto.getAd())) mevcutPersonel.setAd(dto.getAd());
        if (StringUtils.hasText(dto.getSoyad())) mevcutPersonel.setSoyad(dto.getSoyad());
        mevcutPersonel.setTelefon(dto.getTelefon());

        if (dto.getDepartmanId() != null) {
            Departman yeniDepartman = departmanRepository.findById(dto.getDepartmanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Departman bulunamadı, id: " + dto.getDepartmanId()));
            mevcutPersonel.setDepartman(yeniDepartman);
        } else {
            mevcutPersonel.setDepartman(null);
        }

        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equals(mevcutKullanici.getEmail())) {
            if (kullaniciRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Bu e-posta adresi zaten başka bir kullanıcı tarafından kullanılıyor: " + dto.getEmail());
            }
            mevcutKullanici.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getSifre())) {
            mevcutKullanici.setSifre(passwordEncoder.encode(dto.getSifre())); // Şifre burada hash'leniyor
        }

        if (dto.getRoller() != null && !dto.getRoller().isEmpty()) {
            Set<Rol> yeniRoller = dto.getRoller().stream()
                    .map(rolAdi -> rolRepository.findByAd(rolAdi)
                            .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + rolAdi)))
                    .collect(Collectors.toSet());
            mevcutKullanici.setRoller(yeniRoller);
        }
        
        boolean isDoktor = mevcutKullanici.getRoller().stream().anyMatch(rol -> "ROLE_DOKTOR".equals(rol.getAd()));
        
        if (isDoktor && dto.getBransId() != null) {
            Brans brans = bransRepository.findById(dto.getBransId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branş bulunamadı, id: " + dto.getBransId()));
            DoktorDetay doktorDetay = mevcutPersonel.getDoktorDetay();
            if (doktorDetay == null) {
                doktorDetay = new DoktorDetay();
                doktorDetay.setPersonel(mevcutPersonel);
                mevcutPersonel.setDoktorDetay(doktorDetay);
            }
            doktorDetay.setBrans(brans);
        } else if (!isDoktor && mevcutPersonel.getDoktorDetay() != null) {
            mevcutPersonel.setDoktorDetay(null); 
        }
        // (isDoktor && dto.getBransId() == null) durumu için ek mantık eklenebilir.

        kullaniciRepository.save(mevcutKullanici);
        return personelRepository.save(mevcutPersonel);
    }

    @Override
    @Transactional
    public void deletePersonel(Integer id) {
        Personel personel = personelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek personel bulunamadı, id: " + id));
        personelRepository.delete(personel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Personel> getPersonellerByRol(String rolAdi) {
        return personelRepository.findByRolAdi(rolAdi);
    }
}