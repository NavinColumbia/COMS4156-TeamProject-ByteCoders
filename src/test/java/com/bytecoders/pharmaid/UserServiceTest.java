package com.bytecoders.pharmaid;

import com.bytecoders.pharmaid.repository.PrescriptionRepository;
import com.bytecoders.pharmaid.repository.UserRepository;
import com.bytecoders.pharmaid.repository.model.User;
import com.bytecoders.pharmaid.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PrescriptionRepository prescriptionRepository;

  @InjectMocks
  private UserService userService;

  @Test
  void updateUser_success() {
    User existingUser = new User();
    existingUser.setId("123");
    existingUser.setEmail("old@example.com");

    User updatedUser = new User();
    updatedUser.setEmail("new@example.com");

    when(userRepository.findById("123")).thenReturn(Optional.of(existingUser));
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    User result = userService.updateUser("123", updatedUser);

    assertEquals("new@example.com", result.getEmail());
    verify(userRepository).save(existingUser);
  }

  @Test
  void updateUser_userNotFound() {
    User updatedUser = new User();
    updatedUser.setEmail("new@example.com");

    when(userRepository.findById("123")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> userService.updateUser("123", updatedUser));
  }

  @Test
  void deleteUser_success() {
    doNothing().when(prescriptionRepository).deleteByUserId("123");
    doNothing().when(userRepository).deleteById("123");

    userService.deleteUser("123");

    verify(prescriptionRepository).deleteByUserId("123");
    verify(userRepository).deleteById("123");
  }

  @Test
  void existsByEmail_userExists() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));

    boolean result = userService.existsByEmail("test@example.com");

    assertTrue(result);
  }

  @Test
  void existsByEmail_userDoesNotExist() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    boolean result = userService.existsByEmail("test@example.com");

    assertFalse(result);
  }
}
