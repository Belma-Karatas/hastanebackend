package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp; // Sevk tarihi için CreationTimestamp kullanılabilir veya manuel set edilebilir.

import java.time.LocalDateTime;

@Entity
@Table(name = "Sevk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sevk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sevk_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hasta_id", nullable = false)
    private Hasta hasta;

    @ManyToOne(fetch = FetchType.LAZY) // Sevki yapan doktor (Personel)
    @JoinColumn(name = "sevk_eden_doktor_id", nullable = false)
    private Personel sevkEdenDoktor;

    @Column(name = "sevk_tarihi", nullable = false)
    // @CreationTimestamp // Eğer sevk kaydı oluşturulduğu an sevk tarihi olacaksa.
    // Genellikle sevk tarihi ileri bir tarih de olabilir veya manuel girilir. Şimdilik manuel.
    private LocalDateTime sevkTarihi;

    @Column(name = "hedef_kurum", nullable = false, length = 150)
    private String hedefKurum; // Örn: "X Üniversitesi Hastanesi", "Y Devlet Hastanesi"

    @Column(name = "hedef_servis", length = 100) // Örn: "Kardiyoloji", "Genel Cerrahi Yoğun Bakım"
    private String hedefServis; // Null olabilir

    @Lob
    @Column(name = "sevk_nedeni", columnDefinition = "TEXT")
    private String sevkNedeni;

    @Column(name = "durum", nullable = false, length = 20)
    private String durum = "PLANLANDI"; // Varsayılan: PLANLANDI, TAMAMLANDI, IPTAL EDILDI
                                      // Enum kullanmak daha iyi olurdu.
}