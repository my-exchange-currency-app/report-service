package com.demo.skyros.repo;

import com.demo.skyros.entity.ClientRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ClientRequestRepo extends JpaRepository<ClientRequestEntity, Long> {

    List<ClientRequestEntity> findByAuditCreatedDateBetween(Date from, Date to);

    List<ClientRequestEntity> findByAuditCreatedDateBetweenAndTag(Date from, Date to, String tag);

}
