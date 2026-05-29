package com.smf.dto.role;

import jakarta.validation.constraints.NotBlank;

public class RoleRequest {

    @NotBlank
    private String roleName;

    public RoleRequest() {}

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
