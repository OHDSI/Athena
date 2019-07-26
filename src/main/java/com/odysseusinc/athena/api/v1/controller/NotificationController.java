/*
 *
 * Copyright 2019 Odysseus Data Services, inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Ing. Alexandr Cumarav
 * Created: July 25, 2019
 *
 */


package com.odysseusinc.athena.api.v1.controller;


import com.odysseusinc.athena.api.v1.controller.converter.ConverterUtils;
import com.odysseusinc.athena.api.v1.controller.dto.VocabulariesForNotificationDTO;
import com.odysseusinc.athena.api.v1.controller.dto.vocabulary.VocabularyDTO;
import com.odysseusinc.athena.model.athena.Notification;
import com.odysseusinc.athena.model.security.AthenaUser;
import com.odysseusinc.athena.service.NotificationService;
import com.odysseusinc.athena.service.VocabularyService;
import com.odysseusinc.athena.service.impl.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;


@Api
@RestController
@RequestMapping(value = "/api/v1/notifications")
public class NotificationController {

    private final ConverterUtils converterUtils;
    private final NotificationService notificationService;
    private final UserService userService;
    private final VocabularyService vocabularyService;

    @Autowired
    public NotificationController(ConverterUtils converterUtils, NotificationService notificationService, UserService userService, VocabularyService vocabularyService) {
        this.converterUtils = converterUtils;
        this.notificationService = notificationService;
        this.userService = userService;
        this.vocabularyService = vocabularyService;
    }

    @PostMapping
    public ResponseEntity notifyAboutUpdates(
            @Valid @RequestBody VocabulariesForNotificationDTO dto, Principal principal) {

        final AthenaUser user = userService.getUser(principal);
        notificationService.updateNotificationSubscriptions(user.getId(), dto.getVocabularyCodes(), dto.getNotify());

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<VocabularyDTO>> getVocabulariesForNotification(Principal principal) {

        final AthenaUser user = userService.getUser(principal);
        List<Notification> notifications = vocabularyService.getNotifications(user.getId());
        List<VocabularyDTO> vocabularyDTOs = converterUtils.convertList(notifications, VocabularyDTO.class);
        return ResponseEntity.ok(vocabularyDTOs);
    }
}
