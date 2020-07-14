package com.odysseusinc.athena.service.mail;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class EmailRecipients {

    @Builder.Default
    private List<String> to = new ArrayList<>();
    @Builder.Default
    private List<String> cc = new ArrayList<>();
    @Builder.Default
    private List<String> bcc = new ArrayList<>();
    private String replyTo;
}
