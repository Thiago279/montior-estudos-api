package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.dto.MateriaRequest;
import com.toma.monitor_estudos.dto.MateriaResponse;
import com.toma.monitor_estudos.repository.MateriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MateriaServiceTest {

    @Mock
    private MateriaRepository materiaRepository;

    @InjectMocks
    private MateriaService materiaService;

    private static final Long ID_PADRAO = 1L;
    private static final String TITULO_PADRAO = "Java Backend";
    private static final String COR_PADRAO = "#FF0000";

    private static final Long OUTRO_ID = 2L;
    private static final String OUTRO_TITULO = "Estrutura de Dados";
    private static final String OUTRA_COR = "#00FF00";

    private Materia createMateria(Long id, String titulo, String cor) {
        Materia materia = new Materia(titulo, cor);
        materia.setId(id);
        return materia;
    }

    private Materia materiaPadrao() {
        return createMateria(ID_PADRAO, TITULO_PADRAO, COR_PADRAO);
    }

    // ==================== SALVAR ====================

    @Test
    @DisplayName("Deve salvar matéria com sucesso quando a cor for informada")
    void salvarSuccessWithColorParam() {
        // Arrange
        MateriaRequest request = new MateriaRequest(TITULO_PADRAO, COR_PADRAO);
        Materia materiaSalva = materiaPadrao();

        Mockito.when(materiaRepository.save(any(Materia.class)))
                .thenReturn(materiaSalva);

        // Act
        MateriaResponse response = materiaService.salvar(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(ID_PADRAO, response.id());
        Assertions.assertEquals(request.titulo(), response.titulo());
        Assertions.assertEquals(request.cor(), response.cor());

        Mockito.verify(materiaRepository).save(any(Materia.class));
    }

    @Test
    @DisplayName("Deve salvar matéria com sucesso quando a cor for nula")
    void salvarSuccessWhenCorIsNull() {
        // Arrange
        MateriaRequest request = new MateriaRequest(TITULO_PADRAO, null);
        Materia materiaSalva = createMateria(ID_PADRAO, TITULO_PADRAO, null);

        Mockito.when(materiaRepository.save(any(Materia.class)))
                .thenReturn(materiaSalva);

        // Act
        MateriaResponse response = materiaService.salvar(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(ID_PADRAO, response.id());
        Assertions.assertEquals(request.titulo(), response.titulo());
        Assertions.assertNull(response.cor());

        Mockito.verify(materiaRepository).save(any(Materia.class));
    }

    // ==================== LISTAR ====================

    @Test
    @DisplayName("Deve retornar lista de matérias cadastradas com sucesso")
    void listarTodasSuccessReturnsList() {
        // Arrange
        Materia materiaUm = materiaPadrao();
        Materia materiaDois = createMateria(OUTRO_ID, OUTRO_TITULO, OUTRA_COR);

        Mockito.when(materiaRepository.findAll())
                .thenReturn(List.of(materiaUm, materiaDois));

        // Act
        List<MateriaResponse> response = materiaService.listarTodas();

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.size());
        Assertions.assertEquals(ID_PADRAO, response.get(0).id());
        Assertions.assertEquals(TITULO_PADRAO, response.get(0).titulo());
        Assertions.assertEquals(OUTRO_ID, response.get(1).id());
        Assertions.assertEquals(OUTRO_TITULO, response.get(1).titulo());

        Mockito.verify(materiaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver matérias cadastradas")
    void listarTodasShouldReturnEmptyListWhenNoMateriasFound() {
        // Arrange
        Mockito.when(materiaRepository.findAll())
                .thenReturn(List.of());

        // Act
        List<MateriaResponse> response = materiaService.listarTodas();

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isEmpty());

        Mockito.verify(materiaRepository).findAll();
    }

    // ==================== ATUALIZAR POR ID ====================

    @Test
    @DisplayName("Deve retornar a entidade Materia quando o ID for encontrado")
    void AtalizarPorIdSuccess() {
        // Arrange
        Materia materia = materiaPadrao();
        MateriaRequest request = new MateriaRequest(OUTRO_TITULO,OUTRA_COR);
        Materia materiaAtualiada = createMateria(ID_PADRAO, OUTRO_TITULO, OUTRA_COR);


        Mockito.when(materiaRepository.findById(ID_PADRAO))
                .thenReturn(Optional.of(materia));
        Mockito.when(materiaRepository.save(Mockito.any(Materia.class)))
                .thenReturn(materiaAtualiada);

        // Act
        MateriaResponse response = materiaService.atualizar(ID_PADRAO, request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(ID_PADRAO, response.id());
        Assertions.assertEquals(OUTRO_TITULO, response.titulo());
        Assertions.assertEquals(OUTRA_COR, response.cor());

        Mockito.verify(materiaRepository).findById(ID_PADRAO);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando o ID da matéria não for encontrado")
    void atualizarThrowsEntityNotFoundException() {
        // Arrange
        Long idInexistente = 99L;
        MateriaRequest request = new MateriaRequest(TITULO_PADRAO,COR_PADRAO);
        Mockito.when(materiaRepository.findById(idInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> materiaService.atualizar(idInexistente, request)
        );

        Assertions.assertEquals("Matéria não encontrada com ID: " + idInexistente, thrown.getMessage());

        Mockito.verify(materiaRepository).findById(idInexistente);
        Mockito.verifyNoMoreInteractions(materiaRepository);
    }

    // ==================== DELETAR ====================

    @Test
    @DisplayName("Deve deletar a matéria com sucesso quando o ID existir")
    void deletarSuccess() {
        // Arrange
        Mockito.when(materiaRepository.existsById(ID_PADRAO))
                .thenReturn(true);

        // Act
        materiaService.deletar(ID_PADRAO);

        // Assert
        Mockito.verify(materiaRepository).existsById(ID_PADRAO);
        Mockito.verify(materiaRepository).deleteById(ID_PADRAO);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando tentar deletar um ID inexistente")
    void deletarThrowsEntityNotFoundException() {
        // Arrange
        Long idInexistente = 99L;
        Mockito.when(materiaRepository.existsById(idInexistente))
                .thenReturn(false);

        // Act & Assert
        EntityNotFoundException thrown = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> materiaService.deletar(idInexistente)
        );

        Assertions.assertEquals("Matéria não encontrada", thrown.getMessage());

        Mockito.verify(materiaRepository, Mockito.never()).deleteById(any());
    }
}
