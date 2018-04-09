/*
 *
 * Copyright 2018 Observational Health Data Sciences and Informatics
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

package com.odysseusinc.athena.service.graph;

import com.odysseusinc.athena.exceptions.NotExistException;
import com.odysseusinc.athena.model.athenav5.ConceptAncestorRelationV5;
import java.util.List;
import org.springframework.core.convert.support.GenericConversionService;

public class RelationshipGraphFactory {

    private RelationshipGraphFactory() {

    }

    public static RelationshipGraph getRelationshipGraph(int zoomLevel, Long id,
                                                         GenericConversionService conversionService,
                                                         List<ConceptAncestorRelationV5> relations) {
        switch (zoomLevel) {
            case 1:
                return new CommonCountGraph(id, conversionService, relations);
            case 2:
                return new VocabularyGraph(id, conversionService, relations);
            case 3:
                return new VocabularyAndConceptClassGraph(id, conversionService, relations);
            case 4:
                return new ConceptGraph(id, conversionService, relations);
            default:
                throw new NotExistException("There is no relationship graph for zoom level " + zoomLevel,
                        RelationshipGraph.class);
        }
    }
}