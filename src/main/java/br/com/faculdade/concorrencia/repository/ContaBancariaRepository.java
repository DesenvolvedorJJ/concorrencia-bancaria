package br.com.faculdade.concorrencia.repository;

import br.com.faculdade.concorrencia.model.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {
}
