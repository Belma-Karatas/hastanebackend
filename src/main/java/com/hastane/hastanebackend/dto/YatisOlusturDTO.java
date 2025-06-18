package com.hastane.hastanebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull; // Bu import kalabilir, diğer alanlar için kullanılıyor.
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
// import java.time.LocalDateTime; // Giriş tarihi otomatik set edilecekse bu yoruma alınabilir.

@Getter
@Setter
public class YatisOlusturDTO {

    @NotNull(message = "Hasta ID boş olamaz.")
    @Positive(message = "Hasta ID pozitif bir sayı olmalıdır.")
    private Integer hastaId;

    // @NotNull(message = "Yatak ID boş olamaz.") // <<<--- BU SATIR YORUMA ALINDI veya SİLİNDİ
    @Positive(message = "Yatak ID (eğer belirtilmişse) pozitif bir sayı olmalıdır.")
    private Integer yatakId; // Artık null olabilir

    @NotNull(message = "Sorumlu Doktor ID boş olamaz.")
    @Positive(message = "Sorumlu Doktor ID pozitif bir sayı olmalıdır.")
    private Integer sorumluDoktorId; // Bu Personel ID'sine karşılık gelecek

    @NotBlank(message = "Yatış nedeni boş olamaz.")
    private String yatisNedeni;

    // girisTarihi: Entity'de @CreationTimestamp ile otomatik set edileceği için DTO'da olmasına gerek yok.
    // Eger manuel set edilmek isteniyorsa eklenebilir:
    // private LocalDateTime girisTarihi;
}