package com.hastane.hastanebackend.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "Hasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Hasta.class)
public class Hasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hasta_id")
    private Integer id;

    // Kullanici'daki ad/soyadı kullanacağımızı varsayarak bu alanları çıkarabiliriz,
    // ancak SQL şemanızda olduğu için ve bazen farklılık gösterebileceği için
    // (örn: resmi ad vs. kullanılan ad) şimdilik tutuyorum.
    // Eğer Kullanici'daki ad/soyad yeterliyse, bunları kaldırabiliriz.
    @Column(name = "ad", nullable = false, length = 100)
    private String ad;

    @Column(name = "soyad", nullable = false, length = 100)
    private String soyad;

    @Column(name = "tc_kimlik_no", unique = true, length = 11)
    private String tcKimlikNo;

    @Column(name = "dogum_tarihi")
    private LocalDate dogumTarihi;

    @Column(name = "cinsiyet", length = 10)
    private String cinsiyet;


    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kullanici_id", referencedColumnName = "kullanici_id", unique = true, nullable = false)
    private Kullanici kullanici;
}