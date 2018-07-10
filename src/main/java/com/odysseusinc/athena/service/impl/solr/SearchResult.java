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

package com.odysseusinc.athena.service.impl.solr;

import java.util.List;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Created by PGrafkin on 31.01.2017.
 */
public class SearchResult<T> {

    private SolrQuery solrQuery;
    private QueryResponse solrResponse;
    private List<T> entityList;

    public SearchResult(SolrQuery solrQuery, QueryResponse solrResponse, List<T> entityList) {

        this.solrQuery = solrQuery;
        this.solrResponse = solrResponse;
        this.entityList = entityList;
    }

    public SolrQuery getSolrQuery() {

        return solrQuery;
    }

    public void setSolrQuery(SolrQuery solrQuery) {

        this.solrQuery = solrQuery;
    }

    public QueryResponse getSolrResponse() {

        return solrResponse;
    }

    public void setSolrResponse(QueryResponse solrResponse) {

        this.solrResponse = solrResponse;
    }

    public List<T> getEntityList() {

        return entityList;
    }

    public void setEntityList(List<T> entityList) {

        this.entityList = entityList;
    }
}
