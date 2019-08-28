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
 * Authors: Alexandr Cumarav
 * Created: July 27, 2019
 *
 */

package com.odysseusinc.athena.service.job;


import com.odysseusinc.athena.service.impl.VocabularyUpdatesChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@ConditionalOnProperty(name = "scheduled.vocabulary.checker")
@Component
public class VocabularyUpdatesJob {

    private final VocabularyUpdatesChecker vocabularyUpdatesChecker;

    @Autowired
    public VocabularyUpdatesJob(VocabularyUpdatesChecker vocabularyUpdatesChecker) {

        this.vocabularyUpdatesChecker = vocabularyUpdatesChecker;
    }

    @Scheduled(cron = "${scheduled.vocabulary.checker}")
    public void triggerCheckVocabularyUpdates() {

        vocabularyUpdatesChecker.sendVocabularyUpdatesNotification();
    }
}
