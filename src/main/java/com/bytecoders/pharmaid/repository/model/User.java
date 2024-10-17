package com.bytecoders.pharmaid.repository.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.lang.NonNull;

@Entity
@Table(name = "users")
public class User {
  /*@Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @ManyToOne
  @JoinColumn(name = "user_type_id", nullable = false)
  private UserType userType;

   */

  @Id
  @UuidGenerator
  private String id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;

  @ManyToOne
  @JoinColumn(name = "user_type_id", nullable = false)
  private UserType userType;

  // Getters and setters
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getHashedPassword() { return hashedPassword; }
  public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

  public Organization getOrganization() { return organization; }
  public void setOrganization(Organization organization) { this.organization = organization; }

  public UserType getUserType() { return userType; }
  public void setUserType(UserType userType) { this.userType = userType; }



}