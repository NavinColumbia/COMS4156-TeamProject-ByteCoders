package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.PermissionType;
import com.bytecoders.pharmaid.repository.model.SharingPermission;
import com.bytecoders.pharmaid.repository.model.SharingPermissionStatus;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for SharingPermission entity operations. */
@Repository
public interface SharingPermissionRepository extends JpaRepository<SharingPermission, String> {

  /**
   * Checks if a permission exists with given parameters.
   *
   * @param owner the owner user
   * @param sharedWithUser the user with whom the data is shared
   * @param permissionTypes collection of permission types to check
   * @param status the status of the permission
   * @return true if permission exists, false otherwise
   */
  boolean existsByOwnerAndSharedWithUserAndPermissionTypeInAndStatus(
      User owner,
      User sharedWithUser,
      Collection<PermissionType> permissionTypes,
      SharingPermissionStatus status);

  /**
   * Checks if a specific permission type exists.
   *
   * @param owner the owner user
   * @param sharedWithUser the user with whom the data is shared
   * @param permissionType specific permission type to check
   * @param status the status of the permission
   * @return true if permission exists, false otherwise
   */
  boolean existsByOwnerAndSharedWithUserAndPermissionTypeAndStatus(
      User owner,
      User sharedWithUser,
      PermissionType permissionType,
      SharingPermissionStatus status);
}
