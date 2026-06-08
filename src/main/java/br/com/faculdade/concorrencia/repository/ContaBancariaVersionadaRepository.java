package br.com.faculdade.concorrencia.repository;

import br.com.faculdade.concorrencia.model.ContaBancariaVersionada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContaBancariaVersionadaRepository extends JpaRepository<ContaBancariaVersionada, Long> {
}
