package com.yes4all.domain.model;


import com.yes4all.domain.Authority;
import com.yes4all.domain.User;

import java.util.Set;

/**
 * A DTO representing a user, with only the public attributes.
 */
public class UserDTO {

    private String id;


    private String firstName;


    private String lastName;

    private Set<Authority> authorities;

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            '}';
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserDTO(User user) {
        this.id = user.getId();
        // Customize it here if you need, or not, firstName/lastName/etc
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    // prettier-ignore

}
