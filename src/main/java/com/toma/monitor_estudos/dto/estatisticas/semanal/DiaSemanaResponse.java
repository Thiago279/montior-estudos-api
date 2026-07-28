package com.toma.monitor_estudos.dto.estatisticas.semanal;

import com.toma.monitor_estudos.dto.estatisticas.MateriaTempoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record DiaSemanaResponse(
        @Schema(description = "Data de referência da estatística", example = "2026-07-22")
        LocalDate data,
        @Schema(description = "Dia da semana corrspondente a data", example = "SEGUNDA-FEIRA")
        String diaSemana,
        @Schema(description = "Tempo total estudando na data especificada em minutos", example = "180")
        long tempoTotalMinutos,
        @Schema(description = "Lista detalhada do tempo estudando cada Matéria no dia")
        List<MateriaTempoResponse> materias
) {

}
