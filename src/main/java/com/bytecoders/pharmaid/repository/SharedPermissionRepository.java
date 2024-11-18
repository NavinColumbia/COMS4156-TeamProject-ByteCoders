package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.openapi.model.SharePermissionType;
import com.bytecoders.pharmaid.openapi.model.ShareRequestStatus;
import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Shared Permission Repository. */
@Repository
public interface SharedPermissionRepository extends JpaRepository<SharedPermission, String> {


  /**
   * Finds Shared Permission by owner, requester, permissionType and status.
   *
   * @param owner          owner {@link User} of the health records
   * @param requester      the {@link User} requesting access to the health records
   * @param permissionType the {@link SharePermissionType}
   * @param status         the {@link ShareRequestStatus}
   * @return Optional permission.
   */
  Optional<SharedPermission> findByOwnerAndRequesterAndSharePermissionTypeAndStatus(
      User owner, User requester, SharePermissionType permissionType, ShareRequestStatus status);

  /**
   * Finds Shared Permission by owner, requester, permissionType and status.
   *
   * @param owner           owner {@link User} of the health records
   * @param requester       the {@link User} requesting access to the health records
   * @param permissionTypes a list of {@link SharePermissionType} to check for
   * @param statuses        a list of {@link ShareRequestStatus} to check for
   * @return Optional permission.
   */
  Optional<SharedPermission> findFirstByOwnerAndRequesterAndSharePermissionTypeInAndStatusIn(
      User owner,
      User requester,
      List<SharePermissionType> permissionTypes,
      List<ShareRequestStatus> statuses);
}
