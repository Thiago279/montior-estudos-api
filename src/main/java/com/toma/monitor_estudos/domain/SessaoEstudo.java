package com.toma.monitor_estudos.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sessao_estudo")
public class SessaoEstudo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime dataInicio;
    @Column(nullable = true)
    private LocalDateTime dataFim;

    @ManyToOne
    @JoinColumn(name = "materia_id", nullable = false)
    private Materia materia;

    public SessaoEstudo(){
    }

    public SessaoEstudo(LocalDateTime dataInicio, LocalDateTime dataFim, Materia materia) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.materia = materia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDateTime dataFim) {
        this.dataFim = dataFim;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }
}
