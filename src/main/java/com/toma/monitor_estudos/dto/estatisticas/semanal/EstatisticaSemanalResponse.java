package com.toma.monitor_estudos.dto.estatisticas.semanal;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record EstatisticaSemanalResponse(
        @Schema(
                description = "Data de início da semana (padrão = YYYY-MM-DD)",
                example = "2026-07-20"
        )
        LocalDate dataInicio,
        @Schema(
                description = "Data de fim da semna (padrão = YYYY-MM-DD)",
                example = "2026-07-26"
        )
        LocalDate dataFim,
        @Schema(description = "Tempo total (em minutos) estudando na semana", example = "180")
        long tempoTotalMinutos,
        @Schema(description = "Lista escpecificando o relatório de estudaos pra cada dia da semana")
        List<DiaSemanaResponse> dias
) {
}
