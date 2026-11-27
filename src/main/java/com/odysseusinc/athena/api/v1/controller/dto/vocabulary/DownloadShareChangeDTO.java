/*
 *
 * Copyright 2018 Odysseus Data Services, inc.
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
 * Authors: Pavel Grafkin, Vitaly Koulakov, Maria Pozhidaeva
 * Created: April 4, 2018
 *
 */

package com.odysseusinc.athena.api.v1.controller.dto.vocabulary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

public class DownloadShareChangeDTO {
    @NotNull
    @Size(max = 25599)
    private String emailList;

    @JsonIgnore
    @Size(max = 100)
    public List<@NotBlank @Size(max = 255) @Email String> getRecipients() {
        if (emailList == null || emailList.isEmpty()) return List.of();
        return Arrays.stream(emailList.split(",", 101)).map(String::strip).toList();
    }

    public String getEmailList() {
        return emailList;
    }

    public void setEmailList(String emailList) {
        this.emailList = emailList;
    }
}
