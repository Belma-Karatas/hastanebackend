package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.HashSet; // Bu importu ekle veya yorum satırını kaldır
import java.util.Set;    // Bu importu ekle veya yorum satırını kaldır

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

    // Aciklama alanı zaten kaldırılmıştı, doğru.

    // ReceteIlac ile olan OneToMany ilişkisi
    // mappedBy = "ilac": ReceteIlac entity'sindeki 'ilac' alanı bu ilişkinin sahibi.
    // fetch = FetchType.LAZY: İlişkili ReceteIlac nesneleri sadece ihtiyaç duyulduğunda yüklenir.
    // cascade: Ilac silindiğinde ilişkili ReceteIlac kayıtlarının otomatik silinmesini istemiyoruz.
    // Bu, veri bütünlüğü için önemlidir. Silme işlemi servis katmanında kontrol edilmelidir.
    @OneToMany(mappedBy = "ilac", fetch = FetchType.LAZY)
    private Set<ReceteIlac> receteIlaclari = new HashSet<>();

    // equals ve hashCode metotları Lombok tarafından ID üzerinden otomatik yönetilebilir
    // veya manuel eklenebilir. Şimdilik Lombok'un varsayılan davranışına bırakıyoruz.
}