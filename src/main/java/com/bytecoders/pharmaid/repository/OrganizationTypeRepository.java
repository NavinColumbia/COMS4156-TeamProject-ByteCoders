package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.OrganizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationTypeRepository extends JpaRepository<OrganizationType, String> {

}