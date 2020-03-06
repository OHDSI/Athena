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

package com.odysseusinc.athena.service.impl;

import com.odysseusinc.athena.service.SolrService;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SolrServiceImpl implements SolrService {

    @Autowired
    private SolrClient solrClient;

    @Value("${athena.solrServerUrl}")
    private String serverUrl;

    @Value("${athena.solrUser}")
    private String solrUser;

    @Value("${athena.solrPassword}")
    private String solrPassword;

    @Override
    public QueryResponse search(SolrQuery solrQuery) throws IOException, SolrServerException {

        SolrRequest<QueryResponse> req = new QueryRequest(solrQuery);
        req.setMethod(SolrRequest.METHOD.POST);
        req.setBasicAuthCredentials(solrUser, solrPassword);
        return req.process(solrClient);
    }
}
