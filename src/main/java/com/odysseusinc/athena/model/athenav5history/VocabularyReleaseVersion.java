/*
 *
 * Copyright 2024 Odysseus Data Services, inc.
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
 * Authors: Yaroslav Molodkov
 * Created: January 11, 2024
 *
 */

package com.odysseusinc.athena.model.athenav5history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "vocabulary_release_version")
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class VocabularyReleaseVersion {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "vocabulary_name", nullable = false)
    private String vocabularyName;

    @Column(name = "athena_name", nullable = false)
    private String athenaName;

    @Column(name = "import_datetime")
    private Instant importDate;

    @Column(name = "cached_datetime")
    private Instant cachedDate;

}