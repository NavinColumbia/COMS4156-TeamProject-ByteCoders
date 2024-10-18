package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.Organization;
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
  Optional<SharingPermission> findByOwnerAndSharedWithOrganization(User owner, Organization sharedWithOrganization);

    boolean existsByOwnerIdAndSharedWithUserId(String ownerId, String sharedWithUserId);



    Optional<SharingPermission> findByOwnerAndSharedWithOrganizationAndPermissionType(
        User owner, Organization sharedWithOrganization, PermissionType permissionType);
/*
    boolean existsByOwnerAndSharedWithUserAndPermissionType(
        User owner, User sharedWithUser, PermissionType permissionType);

    boolean existsByOwnerAndSharedWithOrganizationAndPermissionType(
        User owner, Organization sharedWithOrganization, PermissionType permissionType);


  boolean existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
      User owner, User sharedWithUser, Collection<PermissionType> permissionTypes, SharingPermissionStatus status);


  boolean existsByOwnerAndSharedWithOrganizationAndPermissionTypeInAndStatus(
      User owner, Organization sharedWithOrganization, Collection<PermissionType> permissionTypes, SharingPermissionStatus status);
*/
boolean existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
    User owner,
    User sharedWithUser,
    Collection<PermissionType> permissionTypes,
    SharingPermissionStatus status);

  boolean existsByOwnerAndSharedWithOrganizationAndPermissionTypeInAndStatus(
      User owner,
      Organization sharedWithOrganization,
      Collection<PermissionType> permissionTypes,
      SharingPermissionStatus status);

  boolean existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
      User owner,
      User sharedWithUser,
      PermissionType permissionType,
      SharingPermissionStatus status);

  boolean existsByOwnerAndSharedWithOrganizationAndPermissionTypeAndStatus(
      User owner,
      Organization sharedWithOrganization,
      PermissionType permissionType,
      SharingPermissionStatus status);

}