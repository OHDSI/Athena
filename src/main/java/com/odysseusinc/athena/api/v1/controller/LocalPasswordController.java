package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.api.v1.controller.dto.RemindPasswordDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ResetPasswordDTO;
import com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse;
import com.odysseusinc.athena.service.registration.PasswordRecoveryService;
import com.odysseusinc.athena.service.registration.AccountEmailQueue;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.NO_ERROR;
import static com.odysseusinc.athena.api.v1.controller.dto.arachne.ArachnePortalResponse.ErrorCode.VALIDATION_ERROR;

@RestController
@RequestMapping("/api/v1/users")
public class LocalPasswordController {
    private final PasswordRecoveryService recovery;
    private final AccountEmailQueue queue;

    private final AccountRequestLimits limits;

    public LocalPasswordController(PasswordRecoveryService recovery, AccountEmailQueue queue, AccountRequestLimits limits) {
        this.queue = queue;
        this.limits = limits;
        this.recovery = recovery;
    }

    @PostMapping("/remind-password")
    public ArachnePortalResponse<Void> remind(@Valid @RequestBody RemindPasswordDTO form, HttpServletRequest request) {
        if (limits.allowReminder(request.getRemoteAddr(), form.getEmail())) {
            queue.enqueueReset(form.getEmail());
        }
        return new ArachnePortalResponse<>(NO_ERROR);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ArachnePortalResponse<Void>> reset(@Valid @RequestBody ResetPasswordDTO form,
                                                            HttpServletRequest request) {
        int delay = limits.resetDelay(request.getRemoteAddr());
        if (delay > 0) {
            return AccountRequestLimits.retryLater(delay);
        }
        String password = form.getPassword();
        if (password.isBlank() || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return invalid("Use at least 8 characters and at most 72 UTF-8 bytes for your password.");
        }
        if (!recovery.reset(form.getEmail(), form.getToken(), password)) {
            return invalid("This reset link is invalid or has expired. Request a new link.");
        }
        return ResponseEntity.ok(new ArachnePortalResponse<>(NO_ERROR));
    }

    private ResponseEntity<ArachnePortalResponse<Void>> invalid(String message) {
        var response = new ArachnePortalResponse<Void>(VALIDATION_ERROR);
        response.setErrorMessage(message);
        return ResponseEntity.badRequest().body(response);
    }
}
