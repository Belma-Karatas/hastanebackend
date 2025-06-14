package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Recete oluşturma tarihi için

import java.time.LocalDate; // SQL'deki DATE tipi için
import java.time.LocalDateTime; // Oluşturulma zamanı için
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Recete") // SQL'deki tablo adıyla eşleşmeli
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recete_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "muayene_id", nullable = false)
    private Muayene muayene; // Her reçete bir muayeneye bağlıdır

    @Column(name = "recete_tarihi", nullable = false)
    private LocalDate receteTarihi; // SQL'deki DATE tipine karşılık gelir

    @CreationTimestamp // Kayıt oluşturulduğunda otomatik olarak zaman damgası ekler
    @Column(name = "olusturulma_zamani", updatable = false) // Bu alan sonradan güncellenemez
    private LocalDateTime olusturulmaZamani;

    // ReceteIlac ile olan OneToMany ilişkisi
    // Bir reçetede birden fazla ilaç (ReceteIlac kaydı üzerinden) bulunabilir.
    // Reçete silindiğinde ilişkili ReceteIlac kayıtları da silinmelidir (cascade = CascadeType.ALL).
    // orphanRemoval = true: Reçeteden bir ReceteIlac kaydı çıkarıldığında (Set'ten remove edildiğinde)
    // veritabanından da silinmesini sağlar.
    @OneToMany(mappedBy = "recete", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ReceteIlac> receteIlaclari = new HashSet<>();

    // Yardımcı metotlar (ReceteIlac ekleme/çıkarma için - iyi bir pratiktir)
    public void addReceteIlac(ReceteIlac receteIlac) {
        receteIlaclari.add(receteIlac);
        receteIlac.setRecete(this);
    }

    public void removeReceteIlac(ReceteIlac receteIlac) {
        receteIlaclari.remove(receteIlac);
        receteIlac.setRecete(null);
    }

    // equals ve hashCode metotları Lombok tarafından ID üzerinden otomatik yönetilebilir
    // veya manuel eklenebilir.
}