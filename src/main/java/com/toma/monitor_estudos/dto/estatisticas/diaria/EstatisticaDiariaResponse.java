package com.toma.monitor_estudos.dto.estatisticas.diaria;

import com.toma.monitor_estudos.dto.estatisticas.SessaoResumoResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record EstatisticaDiariaResponse(
        @Schema(description = "Data de referência da estatística", example = "2026-07-22")
        LocalDate data,
        @Schema(description = "Tempo total estudando na data especificada em minutos", example = "180")
        long tempoTotalMinutos,
        @Schema(description = "Lista detalhada das sessões do dia")
        List<SessaoResumoResponse> sessoes
) {}
