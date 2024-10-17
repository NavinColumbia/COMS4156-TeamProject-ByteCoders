package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {
}
