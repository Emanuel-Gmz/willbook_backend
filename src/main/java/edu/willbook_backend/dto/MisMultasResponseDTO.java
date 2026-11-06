package edu.willbook_backend.dto;

import java.util.List;

public record MisMultasResponseDTO(
        boolean cuentaRestringida,
        String mensajeBanner,
        List<MultaPersonalDTO> multas
) {}