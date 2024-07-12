package org.sparklely.starbot.network.QQBot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.sparklely.starbot.network.Request


class QQApi {

    private val mapType: Type = object : TypeToken<Map<String, Any>>() {}.type
    private val apiAddress="https://api.sgroup.qq.com"

    private var appId:String = ""
    private var clientSecret:String = ""
    private var appAccessToken:MutableMap<String,Any> =  mutableMapOf()
    private var requestHeaders:HashMap<String,String> = hashMapOf()

    fun login(appId:String,clientSecret:String){
        this.appId=appId
        this.clientSecret=clientSecret
        getAppAccessToken()
    }

    private fun getAppAccessToken(){
        val data= hashMapOf<String,Any>()
        data["appId"]=appId
        data["clientSecret"]=clientSecret
        appAccessToken = Gson().fromJson(Request().post("https://bots.qq.com/app/getAppAccessToken", data = data).body(), mapType)
        appAccessToken["expires_time"]=System.currentTimeMillis()+Integer.parseInt(appAccessToken["expires_in"] as String)
        appAccessToken.remove("expires_in")
        requestHeaders["Authorization"]="QQBot ${appAccessToken["access_token"]}"
        requestHeaders["X-Union-Appid"]=appId
        requestHeaders["Content-Type"]="application/json"
    }

    fun checkAppAccessToken(){
        if(Integer.parseInt(appAccessToken["expires_in"] as String)-System.currentTimeMillis()<=60)
            getAppAccessToken()
    }

    private fun getWSAddress(): String?{
        var data = hashMapOf<String,String>()
        data = Gson().fromJson(Request().get("$apiAddress/gateway", header = requestHeaders).body(), mapType)
        return data["url"]
    }

}

