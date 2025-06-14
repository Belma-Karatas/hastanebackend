package com.hastane.hastanebackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Yatis_Hemsire_Atama", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"yatis_id", "hemsire_personel_id"}) // Bir yatışa aynı hemşire birden fazla atanamaz
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YatisHemsireAtama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "yatis_hemsire_atama_id") // Ayrı bir primary key ekleyebiliriz veya kompozit PK kullanabiliriz. Ayrı PK daha kolay.
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yatis_id", nullable = false)
    private Yatis yatis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hemsire_personel_id", nullable = false)
    private Personel hemsire; // Personel entity'si hemşire rolündeki personeli temsil edecek

    @Column(name = "atama_tarihi", nullable = false, updatable = false)
    @CreationTimestamp // Kayıt anında otomatik olarak zaman damgası ekler
    private LocalDateTime atamaTarihi;

    // İleride eklenebilecek alanlar:
    // private String gorevAciklamasi;
    // private LocalDateTime atamaBitisTarihi; // Eğer hemşirenin o yatıştaki görevi biterse

    // SQL'deki PRIMARY KEY (yatis_id, hemsire_personel_id) tanımını
    // @IdClass veya @EmbeddedId ile de yapabilirdik ama ayrı bir auto-increment ID genellikle daha pratiktir.
    // uniqueConstraints ile de aynı mantığı sağlamış olduk.
}