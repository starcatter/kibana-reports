/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package com.amazon.opendistroforelasticsearch.reportsscheduler.resthandler

import com.amazon.opendistroforelasticsearch.reportsscheduler.ReportsSchedulerPlugin.Companion.BASE_REPORTS_URI
import com.amazon.opendistroforelasticsearch.reportsscheduler.action.ReportInstanceActions
import com.amazon.opendistroforelasticsearch.reportsscheduler.model.IRestResponse
import com.amazon.opendistroforelasticsearch.reportsscheduler.model.InContextReportCreateRequest
import com.amazon.opendistroforelasticsearch.reportsscheduler.model.OnDemandReportCreateRequest
import com.amazon.opendistroforelasticsearch.reportsscheduler.model.RestErrorResponse
import org.elasticsearch.client.node.NodeClient
import org.elasticsearch.rest.RestChannel
import org.elasticsearch.rest.RestHandler.Route
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.rest.RestRequest.Method.POST
import org.elasticsearch.rest.RestRequest.Method.PUT
import org.elasticsearch.rest.RestStatus

/**
 * Rest handler for creating on-demand report instances.
 * This handler uses [ReportInstanceActions].
 */
internal class OnDemandReportRestHandler : PluginRestHandler() {
    companion object {
        private const val REPORT_INSTANCE_LIST_ACTION = "on_demand_report_actions"
        private const val ON_DEMAND_REPORT_URL = "$BASE_REPORTS_URI/on-demand"
    }

    /**
     * {@inheritDoc}
     */
    override fun getName(): String {
        return REPORT_INSTANCE_LIST_ACTION
    }

    /**
     * {@inheritDoc}
     */
    override fun routes(): List<Route> {
        return listOf(
            /**
             * Create a new report instance from provided definition
             * Request URL: PUT ON_DEMAND_REPORT_URL
             * Request body: Ref [com.amazon.opendistroforelasticsearch.reportsscheduler.model.InContextReportCreateRequest]
             * Response body: Ref [com.amazon.opendistroforelasticsearch.reportsscheduler.model.InContextReportCreateResponse]
             */
            Route(PUT, ON_DEMAND_REPORT_URL),

            /**
             * Create a new report from definition and return instance
             * Request URL: POST ON_DEMAND_REPORT_URL?id=<reportDefinitionId>
             * Request body: Ref [com.amazon.opendistroforelasticsearch.reportsscheduler.model.OnDemandReportCreateRequest]
             * Response body: Ref [com.amazon.opendistroforelasticsearch.reportsscheduler.model.OnDemandReportCreateResponse]
             */
            Route(POST, ON_DEMAND_REPORT_URL)
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun responseParams(): Set<String> {
        return setOf(ID_FIELD)
    }

    /**
     * {@inheritDoc}
     */
    override fun executeRequest(request: RestRequest, client: NodeClient, channel: RestChannel): IRestResponse {
        val parser = request.contentParser()
        parser.nextToken()
        return when (request.method()) {
            PUT -> ReportInstanceActions.createOnDemand(InContextReportCreateRequest.parse(parser))
            POST -> {
                val reportDefinitionId = request.param(ID_FIELD) ?: throw IllegalArgumentException("Must specify id")
                ReportInstanceActions.createOnDemandFromDefinition(OnDemandReportCreateRequest.parse(parser, reportDefinitionId))
            }
            else -> RestErrorResponse(RestStatus.METHOD_NOT_ALLOWED, "${request.method()} is not allowed")
        }
    }
}