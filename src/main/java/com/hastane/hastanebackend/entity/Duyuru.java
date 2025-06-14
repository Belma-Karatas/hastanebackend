package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Duyuru")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Duyuru {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "duyuru_id")
    private Integer id;

    @Column(name = "baslik", nullable = false, length = 255)
    private String baslik;

    @Column(name = "icerik", nullable = false, columnDefinition = "TEXT") // @Lob kaldırıldı
    private String icerik;

    @Column(name = "yayin_tarihi", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime yayinTarihi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yayinlayan_personel_id", nullable = true)
    private Personel yayinlayanPersonel;

    // İleride eklenebilecek alanlar:
    // private LocalDate gecerlilikTarihi;
    // private String hedefKitle;
    // private boolean aktifMi = true;
}
