package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Ilac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ilac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ilac_id")
    private Integer id;

    @Column(name = "ad", nullable = false, length = 100, unique = true)
    private String ad;

    // YENİ EKLENEN ALANLAR
    @Column(name = "barkod", length = 50) // Opsiyonel, uzunluğunu ihtiyaca göre ayarlayın
    private String barkod;

    @Column(name = "form", length = 50) // Opsiyonel, örn: "Tablet", "Şurup", "Kapsül"
    private String form;

    @Column(name = "etken_madde", columnDefinition = "TEXT") // Opsiyonel, uzun olabilir diye TEXT
    private String etkenMadde;
    // YENİ EKLENEN ALANLAR SONU

    @OneToMany(mappedBy = "ilac", fetch = FetchType.LAZY)
    private Set<ReceteIlac> receteIlaclari = new HashSet<>();

}