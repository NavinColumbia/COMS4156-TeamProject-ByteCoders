package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;


@Repository
public interface SharingPermissionRepository extends JpaRepository<SharingPermission, String> {

  Optional<SharingPermission> findByOwnerAndSharedWithUser(User owner, User sharedWithUser);

  boolean existsByOwnerIdAndSharedWithUserId(String ownerId, String sharedWithUserId);


  boolean existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
      User owner,
      User sharedWithUser,
      Collection<PermissionType> permissionTypes,
      SharingPermissionStatus status);


  boolean existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
      User owner,
      User sharedWithUser,
      PermissionType permissionType,
      SharingPermissionStatus status);


}