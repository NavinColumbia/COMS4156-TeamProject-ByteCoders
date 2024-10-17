package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Organization;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;


@Repository
public interface SharingPermissionRepository extends JpaRepository<SharingPermission, String> {
  Optional<SharingPermission> findByOwnerAndSharedWithUser(User owner, User sharedWithUser);
  Optional<SharingPermission> findByOwnerAndSharedWithOrganization(User owner, Organization sharedWithOrganization);

    boolean existsByOwnerIdAndSharedWithUserId(String ownerId, String sharedWithUserId);


}