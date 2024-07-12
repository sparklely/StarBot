package org.sparklely.starbot.network.QQBot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.sparklely.starbot.network.Request


class QQApi {

    private var appId:String = ""
    private var clientSecret:String = ""
    private var appAccessToken:MutableMap<String,String> =  mutableMapOf()

    fun login(appId:String,clientSecret:String){
        this.appId=appId
        this.clientSecret=clientSecret
    }

    fun getAppAccessToken(){
        val data= hashMapOf<String,Any>()
        data["appId"]=this.appId
        data["clientSecret"]=this.clientSecret
        val mapType: Type = object : TypeToken<Map<String, Any>>() {}.type
        this.appAccessToken = Gson().fromJson(Request().post("https://bots.qq.com/app/getAppAccessToken",
                        data = data).body(), mapType)
        this.appAccessToken["expires_time"]=""
    }
}