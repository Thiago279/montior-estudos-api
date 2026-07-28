package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import com.toma.monitor_estudos.domain.StatusSessao;
import com.toma.monitor_estudos.dto.estatisticas.diaria.EstatisticaDiariaResponse;
import com.toma.monitor_estudos.dto.estatisticas.semanal.EstatisticaSemanalResponse;
import com.toma.monitor_estudos.repository.SessaoEstudoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class EstatisticasServiceTest {

    @Mock
    private SessaoEstudoRepository sessaoEstudoRepository;

    @InjectMocks
    private EstatisticasService estatisticasService;

    @Test
    void deveCalcularEstatisticaDiariaParaSessaoFinalizada() {
        // Arrange
        LocalDate hoje = LocalDate.of(2026, 7, 20);

        Materia materia = new Materia();
        materia.setId(3L);
        materia.setTitulo("Programação Java");
        materia.setCor("#808080");

        SessaoEstudo sessao = new SessaoEstudo();
        sessao.setId(6L);
        sessao.setMateria(materia);
        sessao.setDataInicio(LocalDateTime.of(2026, 7, 20, 13, 0));
        sessao.setDataFim(LocalDateTime.of(2026, 7, 20, 15, 45)); // 165 minutos (2h45m)

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessao));

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(hoje);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(hoje, response.data());
        Assertions.assertEquals(165L, response.tempoTotalMinutos());
        Assertions.assertEquals(1, response.sessoes().size());

        var resumo = response.sessoes().get(0);
        Assertions.assertEquals(6L, resumo.sessaoId());
        Assertions.assertEquals("Programação Java", resumo.materiaTitulo());
        Assertions.assertEquals(StatusSessao.FINALIZADA, resumo.status());
        Assertions.assertEquals(165L, resumo.duracaoMinutos());
    }

    @Test
    void deveCalcularEstatisticaDiariaParaSessaoEmAndamento() {
        // Arrange
        LocalDate hoje = LocalDate.now();

        Materia materia = new Materia();
        materia.setId(1L);
        materia.setTitulo("Arquitetura de Software");
        materia.setCor("#4A90E2");

        SessaoEstudo sessaoAtiva = new SessaoEstudo();
        sessaoAtiva.setId(7L);
        sessaoAtiva.setMateria(materia);
        // Começou há exatamente 30 minutos atrás
        sessaoAtiva.setDataInicio(LocalDateTime.now().minusMinutes(30));
        sessaoAtiva.setDataFim(null);

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoAtiva));

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(hoje);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.sessoes().size());

        var resumo = response.sessoes().get(0);
        Assertions.assertEquals(StatusSessao.EM_ANDAMENTO, resumo.status());
        Assertions.assertNull(resumo.horaFim());
        Assertions.assertTrue(resumo.duracaoMinutos() >= 30); // Pelo menos 30 minutos decorridos
    }

    @Test
    void deveRetornarEstatisticaVaziaQuandoNaoHouverEstudosNoDia() {
        // Arrange
        LocalDate hoje = LocalDate.of(2026, 7, 20);

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(hoje);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(0L, response.tempoTotalMinutos());
        Assertions.assertTrue(response.sessoes().isEmpty());
    }
    @Test
    void deveSomarTempoTotalDeMultiplasSessoesNoMesmoDia() {
        // Arrange
        LocalDate hoje = LocalDate.of(2026, 7, 20);

        Materia materia = new Materia();
        materia.setId(1L);
        materia.setTitulo("Algoritmos");

        SessaoEstudo sessao1 = new SessaoEstudo();
        sessao1.setId(1L);
        sessao1.setMateria(materia);
        sessao1.setDataInicio(LocalDateTime.of(2026, 7, 20, 9, 0));
        sessao1.setDataFim(LocalDateTime.of(2026, 7, 20, 11, 0)); // 120 minutos

        SessaoEstudo sessao2 = new SessaoEstudo();
        sessao2.setId(2L);
        sessao2.setMateria(materia);
        sessao2.setDataInicio(LocalDateTime.of(2026, 7, 20, 14, 0));
        sessao2.setDataFim(LocalDateTime.of(2026, 7, 20, 15, 30)); // 90 minutos

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessao1, sessao2));

        // Act
        EstatisticaDiariaResponse response = estatisticasService.obterEstatisticaDiaria(hoje);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.sessoes().size());
        Assertions.assertEquals(210L, response.tempoTotalMinutos()); // 120 + 90 = 210 min
    }

    @Test
    void deveCalcularEstatisticaSemanalComSucesso(){
        // Arrange
        LocalDate segunda = LocalDate.of(2026, 7, 20);
        LocalDate quarta = LocalDate.of(2026, 7, 22);

        Materia java = new Materia();
        java.setId(3L);
        java.setTitulo("Programação Java");
        java.setCor("#808080");

        Materia js = new Materia();
        js.setId(4L);
        js.setTitulo("Programação Javascript");
        js.setCor("#4A90E2");

        SessaoEstudo sessaoSegunda = new SessaoEstudo();
        sessaoSegunda.setId(6L);
        sessaoSegunda.setMateria(java);
        sessaoSegunda.setDataInicio(LocalDateTime.of(2026, 7, 20, 13, 0));
        sessaoSegunda.setDataFim(LocalDateTime.of(2026, 7, 20, 14, 0));

        SessaoEstudo sessaoQuarta = new SessaoEstudo();
        sessaoQuarta.setId(7L);
        sessaoQuarta.setMateria(js);
        sessaoQuarta.setDataInicio(LocalDateTime.of(2026, 7, 22, 13, 0));
        sessaoQuarta.setDataFim(LocalDateTime.of(2026, 7, 22, 15, 0));

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(List.of(sessaoSegunda, sessaoQuarta));



        // Act
        EstatisticaSemanalResponse response = estatisticasService.obterEstatisticaSemanal(quarta);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(180L, response.tempoTotalMinutos());

        var diaSegunda = response.dias().get(0);
        Assertions.assertEquals(segunda, diaSegunda.data());
        Assertions.assertEquals(60L, diaSegunda.tempoTotalMinutos());
        Assertions.assertEquals("Programação Java", diaSegunda.materias().get(0).materiaTitulo());

        var diaTerca = response.dias().get(1);
        Assertions.assertEquals(0L, diaTerca.tempoTotalMinutos());
        Assertions.assertTrue(diaTerca.materias().isEmpty());

        var diaQuarta = response.dias().get(2);
        Assertions.assertEquals(quarta, diaQuarta.data());
        Assertions.assertEquals(120L, diaQuarta.tempoTotalMinutos());
        Assertions.assertEquals("Programação Javascript", diaQuarta.materias().get(0).materiaTitulo());
    }

    @Test
    void deveRetornarEstruturaZeradaQuandoNaoHouverEstudosNaSemana() {
        // Arrange
        LocalDate segunda = LocalDate.of(2026, 7, 20);

        Mockito.when(sessaoEstudoRepository.findByDataInicioBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        EstatisticaSemanalResponse response = estatisticasService.obterEstatisticaSemanal(segunda);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(0L, response.tempoTotalMinutos());
        Assertions.assertEquals(7, response.dias().size());

        for (var dia : response.dias()) {
            Assertions.assertEquals(0L, dia.tempoTotalMinutos());
            Assertions.assertTrue(dia.materias().isEmpty());
        }
    }

}
