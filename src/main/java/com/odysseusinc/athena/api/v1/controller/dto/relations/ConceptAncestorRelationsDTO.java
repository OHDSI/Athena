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

package com.odysseusinc.athena.api.v1.controller.dto.relations;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class ConceptAncestorRelationsDTO {
    private LinkedHashSet<ShortTermDTO> terms = new LinkedHashSet<>();
    private List<LinkDTO> links = new LinkedList<>();

    private long connectionsCount = 0;

    public LinkedHashSet<ShortTermDTO> getTerms() {

        return terms;
    }

    public void setTerms(LinkedHashSet<ShortTermDTO> terms) {

        this.terms = terms;
    }

    public List getLinks() {

        return links;
    }

    public void setLinks(List<LinkDTO> links) {

        this.links = links;
    }

    public long getConnectionsCount() {

        return connectionsCount;
    }

    public void setConnectionsCount(long connectionsCount) {

        this.connectionsCount = connectionsCount;
    }
}
