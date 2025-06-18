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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yatis_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    // --- DEĞİŞİKLİK BURADA ---
    @ManyToOne(fetch = FetchType.LAZY) // Yatak null olabilir başlangıçta
    @JoinColumn(name = "yatak_id", nullable = true, unique = false) // unique = true kaldırıldı, nullable = true oldu
    private Yatak yatak;
    // --- DEĞİŞİKLİK SONU ---

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

    // --- YENİ ALAN ---
    @Column(name = "durum", nullable = false, length = 50)
    private String durum = "YATAK BEKLIYOR"; // Varsayılan durum
    // --- YENİ ALAN SONU ---

    @OneToMany(mappedBy = "yatis", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<YatisHemsireAtama> hemsireAtamalari = new HashSet<>();

    public void addHemsireAtama(YatisHemsireAtama atama) {
        hemsireAtamalari.add(atama);
        atama.setYatis(this);
    }

    public void removeHemsireAtama(YatisHemsireAtama atama) {
        hemsireAtamalari.remove(atama);
        atama.setYatis(null);
    }
}