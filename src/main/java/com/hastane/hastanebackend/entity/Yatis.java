package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Yatis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Yatis {

    // ... (id, hasta, yatak, sorumluDoktor, girisTarihi, cikisTarihi, yatisNedeni alanları aynı)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yatis_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yatak_id", nullable = false, unique = true)
    private Yatak yatak;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorumlu_doktor_id", nullable = false)
    private Personel sorumluDoktor;

    @Column(name = "giris_tarihi", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime girisTarihi;

    @Column(name = "cikis_tarihi", nullable = true)
    private LocalDateTime cikisTarihi;

    @Lob
    @Column(name = "yatis_nedeni", columnDefinition = "TEXT")
    private String yatisNedeni;

    // YENİ EKLENEN KISIM: Yatis'ten YatisHemsireAtama'ya OneToMany ilişki
    // Bir yatışa birden fazla hemşire atanabilir.
    // Yatış silindiğinde, ilişkili atama kayıtları da silinmelidir (cascade = CascadeType.ALL).
    @OneToMany(mappedBy = "yatis", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<YatisHemsireAtama> hemsireAtamalari = new HashSet<>();

    // Yardımcı metotlar (opsiyonel ama iyi pratiktir)
    public void addHemsireAtama(YatisHemsireAtama atama) {
        hemsireAtamalari.add(atama);
        atama.setYatis(this);
    }

    public void removeHemsireAtama(YatisHemsireAtama atama) {
        hemsireAtamalari.remove(atama);
        atama.setYatis(null);
    }
}