package com.toma.monitor_estudos.controller;

import com.toma.monitor_estudos.dto.estatisticas.diaria.EstatisticaDiariaResponse;
import com.toma.monitor_estudos.dto.estatisticas.periodo.EstatisticaPeriodoResponse;
import com.toma.monitor_estudos.dto.estatisticas.semanal.EstatisticaSemanalResponse;
import com.toma.monitor_estudos.service.EstatisticasService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EstatisticasController.class)
class EstatisticasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstatisticasService estatisticasService;

    // -------------------------------------------------------------------------
    // Constantes e Helpers
    // -------------------------------------------------------------------------

    private static final String BASE_URL = "/monitor-estudos/estatisticas";
    private static final LocalDate DATA_CONSULTA = LocalDate.of(2026, 7, 31);
    private static final LocalDate DATA_FIM = LocalDate.of(2026, 8, 5);
    private static final long TEMPO_TOTAL_PADRAO = 180L;

    private EstatisticaDiariaResponse responseDiariaPadrao(LocalDate data) {
        return new EstatisticaDiariaResponse(data, TEMPO_TOTAL_PADRAO, List.of());
    }

    private EstatisticaSemanalResponse responseSemanalPadrao() {
        return new EstatisticaSemanalResponse(DATA_CONSULTA, DATA_FIM, TEMPO_TOTAL_PADRAO, List.of());
    }

    private EstatisticaPeriodoResponse responsePeriodoPadrao(LocalDate fim, long sessoes) {
        return new EstatisticaPeriodoResponse(DATA_CONSULTA, fim, TEMPO_TOTAL_PADRAO, sessoes, List.of());
    }

    // -------------------------------------------------------------------------
    // GET /monitor-estudos/estatisticas/diaria
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK e o payload com estatísticas diárias quando data for informada")
    void obterDiariaSuccess() throws Exception {
        // Arrange
        EstatisticaDiariaResponse response = responseDiariaPadrao(DATA_CONSULTA);

        Mockito.when(estatisticasService.obterEstatisticaDiaria(DATA_CONSULTA))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/diaria")
                        .param("data", "2026-07-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("2026-07-31"))
                .andExpect(jsonPath("$.tempoTotalMinutos").value(TEMPO_TOTAL_PADRAO))
                .andExpect(jsonPath("$.sessoes").isArray());

        verify(estatisticasService).obterEstatisticaDiaria(DATA_CONSULTA);
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao obter estatísticas diárias sem informar o parâmetro data")
    void obterDiariaSuccessNoDateInfomrmed() throws Exception {
        // Arrange
        LocalDate hoje = LocalDate.now();
        EstatisticaDiariaResponse response = responseDiariaPadrao(hoje);

        Mockito.when(estatisticasService.obterEstatisticaDiaria(hoje))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/diaria")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoTotalMinutos").value(TEMPO_TOTAL_PADRAO));

        verify(estatisticasService).obterEstatisticaDiaria(hoje);
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o formato da data for inválido no relatório diário")
    void obterDiaria_DeveRetornar400_QuandoFormatoDataForInvalido() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/diaria")
                        .param("data", "31-07-2026")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /monitor-estudos/estatisticas/semanal
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK e o payload com estatísticas semanais")
    void obterSemanalSuccess() throws Exception {
        // Arrange
        EstatisticaSemanalResponse response = responseSemanalPadrao();

        Mockito.when(estatisticasService.obterEstatisticaSemanal(DATA_CONSULTA))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/semanal")
                        .param("data", "2026-07-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicio").value("2026-07-31"))
                .andExpect(jsonPath("$.dataFim").value("2026-08-05"))
                .andExpect(jsonPath("$.tempoTotalMinutos").value(TEMPO_TOTAL_PADRAO))
                .andExpect(jsonPath("$.dias").isArray());

        verify(estatisticasService).obterEstatisticaSemanal(DATA_CONSULTA);
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o formato da data for inválido no relatório semanal")
    void obterSemanalInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/semanal")
                        .param("data", "invalido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao obter estatísticas semanais sem informar o parâmetro data (repassando null ao service)")
    void obterSemanalSuccessNoDateInformed() throws Exception {
        // Arrange
        EstatisticaSemanalResponse response = responseSemanalPadrao();

        Mockito.when(estatisticasService.obterEstatisticaSemanal(null))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/semanal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicio").value("2026-07-31"))
                .andExpect(jsonPath("$.tempoTotalMinutos").value(TEMPO_TOTAL_PADRAO));

        verify(estatisticasService).obterEstatisticaSemanal(null);
    }

    // -------------------------------------------------------------------------
    // GET /monitor-estudos/estatisticas/periodo
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 OK e o payload do período quando inicio e fim forem informados")
    void obterPeriodoSuccess() throws Exception {
        // Arrange
        EstatisticaPeriodoResponse response = responsePeriodoPadrao(DATA_FIM, 5L);

        Mockito.when(estatisticasService.obterEstatisticaPeriodo(DATA_CONSULTA, DATA_FIM))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/periodo")
                        .param("inicio", "2026-07-31")
                        .param("fim", "2026-08-05")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicio").value("2026-07-31"))
                .andExpect(jsonPath("$.dataFim").value("2026-08-05"))
                .andExpect(jsonPath("$.tempoTotalMinutos").value(TEMPO_TOTAL_PADRAO))
                .andExpect(jsonPath("$.quantidadeSessoes").value(5))
                .andExpect(jsonPath("$.materias").isArray());

        verify(estatisticasService).obterEstatisticaPeriodo(DATA_CONSULTA, DATA_FIM);
    }

    @Test
    @DisplayName("Deve retornar 200 OK ao obter estatísticas do período informando apenas a data de início")
    void obterPeriodoSuccessOnlyInicioInformed() throws Exception {
        // Arrange
        EstatisticaPeriodoResponse response = responsePeriodoPadrao(null, 2L);

        Mockito.when(estatisticasService.obterEstatisticaPeriodo(eq(DATA_CONSULTA), Mockito.isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/periodo")
                        .param("inicio", "2026-07-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicio").value("2026-07-31"))
                .andExpect(jsonPath("$.quantidadeSessoes").value(2));

        verify(estatisticasService).obterEstatisticaPeriodo(eq(DATA_CONSULTA), Mockito.isNull());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar consultar período sem o parâmetro obrigatório inicio")
    void obterPeriodoInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/periodo")
                        .param("fim", "2026-08-05")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
