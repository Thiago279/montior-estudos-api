package com.toma.monitor_estudos.controller;

import com.toma.monitor_estudos.dto.MateriaRequest;
import com.toma.monitor_estudos.dto.MateriaResponse;
import com.toma.monitor_estudos.dto.erro.ErroResponse;
import com.toma.monitor_estudos.service.MateriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitor-estudos/materias")
@Tag(name = "Matérias", description = "Endpoints para gerenciar materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma matéria (a partir do id)", description = "Atualiza uma matéria com base nos dados fornecidos no corpo da requisição. (titulo e cor)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Matéria atualizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Matéria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public ResponseEntity<MateriaResponse> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid MateriaRequest request) {
        MateriaResponse response = materiaService.atualizar(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping
    @Operation(summary = "Cria uma nova matéria", description = "Cria uma nova matéria com base nos dados fornecidos no corpo da requisição. (titulo e cor)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Matéria cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
            )
    })
    public ResponseEntity<MateriaResponse> criar(@RequestBody @Valid MateriaRequest request) {
        MateriaResponse response = materiaService.salvar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todas as matérias", description = "Retorna uma lista com todas as matérias cadastradas no sistema.")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de matérias retornada com sucesso")
    public ResponseEntity<List<MateriaResponse>> listar() {
        List<MateriaResponse> materias = materiaService.listarTodas();
        return ResponseEntity.ok(materias);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma matéria", description = "Exclui uma matéria cadastrada pelo seu ID.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Matéria excluída com sucesso"
    ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Matéria não encontrada para exclusão",
                    content = @Content(schema = @Schema(implementation = ErroResponse.class))
    )
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        materiaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
