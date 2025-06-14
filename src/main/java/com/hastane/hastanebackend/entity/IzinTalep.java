package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Izin_Talep")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IzinTalep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "izin_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talep_eden_personel_id", nullable = false)
    private Personel talepEdenPersonel;

    @ManyToOne(fetch = FetchType.EAGER) // İzin türü bilgisi genellikle hemen istenir
    @JoinColumn(name = "izin_turu_id", nullable = false)
    private IzinTuru izinTuru;

    @Column(name = "baslangic_tarihi", nullable = false)
    private LocalDate baslangicTarihi;

    @Column(name = "bitis_tarihi", nullable = false)
    private LocalDate bitisTarihi;

    @Column(name = "gun_sayisi", nullable = false)
    private Integer gunSayisi; // Başlangıç ve bitiş tarihinden hesaplanabilir veya direkt girilebilir

    @Lob
    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String aciklama; // Personelin izin için yazdığı açıklama

    @Column(name = "talep_tarihi", nullable = false, updatable = false)
    @CreationTimestamp // Kayıt anında otomatik olarak zaman damgası ekler
    private LocalDateTime talepTarihi;

    @Column(name = "durum", nullable = false, length = 20)
    private String durum = "BEKLIYOR"; // Varsayılan durum: BEKLIYOR, ONAYLANDI, REDDEDILDI

    @ManyToOne(fetch = FetchType.LAZY) // Onaylayan yönetici (Personel)
    @JoinColumn(name = "onaylayan_yonetici_id", nullable = true) // Başlangıçta null olabilir
    private Personel onaylayanYonetici;

    @Column(name = "onay_tarihi", nullable = true)
    private LocalDateTime onayTarihi; // Onaylandığı veya reddedildiği zaman
}