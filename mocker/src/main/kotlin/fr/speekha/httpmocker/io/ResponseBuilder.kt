/*
 * Copyright 2019 David Blanc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.speekha.httpmocker.io

import fr.speekha.httpmocker.HTTP_RESPONSES_CODE
import fr.speekha.httpmocker.getLogger
import fr.speekha.httpmocker.model.ResponseDescriptor
import fr.speekha.httpmocker.responseNotFound
import fr.speekha.httpmocker.scenario.ScenarioProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

internal class ResponseBuilder(
    private val request: Request,
    private val response: ResponseDescriptor = responseNotFound(),
    private val provider: ScenarioProvider? = null
) {

    private val logger = getLogger()

    fun buildResponse(): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(response.code)
        .message(messageForHttpCode(response.code))
        .addHeaders(response)
        .body(loadResponseBody(response))
        .build()

    private fun Response.Builder.addHeaders(response: ResponseDescriptor) = apply {
        header("Content-type", response.mediaType)
        response.headers.forEach {
            if (it.value != null) {
                header(it.name, it.value)
            }
        }
    }

    private fun loadResponseBody(response: ResponseDescriptor) = (response.bodyFile?.let {
        logger.info("Loading response body from file: $it")
        provider?.loadResponseBody(request, it)
    } ?: response.body.toByteArray()
        ).toResponseBody(response.mediaType.toMediaTypeOrNull())

    private fun messageForHttpCode(httpCode: Int) =
        HTTP_RESPONSES_CODE[httpCode] ?: "Unknown error code"
}
