package edu.willbook_backend.repository;

import edu.willbook_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
    Optional<Usuario> findByDni(String dni);
    // Búsqueda por DNI, Nombre o Apellido para el panel del bibliotecario
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.dni) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) " +
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :filtro, '%'))")
    List<Usuario> buscarPorDniNombreOApellido(@Param("filtro") String filtro);
}
