package com.toma.monitor_estudos.repository;

import com.toma.monitor_estudos.domain.SessaoEstudo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessaoEstudoRepository extends JpaRepository<SessaoEstudo, Long> {

    List<SessaoEstudo> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("""
    SELECT s FROM SessaoEstudo s
    WHERE s.dataInicio <= :fim
      AND (s.dataFim IS NULL OR s.dataFim >= :inicio)
""")
    List<SessaoEstudo> findSessoesNoIntervalo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    Optional<SessaoEstudo> findByDataFimIsNull();

    @Query("""
    SELECT s FROM SessaoEstudo s
    WHERE (:idIgnorar IS NULL OR s.id <> :idIgnorar)
      AND s.dataInicio < :fim
      AND (s.dataFim IS NULL OR s.dataFim > :inicio)
""")
    List <SessaoEstudo> findConflitosHorario(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("idIgnorar") Long idIgnorar
    );
}
