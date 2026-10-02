package com.payinvariants;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessorEventRepository extends JpaRepository<ProcessorEvent, String>{
    List<ProcessorEvent> findByCardToken(String cardToken);
}