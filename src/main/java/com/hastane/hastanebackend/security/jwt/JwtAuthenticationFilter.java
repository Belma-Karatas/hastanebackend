package com.hastane.hastanebackend.security.jwt; // Paket adın bu olmalı

import com.hastane.hastanebackend.security.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException; // ÖNEMLİ: Bu importu ekle
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info(">>> JwtAuthenticationFilter tetiklendi. İstek URI: {}", request.getRequestURI());
        String jwt = null;
        try {
            jwt = getJwtFromRequest(request);
            logger.info(">>> Header'dan alınan JWT: {}", jwt);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // validateToken true dönerse token geçerli
                logger.info(">>> JWT geçerli bulundu. Kullanıcı adı çekiliyor...");
                String username = tokenProvider.getUsernameFromJWT(jwt); // Bu email olmalı
                logger.info(">>> JWT'den çekilen kullanıcı adı (email): {}", username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username); // DB'den kullanıcıyı yükle
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication); // Kullanıcıyı security context'e set et
                logger.info(">>> Kullanıcı ({}) için SecurityContext'e authentication set edildi.", username);
            } else {
                if (!StringUtils.hasText(jwt)) {
                    logger.warn(">>> JWT bulunamadı (null veya boş). İstek yetkilendirilmeden devam edecek.");
                } else {
                    // validateToken false döndü, JwtTokenProvider içinde zaten spesifik hata loglanıyor olmalı
                    // (Expired, Malformed, Signature, Unsupported, IllegalArgument)
                    logger.warn(">>> JWT geçerli değil (validateToken false döndü). JWT: {}", jwt);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn(">>> JWT'nin süresi dolmuş: {} - Mesaj: {}", jwt, e.getMessage());
        } catch (SignatureException e) { // io.jsonwebtoken.security.SignatureException
            logger.warn(">>> JWT imzası geçersiz: {} - Mesaj: {}", jwt, e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn(">>> JWT formatı bozuk: {} - Mesaj: {}", jwt, e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn(">>> JWT tipi desteklenmiyor: {} - Mesaj: {}", jwt, e.getMessage());
        } catch (IllegalArgumentException e) { // Bu, tokenProvider.getUsernameFromJWT'den veya validateToken'dan gelebilir
            logger.warn(">>> JWT ile ilgili geçersiz argüman (claims boş olabilir): {} - Mesaj: {}", jwt, e.getMessage());
        } catch (Exception ex) {
            logger.error(">>> JwtAuthenticationFilter içinde beklenmedik hata: Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response); // İsteği bir sonraki filter'a veya controller'a ilet
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}