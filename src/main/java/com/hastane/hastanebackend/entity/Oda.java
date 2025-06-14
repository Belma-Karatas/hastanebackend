package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Oda", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kat_id", "oda_numarasi"}) // Bir katta aynı oda numarası birden fazla olamaz
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Oda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oda_id")
    private Integer id;

    @Column(name = "oda_numarasi", nullable = false, length = 20)
    private String odaNumarasi;

    @ManyToOne(fetch = FetchType.EAGER) // Oda bilgisi çekilirken genellikle kat bilgisi de istenir.
    @JoinColumn(name = "kat_id", nullable = false)
    private Kat kat;

    @Column(name = "kapasite", nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer kapasite = 1; // Varsayılan kapasite 1

    // Bir odada birden fazla yatak olabilir.
    // Oda silindiğinde, ona bağlı yatakların da silinmesini istiyoruz (cascade = CascadeType.ALL).
    @OneToMany(mappedBy = "oda", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Yatak> yataklar = new HashSet<>();

    // Yardımcı metotlar (Yatak ekleme/çıkarma için)
    public void addYatak(Yatak yatak) {
        yataklar.add(yatak);
        yatak.setOda(this);
    }

    public void removeYatak(Yatak yatak) {
        yataklar.remove(yatak);
        yatak.setOda(null);
    }
}