package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.TargetTenant;
import com.pms.propertymanagement.repository.TargetTenantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TargetTenantInitializer {

    private final TargetTenantsRepository targetTenantsRepository;

    public void init (){
        if (!targetTenantsRepository.findAll().isEmpty()) return;

        List<TargetTenant> targets = List.of(
                new TargetTenant( "Sinh viên", "fa-solid fa-user-graduate" ),
                new TargetTenant("Người đi làm", "fa-solid fa-user-tie" ),
                new TargetTenant( "Gia đình", "fa-solid fa-house-user"),
                new TargetTenant( "Cặp đôi", "fa-solid fa-users-between-lines" )
        );
        targetTenantsRepository.saveAll(targets);
    }
}
