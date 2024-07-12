package org.sparklely.starbot.messaging

enum class MessageTypes(val value: Int){
    TEXT(0),
    MARKDOWN(2),
    ARK(3),
    EMBED(4),
    MEDIA(7)
}
