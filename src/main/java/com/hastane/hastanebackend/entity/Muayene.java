package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
// Reçete entity'si ile ilişki için import (ileride)
// import java.util.List;

@Entity
@Table(name = "Muayene")
@Getter
@Setter
@NoArgsConstructor
public class Muayene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "muayene_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "randevu_id", unique = true, nullable = true)
    private Randevu randevu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doktor_personel_id", nullable = false)
    private Personel doktor;

    @Column(name = "muayene_tarihi_saati", nullable = false)
    private LocalDateTime muayeneTarihiSaati;

    // SİKAYET ALANI KALDIRILDI

    @Column(name = "hikaye", columnDefinition = "TEXT") // Hastanın öyküsü
    private String hikaye;
    
    @Column(name = "tani", columnDefinition = "TEXT") // Doktorun koyduğu tanı
    private String tani;

    @Column(name = "tedavi_notlari", columnDefinition = "TEXT") // Tedavi planı, ek notlar
    private String tedaviNotlari;

    @CreationTimestamp
    @Column(name = "olusturulma_tarihi", updatable = false)
    private LocalDateTime olusturulmaTarihi;

    // Bir muayenenin birden fazla reçetesi olabilir (ileride eklenecek)
    // @OneToMany(mappedBy = "muayene", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Recete> receteler;
}