package com.smf.dto.role;

public class RoleResponse {

    private Long id;
    private String roleName;

    public RoleResponse() {}

    public RoleResponse(Long id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }
}
