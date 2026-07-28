package com.toma.monitor_estudos.controller;

import com.toma.monitor_estudos.dto.erro.ErroResponse;
import com.toma.monitor_estudos.dto.estatisticas.diaria.EstatisticaDiariaResponse;
import com.toma.monitor_estudos.dto.estatisticas.periodo.EstatisticaPeriodoResponse;
import com.toma.monitor_estudos.dto.estatisticas.semanal.EstatisticaSemanalResponse;
import com.toma.monitor_estudos.service.EstatisticasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/monitor-estudos/estatisticas")
@Tag(name = "Estatísticas", description = "Endpoints para obter estatísticas de estudo")
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    public EstatisticasController(EstatisticasService estatisticasService) {
        this.estatisticasService = estatisticasService;
    }

    @GetMapping("/diaria")
    @Operation(summary = "Obtém estatísticas diárias", description = "Retorna o tempo total acumulado e o detalhamento das sessões de estudo para a data informada. Se omitida, considera a data atual.")
    @ApiResponses({
            @ApiResponse(
            responseCode = "200",
            description = "Relatório diário gerado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de data inválido. O parâmetro 'data' deve seguir o padrão ISO (YYYY-MM-DD)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            )

    })
    public ResponseEntity<EstatisticaDiariaResponse> obterDiaria(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        // Se o frontend não enviar o parâmetro ?data=YYYY-MM-DD, assume a data de hoje por padrão
        LocalDate dataConsulta = (data != null) ? data : LocalDate.now();

        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(dataConsulta);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/semanal")
    @Operation(summary = "Obtém estatísticas semanais", description = "Retorna a soma de minutos e o agrupamento por matérias de Segunda a Domingo para a semana da data informada. Se omitida, considera a semana atual.")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "Relatório semanal gerado com sucesso"
    ),
    @ApiResponse(
            responseCode = "400",
            description = "Formato de data inválido. O parâmetro 'data' deve seguir o padrão ISO (YYYY-MM-DD)",
            content = @Content(schema = @Schema(implementation = ErroResponse.class))
    )})
    public ResponseEntity<EstatisticaSemanalResponse> obterSemanal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data){
        EstatisticaSemanalResponse response = estatisticasService.obterEstatisticaSemanal(data);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/periodo")
    @Operation(summary = "Obtém estatísticas de período especificado", description = "Retorna a soma de minutos e o agrupamento por matérias para o período entre as datas informadas. Se data final for omitida, considera o dia atual.")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "Relatório do período gerado com sucesso"
    ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Formato de data inválido. O parâmetro 'data' deve seguir o padrão ISO (YYYY-MM-DD)",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            )})
    public ResponseEntity<EstatisticaPeriodoResponse> obterPeriodo(
             @RequestParam(name = "inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio, @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim){
        EstatisticaPeriodoResponse response = estatisticasService.obterEstatisticaPeriodo(dataInicio, dataFim);
        return ResponseEntity.ok(response);
    }

}
