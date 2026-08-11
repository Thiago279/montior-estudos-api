package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.dto.MateriaRequest;
import com.toma.monitor_estudos.dto.MateriaResponse;
import com.toma.monitor_estudos.repository.MateriaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;

    public MateriaService(MateriaRepository materiaRepository) {
        this.materiaRepository = materiaRepository;
    }

    @Transactional
    public MateriaResponse salvar(MateriaRequest request) {
        Materia materia = new Materia();
        materia.setTitulo(request.titulo());

        if (request.cor() != null && !request.cor().isBlank()) {
            materia.setCor(request.cor());
        }

        Materia materiaSalva = materiaRepository.save(materia);
        return mapearParaResponse(materiaSalva);
    }
    @Transactional
    public MateriaResponse atualizar(Long id, MateriaRequest request){
        Materia materia = buscarPorId(id);
        materia.setTitulo(request.titulo());

        if (request.cor() != null && !request.cor().isBlank()) {
            materia.setCor(request.cor());
        }
        Materia materiaSalva = materiaRepository.save(materia);
        return mapearParaResponse(materiaSalva);
    }

    public List<MateriaResponse> listarTodas() {
        return materiaRepository.findAll()
                .stream()
                .map(this::mapearParaResponse)
                .toList();
    }

    private Materia buscarPorId(Long id) {
        return materiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Matéria não encontrada com ID: " + id));
    }

    @Transactional
    public void deletar(Long id) {
        if (!materiaRepository.existsById(id)) {
            throw new EntityNotFoundException("Matéria não encontrada");
        }
        materiaRepository.deleteById(id);
    }

    private MateriaResponse mapearParaResponse(Materia materia) {
        return new MateriaResponse(
                materia.getId(),
                materia.getTitulo(),
                materia.getCor()
        );
    }
}
