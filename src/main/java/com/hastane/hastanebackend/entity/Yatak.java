package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Yatak", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"oda_id", "yatak_numarasi"}) // Bir odada aynı yatak numarası birden fazla olamaz
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Yatak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yatak_id")
    private Integer id;

    @Column(name = "yatak_numarasi", nullable = false, length = 20)
    private String yatakNumarasi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "oda_id", nullable = false)
    private Oda oda;

    @Column(name = "dolu_mu", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean doluMu = false;

    // Bir yatakta o an aktif olan tek bir yatış olabilir (Yatis tablosundaki yatak_id unique olduğu için).
    // Bu ilişki Yatis entity'sindeki 'yatak' alanı tarafından yönetilir (mappedBy).
    // FetchType.LAZY: Yatak çekildiğinde ilişkili yatış bilgisi hemen yüklenmez, sadece ihtiyaç duyulduğunda.
    // Cascade ayarı genellikle Yatak tarafından yönetilmez. Yatışın yaşam döngüsü YatisService tarafından yönetilir.
    // Örneğin, bir yatak silinmeden önce üzerinde aktif bir yatış olup olmadığı kontrol edilmelidir.
    @OneToOne(mappedBy = "yatak", fetch = FetchType.LAZY)
    private Yatis aktifYatis; // Yatakta o an aktif olan yatışı tutar
}