package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import com.toma.monitor_estudos.domain.StatusSessao;
import com.toma.monitor_estudos.dto.estatisticas.diaria.EstatisticaDiariaResponse;
import com.toma.monitor_estudos.dto.estatisticas.periodo.EstatisticaPeriodoResponse;
import com.toma.monitor_estudos.dto.estatisticas.semanal.EstatisticaSemanalResponse;
import com.toma.monitor_estudos.exception.PeriodoInvalidoException;
import com.toma.monitor_estudos.repository.SessaoEstudoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class EstatisticasServiceTest {

    @Mock
    private SessaoEstudoRepository sessaoEstudoRepository;

    @InjectMocks
    private EstatisticasService estatisticasService;

    private static final LocalDate DIA_PADRAO = LocalDate.of(2026, 7, 28);
    private static final Long ID_MATERIA_PADRAO = 1L;
    private static final Long ID_SESSAO_PADRAO = 1L;
    private static final Long ID_OUTRA_SESSAO = 2L;
    private static final String TITULO_MATERIA_PADRAO = "Java Backend";
    private static final String COR_MATERIA_PADRAO = "#FF0000";
    private static final Long ID_OUTRA_MATERIA = 2L;
    private static final String TITULO_OUTRA_MATERIA = "Estrutura de Dados";
    private static final String COR_OUTRA_MATERIA = "#00FF00";

    private Materia materiaPadrao() {
        Materia materia = new Materia(TITULO_MATERIA_PADRAO, COR_MATERIA_PADRAO);
        materia.setId(ID_MATERIA_PADRAO);
        return materia;
    }

    private LocalDateTime horario(int hora, int minuto) {
        return DIA_PADRAO.atTime(hora, minuto);
    }

    private SessaoEstudo createSessao(Long id, LocalDateTime inicio, LocalDateTime fim, Materia materia) {
        SessaoEstudo sessao = new SessaoEstudo(inicio, fim, materia);
        sessao.setId(id);
        return sessao;
    }

    private Materia createMateria(Long id, String nome) {
        Materia materia = new Materia(nome, COR_OUTRA_MATERIA);
        materia.setId(id);
        return materia;
    }

    // ==================== ESTATÍSTICA DIÁRIA ====================

    @Test
    @DisplayName("Deve calcular estatística diária com sucesso somando os minutos das sessões")
    void obterEstatisticaDiariaSuccess() {
        // Arrange
        long tempoTotalEsperadoMinutos = 300L;
        Materia materia = materiaPadrao();
        SessaoEstudo sessaoEstudo = createSessao(
                ID_SESSAO_PADRAO,
                horario(10, 0),
                horario(12, 0),
                materia
        );
        Materia materiaDois = createMateria(ID_OUTRA_MATERIA, TITULO_OUTRA_MATERIA);
        SessaoEstudo sessaoEstudoDois = createSessao(
                ID_OUTRA_SESSAO,
                horario(15, 0),
                horario(18, 0),
                materiaDois
        );

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoEstudo, sessaoEstudoDois));

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(DIA_PADRAO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(DIA_PADRAO, response.data());
        Assertions.assertEquals(2, response.sessoes().size());
        Assertions.assertEquals(tempoTotalEsperadoMinutos, response.tempoTotalMinutos());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                DIA_PADRAO.atStartOfDay(),
                DIA_PADRAO.atTime(LocalTime.MAX)
        );
    }

    @Test
    @DisplayName("Deve mapear corretamente sessões em andamento na estatística diária")
    void obterEstatisticaDiariaSuccessEmAndamento() {
        // Arrange
        Materia materia = materiaPadrao();
        SessaoEstudo sessaoEstudo = createSessao(
                ID_SESSAO_PADRAO,
                horario(10, 0),
                horario(12, 0),
                materia
        );
        Materia materiaDois = createMateria(ID_OUTRA_MATERIA, TITULO_OUTRA_MATERIA);
        SessaoEstudo sessaoEstudoDois = createSessao(
                ID_OUTRA_SESSAO,
                horario(15, 0),
                null,
                materiaDois
        );

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoEstudo, sessaoEstudoDois));

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(DIA_PADRAO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(DIA_PADRAO, response.data());
        Assertions.assertEquals(2, response.sessoes().size());
        Assertions.assertEquals(StatusSessao.FINALIZADA, response.sessoes().get(0).status());
        Assertions.assertEquals(StatusSessao.EM_ANDAMENTO, response.sessoes().get(1).status());
        Assertions.assertNull(response.sessoes().get(1).horaFim());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                DIA_PADRAO.atStartOfDay(),
                DIA_PADRAO.atTime(LocalTime.MAX)
        );
    }

    @Test
    @DisplayName("Deve retornar estatística diária zerada quando não houver sessões no dia")
    void obterEstatisticaDiariaShouldReturnZeroWhenNoSessoes() {
        // Arrange
        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of());

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(DIA_PADRAO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(DIA_PADRAO, response.data());
        Assertions.assertEquals(0, response.sessoes().size());
        Assertions.assertEquals(0, response.tempoTotalMinutos());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                DIA_PADRAO.atStartOfDay(),
                DIA_PADRAO.atTime(LocalTime.MAX)
        );
    }

    // ==================== ESTATÍSTICA SEMANAL ====================

    @Test
    @DisplayName("Deve calcular estatística semanal corretamente ao informar uma data")
    void obterEstatisticaSemanalSuccess() {
        // Arrange
        Materia materia = materiaPadrao();
        Materia materiaDois = createMateria(ID_OUTRA_MATERIA, TITULO_OUTRA_MATERIA);
        SessaoEstudo sessaoEstudoTerca = createSessao(
                ID_SESSAO_PADRAO,
                horario(10, 0),
                horario(12, 0),
                materia
        );
        SessaoEstudo sessaoEstudoQuinta = createSessao(
                ID_OUTRA_SESSAO,
                DIA_PADRAO.plusDays(2).atTime(15, 0),
                DIA_PADRAO.plusDays(2).atTime(19, 0),
                materiaDois
        );

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoEstudoTerca, sessaoEstudoQuinta));

        // Act
        EstatisticaSemanalResponse response = estatisticasService.obterEstatisticaSemanal(DIA_PADRAO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(360L, response.tempoTotalMinutos());
        Assertions.assertEquals(LocalDate.of(2026, 7, 27), response.dataInicio()); // Segunda-feira
        Assertions.assertEquals(LocalDate.of(2026, 8, 2), response.dataFim());     // Domingo
        Assertions.assertEquals(7, response.dias().size());

        var diaSegunda = response.dias().get(0);
        Assertions.assertEquals(DIA_PADRAO.minusDays(1), diaSegunda.data());
        Assertions.assertEquals(0L, diaSegunda.tempoTotalMinutos());
        Assertions.assertTrue(diaSegunda.materias().isEmpty());

        var diaTerca = response.dias().get(1);
        Assertions.assertEquals(DIA_PADRAO, diaTerca.data());
        Assertions.assertEquals(120L, diaTerca.tempoTotalMinutos());
        Assertions.assertEquals(1, diaTerca.materias().size());
        Assertions.assertEquals(TITULO_MATERIA_PADRAO, diaTerca.materias().get(0).materiaTitulo());

        var diaQuinta = response.dias().get(3);
        Assertions.assertEquals(DIA_PADRAO.plusDays(2), diaQuinta.data());
        Assertions.assertEquals(240L, diaQuinta.tempoTotalMinutos());
        Assertions.assertEquals(TITULO_OUTRA_MATERIA, diaQuinta.materias().get(0).materiaTitulo());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                LocalDate.of(2026, 7, 27).atStartOfDay(),
                LocalDate.of(2026, 8, 2).atTime(LocalTime.MAX)
        );
    }

    @Test
    @DisplayName("Deve assumir a semana atual quando a data for nula")
    void obterEstatisticaSemanalWhenDataIsNull() {
        // Arrange
        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of());

        LocalDate segundaAtualEsperada = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate domingoAtualEsperado = segundaAtualEsperada.plusDays(6);

        // Act
        EstatisticaSemanalResponse response = estatisticasService.obterEstatisticaSemanal(null);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(segundaAtualEsperada, response.dataInicio());
        Assertions.assertEquals(domingoAtualEsperado, response.dataFim());
        Assertions.assertEquals(7, response.dias().size());
        Assertions.assertEquals(0L, response.tempoTotalMinutos());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                segundaAtualEsperada.atStartOfDay(),
                domingoAtualEsperado.atTime(LocalTime.MAX)
        );
    }

    // ==================== ESTATÍSTICA DE PERÍODO ====================

    @Test
    @DisplayName("Deve lançar PeriodoInvalidoException quando dataInicio for posterior a dataFim")
    void obterEstatisticaPeriodoShouldThrowPeriodoInvalidoExceptionWhenDataInicioAfterDataFim() {
        // Arrange
        LocalDate dataInicio = DIA_PADRAO;
        LocalDate dataFim = DIA_PADRAO.minusDays(1);

        // Act & Assert
        PeriodoInvalidoException thrown = Assertions.assertThrows(
                PeriodoInvalidoException.class,
                () -> estatisticasService.obterEstatisticaPeriodo(dataInicio, dataFim)
        );

        Assertions.assertEquals("A data final não pode ser anterior à data inicial.", thrown.getMessage());
    }

    @Test
    @DisplayName("Deve calcular estatística do período agrupando tempo por matéria")
    void obterEstatisticaPeriodoSuccess() {
        // Arrange
        LocalDate dataInicio = DIA_PADRAO;
        LocalDate dataFim = DIA_PADRAO.plusDays(15);

        Materia materiaUm = materiaPadrao();
        Materia materiaDois = createMateria(ID_OUTRA_MATERIA, TITULO_OUTRA_MATERIA);

        SessaoEstudo sessaoEstudo = createSessao(
                ID_SESSAO_PADRAO,
                horario(9, 0),
                horario(12, 0),
                materiaUm
        ); // 180 min - Java Backend
        SessaoEstudo sessaoEstudoDois = createSessao(
                ID_OUTRA_SESSAO,
                DIA_PADRAO.plusDays(2).atTime(15, 0),
                DIA_PADRAO.plusDays(2).atTime(19, 0),
                materiaDois
        ); // 240 min - Estrutura de Dados
        SessaoEstudo sessaoEstudoTres = createSessao(
                3L,
                DIA_PADRAO.plusDays(9).atTime(13, 0),
                DIA_PADRAO.plusDays(9).atTime(20, 0),
                materiaUm
        ); // 420 min - Java Backend

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoEstudo, sessaoEstudoDois, sessaoEstudoTres));

        // Act
        EstatisticaPeriodoResponse response = estatisticasService.obterEstatisticaPeriodo(dataInicio, dataFim);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(dataInicio, response.dataInicio());
        Assertions.assertEquals(dataFim, response.dataFim());
        Assertions.assertEquals(3, response.quantidadeSessoes());
        Assertions.assertEquals(840L, response.tempoTotalMinutos());
        Assertions.assertEquals(2, response.materias().size());

        // Asserções cirúrgicas de agrupamento por matéria
        long tempoJavaBackend = response.materias().stream()
                .filter(m -> m.materiaId().equals(ID_MATERIA_PADRAO))
                .findFirst()
                .orElseThrow()
                .tempoAcumuladoMinutos();
        Assertions.assertEquals(600L, tempoJavaBackend); // 180 + 420

        long tempoEstruturaDados = response.materias().stream()
                .filter(m -> m.materiaId().equals(ID_OUTRA_MATERIA))
                .findFirst()
                .orElseThrow()
                .tempoAcumuladoMinutos();
        Assertions.assertEquals(240L, tempoEstruturaDados); 

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                dataInicio.atStartOfDay(),
                dataFim.atTime(LocalTime.MAX)
        );
    }

    @Test
    @DisplayName("Deve assumir a data/hora atual quando dataFim for nula")
    void obterEstatisticaPeriodoWhenDataFimIsNull() {
        // Arrange
        LocalDate dataInicio = DIA_PADRAO; // 2026-07-28
        LocalDate dataFimEsperada = LocalDate.now();

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        EstatisticaPeriodoResponse response = estatisticasService.obterEstatisticaPeriodo(dataInicio, null);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(dataInicio, response.dataInicio());
        Assertions.assertEquals(dataFimEsperada, response.dataFim());
        Assertions.assertEquals(0, response.quantidadeSessoes());
        Assertions.assertEquals(0L, response.tempoTotalMinutos());

        Mockito.verify(sessaoEstudoRepository).findByDataInicioBetween(
                Mockito.eq(dataInicio.atStartOfDay()),
                any(LocalDateTime.class)
        );
    }
}
