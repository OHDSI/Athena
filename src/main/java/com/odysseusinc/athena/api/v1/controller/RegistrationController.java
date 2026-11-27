package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import com.odysseusinc.athena.api.v1.controller.dto.arachne.UserRegistrationDTO;
import com.odysseusinc.athena.service.registration.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.ALREADY_EXIST;
import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.NO_ERROR;
import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.VALIDATION_ERROR;

@RestController
@RequestMapping("/api/v1/users")
public class RegistrationController {

    private final RegistrationService registration;

    private final AccountRequestLimits limits;

    public RegistrationController(RegistrationService registration, AccountRequestLimits limits) {
        this.limits = limits;
        this.registration = registration;
    }

    @PostMapping
    public ResponseEntity<ArachnePortalResponse<Void>> register(@Valid @RequestBody UserRegistrationDTO form,
                                                                BindingResult errors, HttpServletRequest request) {
        int delay = limits.registrationDelay(request.getRemoteAddr(), form.getEmail());
        if (delay > 0) return AccountRequestLimits.retryLater(delay);
        var response = new ArachnePortalResponse<Void>(VALIDATION_ERROR);
        if (errors.hasErrors()) {
            errors.getFieldErrors().forEach(e -> response.getValidatorErrors().putIfAbsent(e.getField(), e.getDefaultMessage()));
            response.setErrorMessage("Please check the registration fields.");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            registration.register(form);
        } catch (RegistrationService.Rejected e) {
            if ("email".equals(e.field())) {
                response.setErrorCode(ALREADY_EXIST.getCode());
            }
            response.getValidatorErrors().put(e.field(), e.getMessage());
            response.setErrorMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(new ArachnePortalResponse<>(NO_ERROR));
    }

    // Previously issued emails point to this GET. Landing on it never consumes the token.
    @GetMapping("/confirm-email")
    public ResponseEntity<Void> confirmationPage(@RequestParam("token") @Size(max = 128) String token) {
        URI page = UriComponentsBuilder.fromPath("/auth/confirm-email").fragment("token={token}")
                .encode().buildAndExpand(token).toUri();
        return ResponseEntity.status(HttpStatus.SEE_OTHER).header("Referrer-Policy", "no-referrer")
                .header("Cache-Control", "no-store").location(page).build();
    }

    public record Confirmation(@NotBlank @Size(max = 128) String token) {}

    @PostMapping("/confirm-email")
    public ResponseEntity<ArachnePortalResponse<Void>> confirm(@Valid @RequestBody Confirmation form) {
        if (!registration.confirm(form.token())) {
            var error = new ArachnePortalResponse<Void>(VALIDATION_ERROR);
            error.setErrorMessage("This confirmation link is invalid or has expired.");
            return ResponseEntity.badRequest().body(error);
        }
        return ResponseEntity.ok(new ArachnePortalResponse<>(NO_ERROR));
    }
}
