package org.sparklely.starbot.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class Websocket : WebSocketListener() {

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        println("Received message from server: $text")
        // 处理接收到的文本消息
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
        println("Received bytes from server: ${bytes.hex()}")
        // 处理接收到的字节消息
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        println("WebSocket closing: $code / $reason")
        // 这里可以决定是否要关闭连接，或者做一些清理工作
        // 但通常OkHttp会处理关闭过程，除非你有特殊需求
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        println("WebSocket closed: $code / $reason")
        // 连接已经关闭，可以在这里进行最后的清理工作
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        super.onFailure(webSocket, t, response)
        println("WebSocket failed: ${t.message}")
        t.printStackTrace()
        // 处理连接失败的情况，可能是网络问题或其他错误
    }

    fun connect(url:String){
        val client= OkHttpClient.Builder().build()
        val request= Request.Builder().url(url).build()
        client.newWebSocket(request,this)
    }

}