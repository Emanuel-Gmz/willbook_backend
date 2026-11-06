package edu.willbook_backend.repository;

import edu.willbook_backend.model.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    // Lista todos los anuncios vigentes (no expirados) ordenados del más reciente al más antiguo
    @Query("SELECT a FROM Anuncio a WHERE a.fechaExpiracion IS NULL OR a.fechaExpiracion > :ahora ORDER BY a.fechaCreacion DESC")
    List<Anuncio> findAnunciosVigentes(@Param("ahora") LocalDateTime ahora);
}