package edu.willbook_backend.repository;

import edu.willbook_backend.model.PasswordResetToken;
import edu.willbook_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
    Optional<PasswordResetToken> findByUsuario(Usuario usuario);
}