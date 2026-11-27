package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import jakarta.validation.Validation;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DownloadShareChangeValidationTest {
    @Test void validatesAllAddressesAndRequiresExplicitClearing() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var dto = new DownloadShareChangeDTO();
            assertFalse(validator.validate(dto).isEmpty());
            dto.setEmailList("");
            assertTrue(validator.validate(dto).isEmpty());
            assertTrue(dto.getRecipients().isEmpty());
            for (String bad : new String[]{"  ", "ok@example.org,broken", "a@example.org,", "," ,
                    String.join(",", Collections.nCopies(101, "user@example.org"))}) {
                dto.setEmailList(bad);
                assertFalse(validator.validate(dto).isEmpty());
            }
            dto.setEmailList("a@example.org, b@example.org");
            assertTrue(validator.validate(dto).isEmpty());
            assertEquals("b@example.org", dto.getRecipients().get(1));
        }
    }
}
