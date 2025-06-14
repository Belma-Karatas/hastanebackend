package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Olay zamanı için CreationTimestamp kullanılabilir
                                                  // veya manuel set edilebilir. SQL'de manuel görünüyor.

import java.time.LocalDateTime;

@Entity
@Table(name = "Acil_Durum_Kaydi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcilDurumKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kayit_id")
    private Integer id;

    @Lob
    @Column(name = "aciklama", columnDefinition = "TEXT") // Acil durumun detayı
    private String aciklama;

    @Column(name = "konum", nullable = false, length = 255) // Örn: "Kat 3, Oda 301", "Acil Servis Bekleme Alanı"
    private String konum;

    @Column(name = "olay_zamani", nullable = false)
    private LocalDateTime olayZamani; // SQL'de manuel set edilecek gibi, DTO'dan alınacak.

    // Acil durumu tetikleyen/kaydeden personel (HEMSIRE rolünde olmalı)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tetikleyen_personel_id", nullable = false)
    private Personel tetikleyenPersonel;

    // Acil durum bir hastaya bağlıysa (opsiyonel, her acil durum bir hastaya bağlı olmayabilir)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = true)
    private Hasta hasta;

    @Column(name = "durum", nullable = false, length = 20)
    private String durum = "AKTIF"; // Varsayılan: AKTIF, SONLANDIRILDI, MÜDAHALE EDİLİYOR vb.
                                  // Enum kullanmak daha iyi olurdu.
                                  
    // İleride eklenebilecek alanlar:
    // private String mudahaleNotlari;
    // private LocalDateTime sonlandirilmaZamani;
}