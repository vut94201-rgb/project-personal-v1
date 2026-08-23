package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionJpaId implements Serializable {

            @Column(name = "role_id", nullable = false)
    private UUID roleId;

            @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

            @Override
    public boolean equals(Object object) {
                if (this == object) {
                        return true;
                    }
              if (!(object instanceof RolePermissionJpaId that)) {
                        return false;
                    }
                return Objects.equals(roleId, that.roleId)
                                && Objects.equals(permissionId, that.permissionId);
            }

            @Override
    public int hashCode() {
              return Objects.hash(roleId, permissionId);
            }
}