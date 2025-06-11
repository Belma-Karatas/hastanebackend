package com.hastane.hastanebackend.entity;

// import com.fasterxml.jackson.annotation.JsonManagedReference; // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
// import com.fasterxml.jackson.annotation.JsonIdentityReference; // İsteğe bağlı, sadece serileştirme için

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Kullanici")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Kullanici.class) // EKLENDİ
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kullanici_id")
    private Integer id;

    // ... (diğer alanlar aynı kalacak)
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String sifre;

    @Column(name = "aktif_mi")
    private boolean aktifMi = true;

    @Column(name = "olusturma_tarihi", updatable = false)
    private LocalDateTime olusturmaTarihi;

    @PrePersist
    protected void onCreate() {
        if (this.olusturmaTarihi == null) {
            this.olusturmaTarihi = LocalDateTime.now();
        }
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Kullanici_Rol",
            joinColumns = @JoinColumn(name = "kullanici_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    // @JsonManagedReference("kullanici-rol-referansi") // BU SATIRI SİLİN VEYA YORUM SATIRI YAPIN
    // @JsonIdentityReference(alwaysAsId = true) // İsteğe bağlı, rollerin sadece ID'leri olarak serileştirilmesi için
    private Set<Rol> roller = new HashSet<>();

    public void addRol(Rol rol) {
        this.roller.add(rol);
        rol.getKullanicilar().add(this);
    }

    public void removeRol(Rol rol) {
        this.roller.remove(rol);
        rol.getKullanicilar().remove(this);
    }
}