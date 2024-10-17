package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.model.UserType;
import com.bytecoders.pharmaid.repository.model.OrganizationType;
import com.bytecoders.pharmaid.service.TypeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/types")
public class TypeManagementController {

  @Autowired
  private TypeManagementService typeManagementService;

  @PostMapping("/user-types")
  public ResponseEntity<UserType> createUserType(@RequestBody UserType userType) {
    UserType createdType = typeManagementService.createUserType(userType.getName(), userType.getDescription(), userType.isCanAccessAllRecords());
    return ResponseEntity.ok(createdType);
  }

  @GetMapping("/user-types")
  public ResponseEntity<List<UserType>> getAllUserTypes() {
    List<UserType> userTypes = typeManagementService.getAllUserTypes();
    return ResponseEntity.ok(userTypes);
  }

  @PostMapping("/organization-types")
  public ResponseEntity<OrganizationType> createOrganizationType(@RequestBody OrganizationType orgType) {
    OrganizationType createdType = typeManagementService.createOrganizationType(orgType.getName(), orgType.getDescription());
    return ResponseEntity.ok(createdType);
  }

  @GetMapping("/organization-types")
  public ResponseEntity<List<OrganizationType>> getAllOrganizationTypes() {
    List<OrganizationType> orgTypes = typeManagementService.getAllOrganizationTypes();
    return ResponseEntity.ok(orgTypes);
  }

  // You can add more endpoints for updating and deleting types as needed
}