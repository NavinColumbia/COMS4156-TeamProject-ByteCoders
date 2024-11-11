package com.bytecoders.pharmaid.repository;

import com.bytecoders.pharmaid.repository.model.SharedPermission;
import com.bytecoders.pharmaid.repository.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Shared Permission Repository. */
@Repository
public interface SharedPermissionRepository extends JpaRepository<SharedPermission, String> {


  /**
   * Finds Shared Permission by owner, requester, permissionType and status.
   *
   * @param owner          owner
   * @param requester      requester
   * @param permissionType 0 is view, 1 is edit
   * @param status         0 is pending, 1 accepted, 2 denied, revoked ones are deleted
   * @return Optional permission.
   */
  Optional<SharedPermission> findByOwnerAndRequesterAndPermissionTypeAndStatus(User owner,
      User requester,
      Integer permissionType,
      Integer status);


}
