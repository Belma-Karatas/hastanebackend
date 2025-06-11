package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// Diğer importlarınız ve @JsonIdentityInfo gibi anotasyonlarınız burada olmalı
// (Eğer daha önce eklediyseniz)
// import com.fasterxml.jackson.annotation.JsonIdentityInfo;
// import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "Personel")
@Getter
@Setter
// @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Personel.class) // Eğer eklediyseniz kalsın
public class Personel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personel_id")
    private Integer id;

    @Column(name = "ad", nullable = false, length = 100)
    private String ad;

    @Column(name = "soyad", nullable = false, length = 100)
    private String soyad;

    @Column(name = "telefon", length = 20)
    private String telefon;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true) // LAZY yerine EAGER
    @JoinColumn(name = "kullanici_id", referencedColumnName = "kullanici_id", unique = true)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.EAGER) // LAZY yerine EAGER
    @JoinColumn(name = "departman_id", nullable = true)
    private Departman departman;
}