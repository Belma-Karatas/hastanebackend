package com.hastane.hastanebackend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Doktor_Detay") // SQL'deki tablo adınla eşleşmeli
@Getter
@Setter
@NoArgsConstructor // Lombok ile boş constructor
public class DoktorDetay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doktor_detay_id") // SQL'deki sütun adınla eşleşmeli
    private Integer id;

    // Bir DoktorDetay kaydı sadece bir Personel'e ait olabilir ve bu Personel bir doktordur.
    // Personel silindiğinde DoktorDetay'ın da silinmesini istemiyoruz (cascade PERSIST ve MERGE),
    // çünkü Personel entity'sinde orphanRemoval=true ile yönetilecek.
    // Buradaki FetchType.LAZY olması daha iyi olabilir, ihtiyaç anında yüklenir.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personel_id", nullable = false, unique = true) // SQL'deki foreign key sütun adın
    @JsonBackReference("personel-doktordetay") // Personel tarafındaki ilişkiyle eşleşmeli
    private Personel personel;

    // Bir doktorun bir branşı olur. Bir branşta birden fazla doktor olabilir.
    @ManyToOne(fetch = FetchType.EAGER) // Branş bilgisi genellikle hemen istenir
    @JoinColumn(name = "brans_id", nullable = false) // SQL'deki foreign key sütun adın
    private Brans brans;

    // İleride eklenebilecek diğer alanlar:
    // @Column(name = "unvan")
    // private String unvan;
    //
    // @Column(name = "diploma_no", unique = true)
    // private String diplomaNo;

    // Constructor'lar, equals/hashCode, toString (Lombok bunları sağlayabilir veya manuel eklenebilir)
}