package com.hastane.hastanebackend.service;

import com.hastane.hastanebackend.dto.HemsireAtaDTO;
import com.hastane.hastanebackend.dto.YatisGoruntuleDTO;
import com.hastane.hastanebackend.dto.YatisOlusturDTO;

// import java.time.LocalDateTime; // Şu an direkt kullanılmıyor, kaldırılabilir
import java.util.List;
import java.util.Optional;

public interface YatisService {

    /**
     * Yeni bir hasta yatış kaydı oluşturur veya doktor tarafından yatış kararı alır.
     * Eğer yatisOlusturDTO içinde yatakId varsa, direkt yatış yapılır.
     * Eğer yatakId null ise, "YATAK BEKLIYOR" durumunda bir kayıt oluşturulur.
     *
     * @param yatisOlusturDTO Yatış bilgilerini içeren DTO. yatakId null olabilir.
     * @param yapanKullaniciId İşlemi yapan kullanıcının ID'si.
     * @return Oluşturulan/işlenen yatışın detaylarını içeren DTO.
     */
    YatisGoruntuleDTO hastaYatisiYap(YatisOlusturDTO yatisOlusturDTO, Integer yapanKullaniciId);

    /**
     * Bir hastanın yatış kaydını sonlandırır (taburcu eder).
     *
     * @param yatisId         Taburcu edilecek yatışın ID'si.
     * @param yapanKullaniciId İşlemi yapan kullanıcının ID'si.
     * @return Güncellenmiş yatışın detaylarını içeren DTO.
     */
    YatisGoruntuleDTO hastaTaburcuEt(Integer yatisId, Integer yapanKullaniciId);

    /**
     * Verilen ID'ye sahip yatış kaydını DTO olarak bulur.
     *
     * @param yatisId Aranacak yatışın ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Bulunursa YatisGoruntuleDTO nesnesini içeren bir Optional, bulunamazsa boş bir Optional.
     */
    Optional<YatisGoruntuleDTO> getYatisById(Integer yatisId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın tüm yatış kayıtlarını DTO olarak listeler.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen hastaya ait YatisGoruntuleDTO listesi.
     */
    List<YatisGoruntuleDTO> getTumYatislarByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir hastanın aktif (çıkış tarihi null ve durumu "AKTIF" veya "YATAK BEKLIYOR" olan) yatış kaydını DTO olarak bulur.
     *
     * @param hastaId Hasta ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatış varsa YatisGoruntuleDTO içeren Optional, yoksa boş Optional.
     */
    Optional<YatisGoruntuleDTO> getAktifYatisByHastaId(Integer hastaId, Integer talepEdenKullaniciId);

    /**
     * Belirli bir yatakta aktif (çıkış tarihi null olan) yatış varsa onu DTO olarak bulur.
     *
     * @param yatakId Yatak ID'si.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatış varsa YatisGoruntuleDTO içeren Optional, yoksa boş Optional.
     */
    Optional<YatisGoruntuleDTO> getAktifYatisByYatakId(Integer yatakId, Integer talepEdenKullaniciId);

    /**
     * Halen hastanede aktif olarak yatan (durumu "AKTIF" ve çıkış tarihi null olan) tüm hastaların yatışlarını DTO olarak listeler.
     *
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Aktif yatışların YatisGoruntuleDTO listesi.
     */
    List<YatisGoruntuleDTO> getTumAktifYatislar(Integer talepEdenKullaniciId);

    // --- YENİ EKLENEN METOT İMZALARI ---
    /**
     * Belirli bir durumdaki ve henüz çıkış yapmamış yatışları listeler.
     * Örneğin, durumu "YATAK BEKLIYOR" olanları almak için kullanılır.
     *
     * @param durum Aranacak yatış durumu.
     * @param talepEdenKullaniciId Bilgiyi talep eden kullanıcının ID'si.
     * @return Belirtilen durumdaki yatışların YatisGoruntuleDTO listesi.
     */
    List<YatisGoruntuleDTO> getTumYatislarByDurum(String durum, Integer talepEdenKullaniciId);

    /**
     * "YATAK BEKLIYOR" durumundaki bir yatışa yatak atar ve durumunu "AKTIF" yapar.
     * İlgili yatağın durumunu da günceller.
     *
     * @param yatisId          Yatak atanacak yatışın ID'si.
     * @param yatakId          Atanacak yatağın ID'si.
     * @param yapanKullaniciId İşlemi yapan (genellikle Admin) kullanıcının ID'si.
     * @return Güncellenmiş yatışın detaylarını içeren DTO.
     */
    YatisGoruntuleDTO yatakAta(Integer yatisId, Integer yatakId, Integer yapanKullaniciId);
    // --- YENİ EKLENEN METOT İMZALARI SONU ---


    YatisGoruntuleDTO hemsireAta(Integer yatisId, HemsireAtaDTO hemsireAtaDTO, Integer yapanKullaniciId);

    YatisGoruntuleDTO hemsireAtamasiniKaldir(Integer yatisId, Integer yatisHemsireAtamaId, Integer yapanKullaniciId);
}