package com.toma.monitor_estudos.service;

import com.toma.monitor_estudos.domain.Materia;
import com.toma.monitor_estudos.domain.SessaoEstudo;
import com.toma.monitor_estudos.domain.StatusSessao;
import com.toma.monitor_estudos.dto.estatisticas.*;
import com.toma.monitor_estudos.exception.PeriodoInvalidoException;
import com.toma.monitor_estudos.repository.SessaoEstudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstatisticasService {

    private final SessaoEstudoRepository sessaoEstudoRepository;

    public EstatisticasService(SessaoEstudoRepository sessaoEstudoRepository) {
        this.sessaoEstudoRepository = sessaoEstudoRepository;
    }

    @Transactional(readOnly = true)
    public EstatisticaDiariaResponse obterEstatisticaDiaria(LocalDate data) {
        // Define o intervalo exato do dia solicitado: 00:00:00 até 23:59:59
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);

        LocalDateTime agora = LocalDateTime.now();

        // Busca todas as sessões que iniciaram dentro dessa data
        List<SessaoEstudo> sessoes = sessaoEstudoRepository.findByDataInicioBetween(inicioDia, fimDia);

        // Mapeia cada sessão da entidade para o DTO de resumo
        List<SessaoResumoResponse> sessoesResumo = sessoes.stream()
                .map(sessao -> mapearParaResumo(sessao, agora))
                .toList();

        // Calcula a soma total de minutos de todas as sessões do dia
        long tempoTotalMinutos = sessoesResumo.stream()
                .mapToLong(SessaoResumoResponse::duracaoMinutos)
                .sum();

        return new EstatisticaDiariaResponse(data, tempoTotalMinutos, sessoesResumo);
    }

    @Transactional(readOnly = true)
    public EstatisticaSemanalResponse obterEstatisticaSemanal(LocalDate data){
        //  Se a data enviada for null, assume a SEGUNDA-FEIRA da semana atual
        LocalDate inicioSemana = (data != null)
                ? data.with(java.time.DayOfWeek.MONDAY)
                : LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        LocalDate fimSemana = inicioSemana.plusDays(6);

        // Converte para LocalDateTime para buscar no Banco de Dados (00:00:00 até 23:59:59)
        LocalDateTime inicioPeriodo = inicioSemana.atStartOfDay();
        LocalDateTime fimPeriodo = fimSemana.atTime(java.time.LocalTime.MAX);

        LocalDateTime agora = LocalDateTime.now();

        List<SessaoEstudo> sessoes = sessaoEstudoRepository.findByDataInicioBetween(inicioPeriodo, fimPeriodo);

        long tempoTotalSemana = sessoes.stream()
                .mapToLong(sessao -> calcularMinutosSessao(sessao, agora))
                .sum();

        List<LocalDate> diasDaSemana = inicioSemana.datesUntil(fimSemana.plusDays(1)).toList();

        List<DiaSemanaResponse> diasResumo = diasDaSemana.stream()
                .map(dia -> gerarResumoDia(dia, sessoes, agora))
                .toList();


        return new EstatisticaSemanalResponse(
                inicioSemana,
                fimSemana,
                tempoTotalSemana,
                diasResumo
        );
    }

    @Transactional(readOnly = true)
    public EstatisticaPeriodoResponse obterEstatisticaPeriodo(LocalDate dataInicio, LocalDate dataFim){

        LocalDate dataFimEfetiva = (dataFim != null) ? dataFim : LocalDate.now();

        if(dataInicio.isAfter(dataFimEfetiva)){
            throw new PeriodoInvalidoException(
                    "A data final não pode ser anterior à data inicial."
            );
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioPeriodo = dataInicio.atStartOfDay();
        LocalDateTime fimPeriodo =
                (dataFim == null)
                        ? LocalDateTime.now()
                        : dataFim.atTime(LocalTime.MAX);

        List<SessaoEstudo> sessoes = sessaoEstudoRepository.findByDataInicioBetween(inicioPeriodo, fimPeriodo);

        long tempoTotalPeriodo = sessoes.stream()
                .mapToLong(sessao -> calcularMinutosSessao(sessao, agora))
                .sum();

        long quantidadeSessoes = sessoes.size();

        List<MateriaTempoResponse> materiasResumo = agruparMinutosPorMateria(sessoes, agora);

        return new EstatisticaPeriodoResponse(
                dataInicio,
                dataFimEfetiva,
                tempoTotalPeriodo,
                quantidadeSessoes,
                materiasResumo
        );
    }

    private SessaoResumoResponse mapearParaResumo(SessaoEstudo sessao, LocalDateTime agora) {
        boolean emAndamento = sessao.getDataFim() == null;
        StatusSessao status = emAndamento ? StatusSessao.EM_ANDAMENTO : StatusSessao.FINALIZADA;

        LocalDateTime pontoFinalCalculo = emAndamento ? agora : sessao.getDataFim();

        long duracaoMinutos = Duration.between(sessao.getDataInicio(), pontoFinalCalculo).toMinutes();

        LocalTime horaFim = emAndamento ? null : sessao.getDataFim().toLocalTime();

        return new SessaoResumoResponse(
                sessao.getId(),
                sessao.getMateria().getId(),
                sessao.getMateria().getTitulo(),
                sessao.getMateria().getCor(),
                sessao.getDataInicio().toLocalTime(),
                horaFim,
                Math.max(0, duracaoMinutos), // Evita minutos negativos em casos atípicos
                status
        );
    }

    private long calcularMinutosSessao(SessaoEstudo sessao, LocalDateTime momentoSnapshot) {
        LocalDateTime fim = (sessao.getDataFim() != null) ? sessao.getDataFim() : momentoSnapshot;

        long minutos = Duration.between(sessao.getDataInicio(), fim).toMinutes();
        return Math.max(0, minutos); // Garante que não retorne negativo por milissegundos de diferença
    }

    private DiaSemanaResponse gerarResumoDia(LocalDate dia, List<SessaoEstudo> todasSessoes, LocalDateTime agora) {

        List<SessaoEstudo> sessoesDoDia = todasSessoes.stream()
                .filter(sessao -> sessao.getDataInicio().toLocalDate().equals(dia))
                .toList();

        List<MateriaTempoResponse> materiasResumo = agruparMinutosPorMateria(sessoesDoDia, agora);

        long tempoTotalDia = materiasResumo.stream()
                .mapToLong(MateriaTempoResponse::tempoAcumuladoMinutos)
                .sum();

        String nomeDia = dia.getDayOfWeek()
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.of("pt", "BR"))
                .toUpperCase();

        return new DiaSemanaResponse(dia, nomeDia, tempoTotalDia, materiasResumo);
    }

    private List<MateriaTempoResponse> agruparMinutosPorMateria(List<SessaoEstudo> sessoes, LocalDateTime agora) {
        Map<Materia, Long> minutosPorMateria = new HashMap<>();

        for (SessaoEstudo sessao : sessoes) {
            long minutos = calcularMinutosSessao(sessao, agora);
            minutosPorMateria.merge(sessao.getMateria(), minutos, Long::sum);
        }

        return minutosPorMateria.entrySet().stream()
                .map(entry -> new MateriaTempoResponse(
                        entry.getKey().getId(),
                        entry.getKey().getTitulo(),
                        entry.getValue(),
                        entry.getKey().getCor()
                ))
                .toList();
    }
}
