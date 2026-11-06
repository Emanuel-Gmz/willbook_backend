package edu.willbook_backend.repository;

import edu.willbook_backend.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {
    List<Localidad> findAllByOrderByNombreAsc();
}