package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import com.toma.monitor_estudos.dto.SessaoEstudoRequest;
import com.toma.monitor_estudos.dto.SessaoEstudoResponse;
import com.toma.monitor_estudos.exception.SessaoEmAndamentoException;
import com.toma.monitor_estudos.exception.SessaoInvalidaException;
import com.toma.monitor_estudos.repository.MateriaRepository;
import com.toma.monitor_estudos.repository.SessaoEstudoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class SessaoEstudoServiceTest {

    @Mock
    private SessaoEstudoRepository sessaoEstudoRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private SessaoEstudoService sessaoEstudoService;

    private static final LocalDate DIA_TESTE = LocalDate.of(2026, 7, 28);

    private LocalDateTime horario(int hora, int minuto) {
        return DIA_TESTE.atTime(hora, minuto);
    }

    private Materia createMateria(Long id, String nome) {
        Materia materia = new Materia(nome, "#FF0000");
        materia.setId(id);
        return materia;
    }

    private Materia createMateria() {
        return createMateria(1L, "Java Backend");
    }

    private SessaoEstudo createSessao(Long id, LocalDateTime inicio, LocalDateTime fim, Materia materia) {
        SessaoEstudo sessao = new SessaoEstudo(inicio, fim, materia);
        sessao.setId(id);
        return sessao;
    }

    @Test
    @DisplayName("Deve salvar e retornar a nova sessão de estudos")
    void salvarSessaoSuccess() {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);
        Long materiaId = 1L;

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, fim);
        Materia materia = createMateria();
        SessaoEstudo sessaoSalvaNoBanco = createSessao(
                1L,
                request.dataInicio(),
                request.dataFim(),
                materia
        );

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(inicio, fim, null))
                        .thenReturn(List.of());

        Mockito.when(materiaRepository.findById(materiaId))
                .thenReturn(Optional.of(materia));

        Mockito.when(sessaoEstudoRepository.save(any(SessaoEstudo.class)))
                .thenReturn(sessaoSalvaNoBanco);

        // Act
        SessaoEstudoResponse response = sessaoEstudoService.salvar(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals(inicio, response.dataInicio());
        Assertions.assertEquals(fim, response.dataFim());
        Assertions.assertEquals(materiaId, response.materiaId());
        Assertions.assertEquals("Java Backend", response.materiaTitulo());

        Mockito.verify(sessaoEstudoRepository).save(any(SessaoEstudo.class));
    }

    @Test
    @DisplayName("Deve retornar a exceção materia not found, quando nao se encontra a materia da sessão no BD")
    void salvarSessaoThrowsEntityNotFoundException()  {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);
        Long materiaId = 99L;

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, fim);

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(inicio, fim, null))
                .thenReturn(List.of());
        Mockito.when(materiaRepository.findById(materiaId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.salvar(request));

        Assertions.assertEquals("Matéria não encontrada com o ID: " + materiaId, thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve retornar a exceção de Sessao Inválida, quando horário a ser cadastrado entra em conflito com horário existente no BD")
    void salvarSessaoThrowsSessaoInvalidaException() {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);
        Long materiaId = 1L;

        Materia materia = createMateria();

        SessaoEstudo sessaoExistente = new SessaoEstudo(
                inicio,
                fim,
                materia
        );

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, fim);

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(inicio, fim, null))
                .thenReturn(List.of(sessaoExistente));


        // Act & Assert
        SessaoInvalidaException thrown = Assertions.assertThrows(
                SessaoInvalidaException.class,
                () -> sessaoEstudoService.salvar(request));

        Assertions.assertEquals(
                "Já existe uma sessão de Java Backend cadastrada que conflita com este intervalo de tempo.",
                thrown.getMessage()
        );

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve retornar a exceção de Sessao em andamento, quando horário a ser cadastrado entra em conflito com uma sessão em andamento")
    void salvarSessaoThrowsSessaoEmAndamentoException(){
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        Long materiaId = 1L;

        Materia materia = createMateria();
        SessaoEstudo sessaoExistente = new SessaoEstudo(
                inicio,
                null,
                materia
        );

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, null);

        Mockito.when(sessaoEstudoRepository.findByDataFimIsNull())
                .thenReturn(Optional.of(sessaoExistente));

        // Act & Assert
        SessaoEmAndamentoException thrown = Assertions.assertThrows(
                SessaoEmAndamentoException.class,
                () -> sessaoEstudoService.salvar(request));

        Assertions.assertEquals(
                "Já existe uma sessão de estudos em andamento. Finalize-a antes de iniciar outra.",
                thrown.getMessage()
        );

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve retornar exceção de sessão inválida quando a data de início for nula")
    void salvarThrowsSessaoInvalidaExceptionWhenDataInicioIsNull(){
        LocalDateTime inicio = null;
        LocalDateTime fim = horario(13, 0);
        Long materiaId = 1L;

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, fim);

        // Act & Assert
        SessaoInvalidaException thrown = Assertions.assertThrows(
                SessaoInvalidaException.class,
                () -> sessaoEstudoService.salvar(request)
        );

        Assertions.assertEquals("A data de início é obrigatória.", thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve retornar exceção de sessão inválida quando a data de início é posterior a data de fim")
    void salvarThrowsSessaoInvalidaExceptionWhenDataFimIsGreaterThanDatainicio(){
        LocalDateTime inicio = horario(14,0);
        LocalDateTime fim = horario(13, 0);
        Long materiaId = 1L;

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaId, inicio, fim);

        // Act & Assert
        SessaoInvalidaException thrown = Assertions.assertThrows(
                SessaoInvalidaException.class,
                () -> sessaoEstudoService.salvar(request)
        );

        Assertions.assertEquals("A data de início não pode ser posterior à data de fim.", thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

}
