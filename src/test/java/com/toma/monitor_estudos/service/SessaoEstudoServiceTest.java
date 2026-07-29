package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import com.toma.monitor_estudos.dto.SessaoEstudoRequest;
import com.toma.monitor_estudos.dto.SessaoEstudoResponse;
import com.toma.monitor_estudos.exception.SessaoEmAndamentoException;
import com.toma.monitor_estudos.exception.SessaoInvalidaException;
import com.toma.monitor_estudos.exception.SessaoJaFinalizadaException;
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
    private static final Long ID_MATERIA_PADRAO = 1L;
    private static final String NOME_MATERIA_PADRAO = "Java Backend";
    private static final Long ID_SESSAO_PADRAO = 1L;
    private static final Long ID_OUTRA_SESSAO = 2L;

    private LocalDateTime horario(int hora, int minuto) {
        return DIA_TESTE.atTime(hora, minuto);
    }

    private Materia createMateria(Long id, String nome) {
        Materia materia = new Materia(nome, "#FF0000");
        materia.setId(id);
        return materia;
    }

    private Materia materiaPadrao() {
        return createMateria(ID_MATERIA_PADRAO, NOME_MATERIA_PADRAO);
    }

    private SessaoEstudo createSessao(Long id, LocalDateTime inicio, LocalDateTime fim, Materia materia) {
        SessaoEstudo sessao = new SessaoEstudo(inicio, fim, materia);
        sessao.setId(id);
        return sessao;
    }

    private SessaoEstudo sessaoPadrao(Long id) {
        return createSessao(
                id,
                horario(10, 0),
                horario(11, 0),
                materiaPadrao()
        );
    }

    private SessaoEstudoRequest requestPadrao(LocalDateTime inicio, LocalDateTime fim) {
        return new SessaoEstudoRequest(ID_MATERIA_PADRAO, inicio, fim);
    }

    // ==================== SALVAR ====================

    @Test
    @DisplayName("Deve salvar e retornar a nova sessão de estudos")
    void salvarSessaoSuccess() {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);


        SessaoEstudoRequest request = requestPadrao(inicio, fim);
        Materia materia = materiaPadrao();
        SessaoEstudo sessaoSalva = createSessao(
                ID_SESSAO_PADRAO,
                request.dataInicio(),
                request.dataFim(),
                materia
        );

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(inicio, fim, null))
                        .thenReturn(List.of());

        Mockito.when(materiaRepository.findById(ID_MATERIA_PADRAO))
                .thenReturn(Optional.of(materia));

        Mockito.when(sessaoEstudoRepository.save(any(SessaoEstudo.class)))
                .thenReturn(sessaoSalva);

        // Act
        SessaoEstudoResponse response = sessaoEstudoService.salvar(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(ID_SESSAO_PADRAO, response.id());
        Assertions.assertEquals(inicio, response.dataInicio());
        Assertions.assertEquals(fim, response.dataFim());
        Assertions.assertEquals(ID_MATERIA_PADRAO, response.materiaId());
        Assertions.assertEquals(NOME_MATERIA_PADRAO, response.materiaTitulo());

        Mockito.verify(sessaoEstudoRepository).save(any(SessaoEstudo.class));
    }

    @Test
    @DisplayName("Deve retornar a exceção entity not found, quando nao se encontra a materia da sessão no BD")
    void salvarSessaoThrowsEntityNotFoundException()  {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);
        Long materiaInexistenteId = 99L;

        SessaoEstudoRequest request = new SessaoEstudoRequest(materiaInexistenteId, inicio, fim);
        Mockito.when(sessaoEstudoRepository.findConflitosHorario(inicio, fim, null))
                .thenReturn(List.of());
        Mockito.when(materiaRepository.findById(materiaInexistenteId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.salvar(request));

        Assertions.assertEquals("Matéria não encontrada com o ID: " + materiaInexistenteId, thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve retornar a exceção de Sessao Inválida, quando horário a ser cadastrado entra em conflito com horário existente no BD")
    void salvarSessaoThrowsSessaoInvalidaException() {
        // Arrange
        LocalDateTime inicio = horario(14, 0);
        LocalDateTime fim = horario(15, 0);

        Materia materia = materiaPadrao();

        SessaoEstudo sessaoExistente = createSessao(ID_SESSAO_PADRAO, inicio, fim, materia);

        SessaoEstudoRequest request = requestPadrao(inicio,fim);

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

        Materia materia = materiaPadrao();
        SessaoEstudo sessaoExistente = createSessao(ID_SESSAO_PADRAO, inicio, null, materia);
        SessaoEstudoRequest request = requestPadrao(inicio, null);

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
        //Arrange
        SessaoEstudoRequest request = requestPadrao(null, horario(13, 0));

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
        //Arrange
        SessaoEstudoRequest request = requestPadrao(horario(14, 0), horario(13, 0));

        // Act & Assert
        SessaoInvalidaException thrown = Assertions.assertThrows(
                SessaoInvalidaException.class,
                () -> sessaoEstudoService.salvar(request)
        );

        Assertions.assertEquals("A data de início não pode ser posterior à data de fim.", thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }
    // ==================== LISTAR ====================

    @Test
    @DisplayName("Deve retornar lista com as duas sessões cadastradas")
    void listarTodasSuccessReturnsTwoSessions(){
        SessaoEstudo sessaoUm = sessaoPadrao(ID_SESSAO_PADRAO);
        SessaoEstudo sessaoDois = sessaoPadrao(ID_OUTRA_SESSAO);

        Mockito.when(sessaoEstudoRepository.findAll())
                .thenReturn(List.of(sessaoUm,sessaoDois));


        //Act
        List<SessaoEstudoResponse> result = sessaoEstudoService.listarTodas();
        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(ID_SESSAO_PADRAO, result.get(0).id());
        Assertions.assertEquals(ID_OUTRA_SESSAO, result.get(1).id());

        Mockito.verify(sessaoEstudoRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver nenhuma sessão")
    void listarTodasShouldReturnEmptyListWhenNoSessoesFound() {
        // Arrange
        Mockito.when(sessaoEstudoRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<SessaoEstudoResponse> result = sessaoEstudoService.listarTodas();

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(sessaoEstudoRepository).findAll();
    }

    // ==================== DELETAR ====================

    @Test
    @DisplayName("Deve deletar a sessão com sucesso quando o ID existir")
    void deletarSuccess(){
        //Arrange

        Mockito.when(sessaoEstudoRepository.existsById(ID_SESSAO_PADRAO))
                .thenReturn(true);

        //Act
        sessaoEstudoService.deletar(ID_SESSAO_PADRAO);

        // Assert
        Mockito.verify(sessaoEstudoRepository).existsById(ID_SESSAO_PADRAO);
        Mockito.verify(sessaoEstudoRepository).deleteById(ID_SESSAO_PADRAO);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando tentar deletar um ID inexistente")
    void deletarThrowsEntityNotFoundException() {
        // Arrange
        Long idInexistente = 99L;
        Mockito.when(sessaoEstudoRepository.existsById(idInexistente))
                .thenReturn(false);

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.deletar(idInexistente)
        );

        Assertions.assertEquals("Sessão de estudos não encontrada", thrown.getMessage());

        // Garante que NUNCA tentou deletar no banco se não encontrou o ID
        Mockito.verify(sessaoEstudoRepository, Mockito.never()).deleteById(any());
    }

    // ==================== FINALIZAR ====================

    @Test
    @DisplayName("Deve finalizar uma sessão em andamento com sucesso")
    void finalizarSuccess(){
        //Arrange
        SessaoEstudo sessaoNaoFinalizada = createSessao(
                ID_SESSAO_PADRAO,
                horario(13,0),
                null,
                materiaPadrao()
        );

        Mockito.when(sessaoEstudoRepository.findById(ID_SESSAO_PADRAO))
                .thenReturn(Optional.of(sessaoNaoFinalizada));
        Mockito.when(sessaoEstudoRepository.save(any(SessaoEstudo.class)))
                .thenReturn(sessaoNaoFinalizada);

        //Act
        SessaoEstudoResponse result = sessaoEstudoService.finalizar(ID_SESSAO_PADRAO);
        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.dataFim());
        Assertions.assertEquals(ID_SESSAO_PADRAO, result.id());
        Mockito.verify(sessaoEstudoRepository).findById(ID_SESSAO_PADRAO);
        Mockito.verify(sessaoEstudoRepository).save(any(SessaoEstudo.class));
    }

    @Test
    @DisplayName("Deve lançar SessaoJaFinalizadaException quando a sessão já possuir data de término")
    void finalizarThrowsSessaoJaFinalizadaException(){
        //Arrange
        SessaoEstudo sessaoFinalizada = sessaoPadrao(ID_SESSAO_PADRAO);

        Mockito.when(sessaoEstudoRepository.findById(ID_SESSAO_PADRAO))
                .thenReturn(Optional.of(sessaoFinalizada));

        //Act & Assert
        SessaoJaFinalizadaException thrown = Assertions.assertThrows(
                SessaoJaFinalizadaException.class,
                () -> sessaoEstudoService.finalizar(ID_SESSAO_PADRAO)
        );
        Assertions.assertEquals("Esta sessão de estudos já foi finalizada.", thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException ao tentar finalizar uma sessão inexistente")
    void finalizarThrowsEntityNotFoundException(){
        Long idInexistente = 99L;
        Mockito.when(sessaoEstudoRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        //Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.finalizar(idInexistente)
        );
        Assertions.assertEquals("Sessão não encontrada com o ID: " + idInexistente, thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    // ==================== ATUALIZAR ====================

    @Test
    @DisplayName("Deve atualizar e retornar a sessão de estudos quando os dados forem válidos")
    void atualizarSuccess(){
        //Arrange
        Long novaMateriaId = 2L;
        Materia materia = createMateria(novaMateriaId, "POO");
        SessaoEstudoRequest request = new SessaoEstudoRequest(
                novaMateriaId,
                horario(12, 0),
                horario(15, 0)
        );
        SessaoEstudo sessaoExistente = sessaoPadrao(ID_SESSAO_PADRAO); // inicio: 10:00 ; fim 11:00 , materiaId 1

        Mockito.when(sessaoEstudoRepository.findById(ID_SESSAO_PADRAO))
                .thenReturn(Optional.of(sessaoExistente));
        Mockito.when(sessaoEstudoRepository.findConflitosHorario(request.dataInicio(), request.dataFim(), ID_SESSAO_PADRAO))
                .thenReturn(List.of());
        Mockito.when(materiaRepository.findById(novaMateriaId))
                .thenReturn(Optional.of(materia));
        Mockito.when(sessaoEstudoRepository.save(any(SessaoEstudo.class)))
                .thenReturn(sessaoExistente);

        //Act

        SessaoEstudoResponse result = sessaoEstudoService.atualizar(ID_SESSAO_PADRAO, request);

        //Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(horario(12,0), result.dataInicio());
        Assertions.assertEquals(horario(15,0), result.dataFim());
        Assertions.assertEquals(novaMateriaId, result.materiaId());
        Assertions.assertEquals("POO", result.materiaTitulo());

        Mockito.verify(sessaoEstudoRepository).save(any(SessaoEstudo.class));

    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException no atualizar quando a sessão não for encontrada")
    void atualizarThrowsEntityNotFoundExceptionWhenSessaoNotFound(){
        Long idInexistente = 99L;
        SessaoEstudoRequest request = requestPadrao(horario(10, 0), horario(12, 0));
        Mockito.when(sessaoEstudoRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.atualizar(idInexistente, request)
        );

        Assertions.assertEquals("Sessão não encontrada com o ID: " + idInexistente, thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never())
                .save(any());
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException no atualizar quando a matéria não for encontrada")
    void atualizarThrowsEntityNotFoundExceptionWhenMateriaNotFound() {
        // Arrange
        Long materiaInexistenteId = 99L;
        SessaoEstudoRequest request = new SessaoEstudoRequest(
                materiaInexistenteId,
                horario(12, 0),
                horario(15, 0)
        );
        SessaoEstudo sessaoExistente = sessaoPadrao(ID_SESSAO_PADRAO);

        Mockito.when(sessaoEstudoRepository.findById(ID_SESSAO_PADRAO))
                .thenReturn(Optional.of(sessaoExistente));

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(request.dataInicio(), request.dataFim(), ID_SESSAO_PADRAO))
                .thenReturn(List.of());

        Mockito.when(materiaRepository.findById(materiaInexistenteId))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> sessaoEstudoService.atualizar(ID_SESSAO_PADRAO, request)
        );

        Assertions.assertEquals("Matéria não encontrada com o ID: " + materiaInexistenteId, thrown.getMessage());

        Mockito.verify(sessaoEstudoRepository, Mockito.never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar SessaoInvalidaException no atualizar quando houver conflito de horário")
    void atualizarThrowsSessaoInvalidaExceptionWhenConflito() {
        // Arrange
        SessaoEstudoRequest request = requestPadrao(horario(12, 0), horario(15, 0));
        SessaoEstudo sessaoExistente = sessaoPadrao(ID_SESSAO_PADRAO);
        SessaoEstudo sessaoConflitante = createSessao(ID_OUTRA_SESSAO, horario(11, 0), horario(13, 0), materiaPadrao());

        Mockito.when(sessaoEstudoRepository.findById(ID_SESSAO_PADRAO))
                .thenReturn(Optional.of(sessaoExistente));

        Mockito.when(sessaoEstudoRepository.findConflitosHorario(request.dataInicio(), request.dataFim(), ID_SESSAO_PADRAO))
                .thenReturn(List.of(sessaoConflitante));

        // Act & Assert
        SessaoInvalidaException thrown = Assertions.assertThrows(
                SessaoInvalidaException.class,
                () -> sessaoEstudoService.atualizar(ID_SESSAO_PADRAO, request)
        );

        Assertions.assertEquals(
                "Já existe uma sessão de Java Backend cadastrada que conflita com este intervalo de tempo.",
                thrown.getMessage()
        );

        Mockito.verify(materiaRepository, Mockito.never()).findById(any());
        Mockito.verify(sessaoEstudoRepository, Mockito.never()).save(any());
    }

}
