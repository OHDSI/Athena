/*
 *
 * Copyright 2020 Odysseus Data Services, inc.
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
 * Created: March 31, 2020
 *
 */


package com.odysseusinc.athena.api.v1.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

import static com.odysseusinc.athena.model.common.AthenaConstants.COMMON_DATE_FORMAT;

@Data
public class DownloadHistoryDTO {
    private String code;
    private String email;
    private String userName;
    private String organization;
    private LocalDateTime date;


    @JsonFormat(pattern = COMMON_DATE_FORMAT)
    public LocalDateTime getDate() {
        return date;
    }
}
