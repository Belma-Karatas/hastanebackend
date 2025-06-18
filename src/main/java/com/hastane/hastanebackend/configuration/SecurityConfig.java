package com.hastane.hastanebackend.configuration;

import com.hastane.hastanebackend.security.UserDetailsServiceImpl;
import com.hastane.hastanebackend.security.jwt.JwtAuthenticationEntryPoint;
import com.hastane.hastanebackend.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List; // List importu eklendi

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Metot seviyesi güvenlik için (örn: @PreAuthorize)
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Frontend uygulamanızın çalıştığı adresi buraya ekleyin
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Tek Origin için List.of daha uygun
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Veya daha spesifik header'lar: "Authorization", "Content-Type"
        // configuration.setAllowCredentials(true); // Eğer frontend'den cookie tabanlı kimlik bilgisi gönderiyorsanız
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // API olduğu için ve JWT kullandığımız için CSRF genellikle devre dışı bırakılır
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Oturum yönetimi STATELESS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Giriş ve kayıt endpoint'leri
                .requestMatchers("/api/hastalar/register").permitAll() // Hasta kayıt endpoint'i

                // Personel endpoint'leri
                .requestMatchers(HttpMethod.POST, "/api/personeller").hasRole("ADMIN") // Yeni personel oluşturma sadece ADMIN
                .requestMatchers(HttpMethod.GET, "/api/personeller").hasAnyRole("ADMIN", "HASTA", "DOKTOR") // Tüm personelleri listeleme (dikkatli kullanılmalı)
                .requestMatchers(HttpMethod.GET, "/api/personeller/doktorlar").hasAnyRole("ADMIN", "HASTA", "DOKTOR") // Doktorları listeleme
                .requestMatchers(HttpMethod.GET, "/api/personeller/{id}").hasRole("ADMIN") // ID ile personel getirme sadece ADMIN
                .requestMatchers(HttpMethod.PUT, "/api/personeller/{id}").hasRole("ADMIN") // Personel güncelleme sadece ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/personeller/{id}").hasRole("ADMIN") // Personel silme sadece ADMIN
                
                // Temel Veri Endpoint'leri (Genellikle listeleme herkes tarafından yapılabilir, CUD işlemleri Admin'e özel)
                .requestMatchers(HttpMethod.GET, "/api/departmanlar/**").permitAll() // Departmanları herkes listeleyebilir
                .requestMatchers(HttpMethod.POST, "/api/departmanlar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/departmanlar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/departmanlar/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/branslar/**").permitAll() // Branşları herkes listeleyebilir
                .requestMatchers(HttpMethod.POST, "/api/branslar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/branslar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/branslar/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/roller/**").permitAll() // Rolleri herkes listeleyebilir (gerekirse kısıtlanabilir)
                .requestMatchers(HttpMethod.POST, "/api/roller").hasRole("ADMIN") // Yeni rol ekleme sadece ADMIN
                
                // Diğer endpoint'ler için @PreAuthorize kullanılacak veya buraya eklenecek
                // Örnek: İlaçları listeleme herkes tarafından, ekleme/güncelleme/silme ADMIN
                .requestMatchers(HttpMethod.GET, "/api/ilaclar/**").permitAll() 
                .requestMatchers(HttpMethod.POST, "/api/ilaclar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/ilaclar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/ilaclar/**").hasRole("ADMIN")
                
                // Kat, Oda, Yatak listeleme (Authenticated kullanıcılar)
                .requestMatchers(HttpMethod.GET, "/api/katlar/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/odalar/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/yataklar/**").authenticated()
                // Kat, Oda, Yatak CUD işlemleri (Admin)
                .requestMatchers(HttpMethod.POST, "/api/katlar", "/api/odalar", "/api/yataklar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/katlar/**", "/api/odalar/**", "/api/yataklar/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/katlar/**", "/api/odalar/**", "/api/yataklar/**").hasRole("ADMIN")

                // Randevu endpoint'leri (Detaylı yetkilendirme Controller'da @PreAuthorize ile yapılıyor)
                // Ancak genel bir kural eklenebilir. Örneğin, randevu oluşturma HASTA, DOKTOR, ADMIN tarafından yapılabilir.
                .requestMatchers(HttpMethod.POST, "/api/randevular").hasAnyRole("HASTA", "DOKTOR", "ADMIN")
                .requestMatchers("/api/randevular/**").authenticated() // Diğer randevu işlemleri için giriş yapmış olmak yeterli (detaylı kontrol @PreAuthorize'da)

                // Diğer tüm istekler için kimlik doğrulaması gereklidir
                .anyRequest().authenticated() 
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}