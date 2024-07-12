package org.sparklely.starbot.network

import com.google.gson.Gson
import java.net.URI;
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Objects

class Request {

    fun get(
        url: String,
        query: Map<String, String> = hashMapOf(),
        header: Map<String, String> = hashMapOf()
    ): HttpResponse<String> {
        var allURL = "$url?"
        for (key in query.keys) {
            allURL += "f$key=${query[key]}"
        }
        // 建立get连接
        val request = HttpRequest.newBuilder()
            .uri(URI.create(allURL))
            .apply {
                header.forEach { (headerName, headerValue) ->
                    this.header(headerName, headerValue)
                }
            }
            .GET()
            .build()
        // 发送请求
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun post(
        url: String,
        query: Map<String, String> = hashMapOf(),
        header: Map<String, String> = hashMapOf(),
        data: Map<String,Any> = hashMapOf()
    ):HttpResponse<String> {
        var allURL = "$url?"
        for (key in query.keys) {
            allURL += "f$key=${query[key]}"
        }
        val request = HttpRequest.newBuilder()
            .uri(URI.create(allURL))
            .apply {
                header.forEach { (headerName, headerValue) ->
                    this.header(headerName, headerValue)
                }
            }
            .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(data)))
            .build()
        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    }
}