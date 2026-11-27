package com.odysseusinc.athena.api.v1.controller;

import com.odysseusinc.athena.service.registration.AccountEmailQueue;
import com.odysseusinc.athena.service.registration.PasswordRecoveryService;
import com.odysseusinc.athena.service.registration.RegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountAdmissionTest {
    @Test void reminderQueueFailureReturnsTheSameAcknowledgementAsAnUnknownEmail() throws Exception {
        var queue = mock(AccountEmailQueue.class);
        var limits = mock(AccountRequestLimits.class);
        when(limits.allowReminder(anyString(), anyString())).thenReturn(true);
        String known = "known@example.org";
        doThrow(new DataIntegrityViolationException("queue write failed")).when(queue).enqueueReset(known);
        var mvc = MockMvcBuilders.standaloneSetup(new LocalPasswordController(mock(PasswordRecoveryService.class), queue, limits))
                .setControllerAdvice(new ExceptionHandlingController()).build();
        assertEquals(remind(mvc, "unknown@example.org"), remind(mvc, known));
        verify(queue).enqueueReset(known);
    }

    private String remind(MockMvc mvc, String email) throws Exception {
        return mvc.perform(post("/api/v1/users/remind-password").contentType("application/json")
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.errorCode").value(0))
                .andReturn().getResponse().getContentAsString();
    }

    @Test void limitedConfirmationDoesNotConsumeTheToken() throws Exception {
        var registration = mock(RegistrationService.class);
        var limits = mock(AccountRequestLimits.class);
        when(limits.confirmationDelay("127.0.0.1")).thenReturn(30);
        var mvc = MockMvcBuilders.standaloneSetup(new RegistrationController(registration, limits)).build();
        mvc.perform(post("/api/v1/users/confirm-email").contentType("application/json")
                        .content("{\"token\":\"test-token\"}"))
                .andExpect(status().isTooManyRequests()).andExpect(header().string("Retry-After", "30"));
        verifyNoInteractions(registration);
    }
}
