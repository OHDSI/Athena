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

package com.odysseusinc.athena.service.checker;

import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchDTOToSolrQuery;
import com.odysseusinc.athena.api.v1.controller.converter.ConceptSearchResultToDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.service.SolrService;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MaxLimit extends Checker {

    @Value("${solr.limit.max:20000}")
    private Integer maxLimit;
    @Autowired
    private SolrService solrService;
    @Autowired
    ConceptSearchResultToDTO converter;
    @Autowired
    private ConceptSearchDTOToSolrQuery converterToSolrQuery;

    @Override
    public String getDescription() {

        return "Exceeded the limit of response data.";
    }

    @Override
    public boolean check(ConceptSearchDTO searchDTO) throws IOException, SolrServerException {

        SolrQuery solrQuery = converterToSolrQuery.createQuery(searchDTO);
        QueryResponse solrResponse = solrService.search(solrQuery);
        return solrResponse.getResults().getNumFound() < maxLimit;
    }

}
