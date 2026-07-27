package com.toma.monitor_estudos.dto.estatisticas;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record EstatisticaPeriodoResponse(
        LocalDate dataInicio,
        LocalDate dataFim,
        @Schema(description = "Tempo total estudando no período especificado em minutos", example = "180")
        long tempoTotalMinutos,

        long quantidadeSessoes,

        List<MateriaTempoResponse> materias

) {
}
