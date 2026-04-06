package com.expense.tracker.dto;

import java.util.List;

public class UserAdminDTO {
    private String username;
    private boolean enabled;
    private List<String> roles;

    public UserAdminDTO() {}

    public UserAdminDTO(String username, boolean enabled, List<String> roles) {
        this.username = username;
        this.enabled  = enabled;
        this.roles    = roles;
    }

    public String getUsername()          { return username; }
    public void setUsername(String u)    { this.username = u; }

    public boolean isEnabled()           { return enabled; }
    public void setEnabled(boolean e)    { this.enabled = e; }

    public List<String> getRoles()              { return roles; }
    public void setRoles(List<String> roles)    { this.roles = roles; }
}
