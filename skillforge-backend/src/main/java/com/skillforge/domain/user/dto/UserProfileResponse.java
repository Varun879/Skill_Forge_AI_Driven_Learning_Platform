package com.skillforge.domain.user.dto;

import com.skillforge.common.enums.Role;

public class UserProfileResponse {

    private Long   id;
    private String email;
    private String username;
    private String fullName;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Role   role;

    public UserProfileResponse(Long id, String email, String username,
                                String firstName, String lastName,
                                String avatarUrl, Role role) {
        this.id        = id;
        this.email     = email;
        this.username  = username;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.fullName  = (lastName != null && !lastName.isBlank())
                         ? firstName + " " + lastName
                         : firstName;
        this.avatarUrl = avatarUrl;
        this.role      = role;
    }

    public Long   getId()        { return id; }
    public String getEmail()     { return email; }
    public String getUsername()  { return username; }
    public String getFullName()  { return fullName; }
    public String getFirstName() { return firstName; }
    public String getLastName()  { return lastName; }
    public String getAvatarUrl() { return avatarUrl; }
    public Role   getRole()      { return role; }
}
