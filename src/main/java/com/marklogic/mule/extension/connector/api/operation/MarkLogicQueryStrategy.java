/**
 * MarkLogic Mule Connector
 *
 * Copyright © 2020 MarkLogic Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * This project and its code and functionality is not representative of MarkLogic Server and is not supported by MarkLogic.
 */
package com.marklogic.mule.extension.connector.api.operation;

import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.*;
import org.apache.commons.jexl3.*;

/**
 * Created by jkrebs on 4/9/2019.
 */
public enum MarkLogicQueryStrategy
{
    RawStructuredQueryDefinition {
        @Override
        public QueryDefinition getQueryDefinition(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName) {
            return createRawStructuredQuery(queryManager,queryString,fmt,optionsName);
        }

        @Override
        public QueryBatcher newQueryBatcher(DataMovementManager dmm, QueryDefinition query) {
            return dmm.newQueryBatcher((RawStructuredQueryDefinition) query);
        }
    },
    StructuredQueryBuilder{
        @Override
        public QueryDefinition getQueryDefinition(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName) {
            return createStructuredQuery(queryManager,queryString,optionsName);
        }
        @Override
        public QueryBatcher newQueryBatcher(DataMovementManager dmm, QueryDefinition query) {
            return dmm.newQueryBatcher((StructuredQueryDefinition) query);
        }
    },
    CTSQuery {
        @Override
        public QueryDefinition getQueryDefinition(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName) {
            return createCtsQuery(queryManager,queryString,fmt,optionsName);
        }
        @Override
        public QueryBatcher newQueryBatcher(DataMovementManager dmm, QueryDefinition query) {
            return dmm.newQueryBatcher((RawCtsQueryDefinition) query);
        }
    };

    public abstract QueryDefinition getQueryDefinition(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName);
    public abstract QueryBatcher newQueryBatcher(DataMovementManager dmm, QueryDefinition query);

    protected com.marklogic.client.query.RawStructuredQueryDefinition createRawStructuredQuery(QueryManager qManager, String structuredQuery, MarkLogicQueryFormat fmt, String optionsName)
    {
        return qManager.newRawStructuredQueryDefinition(new StringHandle().withFormat(fmt.getMlClientFormat()).with(structuredQuery), optionsName);
    }

    protected StructuredQueryDefinition createStructuredQuery(QueryManager qManager, String structuredQuery, String optionsName)
    {
        JexlEngine jexl = new JexlBuilder().create();
        JexlExpression e = jexl.createExpression(structuredQuery);
        JexlContext jc = new MapContext();
        if (optionsName == null)
        {
            jc.set("sb", qManager.newStructuredQueryBuilder());
        }
        else
        {
            jc.set("sb", qManager.newStructuredQueryBuilder(optionsName));
        }
        Object o = e.evaluate(jc);
        return (StructuredQueryDefinition) o;
    }

    protected QueryDefinition createCtsQuery(QueryManager queryManager, String queryString, MarkLogicQueryFormat fmt, String optionsName)
    {
        return queryManager.newRawCtsQueryDefinitionAs(fmt.getMlClientFormat(), queryString, optionsName);
    }

}
