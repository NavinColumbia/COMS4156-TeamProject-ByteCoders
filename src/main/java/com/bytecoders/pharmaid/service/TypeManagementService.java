package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.OrganizationTypeRepository;
import com.bytecoders.pharmaid.repository.UserTypeRepository;
import com.bytecoders.pharmaid.repository.model.OrganizationType;
import com.bytecoders.pharmaid.repository.model.UserType;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TypeManagementService {
  @Autowired
  private UserTypeRepository userTypeRepository;

  @Autowired
  private OrganizationTypeRepository organizationTypeRepository;

  public UserType createUserType(String name, String description, boolean canAccessAllRecords) {
    UserType userType = new UserType();
    userType.setName(name);
    userType.setDescription(description);
    userType.setCanAccessAllRecords(canAccessAllRecords);
    return userTypeRepository.save(userType);
  }

  public OrganizationType createOrganizationType(String name, String description) {
    OrganizationType orgType = new OrganizationType();
    orgType.setName(name);
    orgType.setDescription(description);
    return organizationTypeRepository.save(orgType);
  }

  public List<UserType> getAllUserTypes() {
    return userTypeRepository.findAll();
  }

  public List<OrganizationType> getAllOrganizationTypes() {
    return organizationTypeRepository.findAll();
  }
}