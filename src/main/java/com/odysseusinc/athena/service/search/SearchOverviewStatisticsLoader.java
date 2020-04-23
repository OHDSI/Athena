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
 * Created: April 22, 2020
 *
 */


package com.odysseusinc.athena.service.search;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchDTO;
import com.odysseusinc.athena.api.v1.controller.dto.ConceptSearchResultDTO;
import com.odysseusinc.athena.exceptions.AthenaException;
import com.odysseusinc.athena.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.odysseusinc.athena.model.common.AthenaConstants.DOMAIN_FACET_KEY;

@Configuration
public class SearchOverviewStatisticsLoader {

    private static final Logger log = LoggerFactory.getLogger(SearchOverviewStatisticsLoader.class);

    private final Cache<String, Map<String, Long>> cache;
    private final SearchService searchService;

    public SearchOverviewStatisticsLoader(SearchService searchService) {
        this.searchService = searchService;
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .build();
    }

    public Map<String, Long> getDomainTermsStatistics() {
        try {
            return cache.get(DOMAIN_FACET_KEY, () -> loadDomainTermsStatistics(DOMAIN_FACET_KEY));
        } catch (Exception ex) {
            cache.invalidate(DOMAIN_FACET_KEY);
            throw new AthenaException("Cannot load terms count cache", ex);
        }
    }

    private Map<String, Long> loadDomainTermsStatistics(String facetKey) throws IOException, SolrServerException {

        log.debug("Refreshing terms count for the: {}", facetKey);
        ConceptSearchDTO searchDTO = new ConceptSearchDTO();
        searchDTO.setQuery(StringUtils.EMPTY);
        ConceptSearchResultDTO search = searchService.search(searchDTO);
        final Map<String, Map<String, Long>> facet = search.getFacets();
        final Map<String, Long> domainData = facet.get(facetKey);
        return Collections.unmodifiableMap(domainData);
    }
}
