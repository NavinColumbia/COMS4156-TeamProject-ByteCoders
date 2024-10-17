package com.bytecoders.pharmaid.service;

import com.bytecoders.pharmaid.repository.model.UserType;
import com.bytecoders.pharmaid.repository.UserTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserTypeService {

  @Autowired
  private UserTypeRepository userTypeRepository;

  public List<UserType> getAllUserTypes() {
    return userTypeRepository.findAll();
  }

  public Optional<UserType> getUserTypeById(String id) {
    return userTypeRepository.findById(id);
  }

  public Optional<UserType> getUserTypeByName(String name) {
    return userTypeRepository.findByName(name);
  }

  public UserType createUserType(UserType userType) {
    return userTypeRepository.save(userType);
  }

  public UserType updateUserType(String id, UserType userTypeDetails) {
    UserType userType = userTypeRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("UserType not found for this id :: " + id));

    userType.setName(userTypeDetails.getName());
    userType.setDescription(userTypeDetails.getDescription());
    userType.setCanAccessAllRecords(userTypeDetails.isCanAccessAllRecords());

    return userTypeRepository.save(userType);
  }

  public void deleteUserType(String id) {
    UserType userType = userTypeRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("UserType not found for this id :: " + id));

    userTypeRepository.delete(userType);
  }

  public boolean existsByName(String name) {
    return userTypeRepository.existsByName(name);
  }
}