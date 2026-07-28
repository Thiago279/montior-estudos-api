package com.toma.monitor_estudos.repository;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SessaoEstudoRepositoryTest {

    @Autowired
    SessaoEstudoRepository repository;

    @Autowired
    EntityManager entityManager;

    private static final LocalDate DIA_TESTE = LocalDate.of(2026, 7, 28);

    private LocalDateTime horario(int hora, int minuto) {
        return DIA_TESTE.atTime(hora, minuto);
    }


    @Test
    @DisplayName("Deve retornar lista vazia (sem horários conflitantes) do DB")
    void findConflitosHorarioSuccess() {
        //ARRANGE
        Materia materia = createMateria();
        LocalDateTime inicioSessaoSalva = horario(10, 0);// 10:00
        LocalDateTime fimSessaoSalva = horario(13, 0);// 13:00

        createSession(inicioSessaoSalva,fimSessaoSalva ,materia);

        LocalDateTime inicioNovaSessao = horario(14, 0);// 14:00
        LocalDateTime fimNovaSessao = horario(16, 0);// 16:00

        //ACT
        List<SessaoEstudo> conflitos =  repository.findConflitosHorario(inicioNovaSessao,fimNovaSessao, null);

        //ASSERT
        assertThat(conflitos).isEmpty();
    }

    @Test
    @DisplayName("deve retornar uma lista com 1 horário conflitante")
    void findConflitosHorarioShouldReturnOneConflict(){
        Materia materia = createMateria();
        LocalDateTime inicioSessaoSalva = horario(10, 0);// 10:00
        LocalDateTime fimSessaoSalva = horario(13, 0);// 13:00

        SessaoEstudo sessaoSalva = createSession(inicioSessaoSalva,fimSessaoSalva ,materia);

        LocalDateTime inicioNovaSessao = horario(12, 0);// 12:00
        LocalDateTime fimNovaSessao = horario(15, 0);// 15:00

        //ACT
        List<SessaoEstudo> conflitos =  repository.findConflitosHorario(inicioNovaSessao,fimNovaSessao, null);

        //ASSERT
        assertThat(conflitos).isNotEmpty();
        assertThat(conflitos).hasSize(1);
        assertThat(conflitos).contains(sessaoSalva);
    }

    @Test
    @DisplayName("deve retornar uma lista com 2 horários conflitantes")
    void findConflitosHorarioShouldReturnTwoConflict(){
        Materia materia = createMateria();
        LocalDateTime inicioSessaoSalva = horario(10, 0);// 10:00
        LocalDateTime fimSessaoSalva = horario(13, 0);// 13:00

        SessaoEstudo sessaoSalvaUm = createSession(inicioSessaoSalva,fimSessaoSalva ,materia);

        inicioSessaoSalva = horario(13, 30);
        fimSessaoSalva = horario(16, 0);

        SessaoEstudo sessaoSalvaDois = createSession(inicioSessaoSalva , fimSessaoSalva, materia);

        LocalDateTime inicioNovaSessao = horario(12, 0);// 12:00
        LocalDateTime fimNovaSessao = horario(15, 0);// 15:00

        //ACT
        List<SessaoEstudo> conflitos =  repository.findConflitosHorario(inicioNovaSessao,fimNovaSessao, null);

        //ASSERT
        assertThat(conflitos).isNotEmpty();
        assertThat(conflitos).hasSize(2);
        assertThat(conflitos).contains(sessaoSalvaUm);
        assertThat(conflitos).contains(sessaoSalvaDois);
    }

    @Test
    @DisplayName("deve retornar uma lista com 1 horário conflitante, a sessão que está em andamento")
    void findConflitosHorarioOngoingSessionConflict(){
        Materia materia = createMateria();
        LocalDateTime inicioSessaoSalva = horario(10, 0);// 10:00


        SessaoEstudo sessaoSalva = createSession(inicioSessaoSalva,null,materia);

        LocalDateTime inicioNovaSessao = horario(12, 0);// 12:00
        LocalDateTime fimNovaSessao = horario(15, 0);// 15:00

        //ACT
        List<SessaoEstudo> conflitos =  repository.findConflitosHorario(inicioNovaSessao,fimNovaSessao, null);

        //ASSERT
        assertThat(conflitos).isNotEmpty();
        assertThat(conflitos).hasSize(1);
        assertThat(conflitos).contains(sessaoSalva);
    }

    @Test
    @DisplayName("Deve retornar lista vazia ao ignorar o ID da própria sessão na verificação de conflitos")
    void findConflitosHorarioIgnoreOwnIdSuccess(){
        Materia materia = createMateria();
        LocalDateTime inicioSessaoSalva = horario(10 , 0);// 10:00
        LocalDateTime fimSessaoSalva = horario(13 , 0);// 13:00

        SessaoEstudo sessaoSalva = createSession(inicioSessaoSalva,fimSessaoSalva ,materia);

        LocalDateTime inicioNovaSessao = horario(10 , 0);// 10:00
        LocalDateTime fimNovaSessao = horario(14, 0);// 14:00

        //ACT
        List<SessaoEstudo> conflitos =  repository.findConflitosHorario(inicioNovaSessao,fimNovaSessao, sessaoSalva.getId());

        //ASSERT
        assertThat(conflitos).isEmpty();
    }

    private SessaoEstudo createSession(LocalDateTime dataInicio, LocalDateTime dataFim, Materia materia){
        SessaoEstudo newSessao = new SessaoEstudo(
                dataInicio,
                dataFim,
                materia
        );

        this.entityManager.persist(newSessao);
        entityManager.flush();
        return newSessao;
    }

    private Materia createMateria() {
        Materia materia = new Materia("Java Backend", "#FF0000");
        entityManager.persist(materia);
        entityManager.flush();
        return materia;
    }

}
