package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import com.odysseusinc.athena.repositories.athena.RegistrationDictionaryRepository;
import com.odysseusinc.athena.repositories.athena.RegistrationDictionaryRepository.Location;
import com.odysseusinc.athena.repositories.athena.RegistrationDictionaryRepository.ProfessionalType;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.NO_ERROR;

@Tag(name = "UserController")
@RestController
@RequestMapping("/api/v1/users")
public class RegistrationDictionaryController {

    private final RegistrationDictionaryRepository dictionaries;

    public RegistrationDictionaryController(RegistrationDictionaryRepository dictionaries) {
        this.dictionaries = dictionaries;
    }

    @GetMapping("/professional-types")
    public ArachnePortalResponse<List<ProfessionalType>> professionalTypes() {
        return new ArachnePortalResponse<>(NO_ERROR, dictionaries.professionalTypes());
    }

    @GetMapping("/countries")
    public ArachnePortalResponse<List<Location>> countries(
            @RequestParam("query") String query, @RequestParam("limit") @Min(0) int limit,
            @RequestParam(value = "includeId", required = false) String includeId) {
        return new ArachnePortalResponse<>(NO_ERROR, dictionaries.countries(query, limit, includeId));
    }

    @GetMapping("/provinces")
    public ArachnePortalResponse<List<Location>> provinces(
            @RequestParam("countryId") String countryId, @RequestParam("query") String query,
            @RequestParam("limit") @Min(0) int limit,
            @RequestParam(value = "includeId", required = false) String includeId) {
        return new ArachnePortalResponse<>(NO_ERROR, dictionaries.provinces(countryId, query, limit, includeId));
    }
}
