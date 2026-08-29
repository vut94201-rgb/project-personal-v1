package com.hanyang.identity.identityservicev4mono.service_identity.api.mapper;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.CreateServicePrincipalRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.UpdateServicePrincipalRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response.ServicePrincipalOwnerResponse;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response.ServicePrincipalProvisioningResponse;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response.ServicePrincipalResponse;
import com.hanyang.identity.identityservicev4mono.service_identity.application.ServicePrincipalView;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.CreateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.UpdateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwner;
import org.springframework.stereotype.Component;

@Component
public class ServicePrincipalRestMapper {

    public CreateServicePrincipalCommand toCommand(CreateServicePrincipalRequest request) {
        return new CreateServicePrincipalCommand(
                request.code(),
                request.displayName(),
                request.purpose(),
                request.description(),
                new EmployeeId(request.primaryOwnerEmployeeId())
        );
    }

    public UpdateServicePrincipalCommand toCommand(
            ServicePrincipalId id,
            UpdateServicePrincipalRequest request
    ) {
        return new UpdateServicePrincipalCommand(
                id,
                request.displayName(),
                request.purpose(),
                request.description()
        );
    }

    public ServicePrincipalResponse toResponse(ServicePrincipalView view) {
        ServicePrincipal principal = view.servicePrincipal();

        return new ServicePrincipalResponse(
                principal.getId().value(),
                principal.getCode(),
                principal.getDisplayName(),
                principal.getPurpose(),
                principal.getDescription(),
                principal.getStatus(),
                toProvisioningResponse(view.provisioning()),
                view.activeOwners().stream()
                        .map(this::toOwnerResponse)
                        .toList()
        );
    }

    public ServicePrincipalOwnerResponse toOwnerResponse(ServicePrincipalOwner owner) {
        return new ServicePrincipalOwnerResponse(
                owner.getId().value(),
                owner.getEmployeeId().value(),
                owner.getOwnershipType(),
                owner.getStatus(),
                owner.getRevokedAt()
        );
    }

    private ServicePrincipalProvisioningResponse toProvisioningResponse(
            ServicePrincipalProvisioningState state
    ) {
        if (state == null) {
            return null;
        }

        return new ServicePrincipalProvisioningResponse(
                state.getProvider(),
                state.getStatus(),
                state.getExternalCode(),
                state.getDesiredRevision(),
                state.getSyncedRevision(),
                state.getLastSyncedAt(),
                state.getLastError()
        );
    }
}