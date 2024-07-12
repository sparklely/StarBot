package org.sparklely.starbot.messaging.contents

import org.sparklely.starbot.messaging.sender.MessageSender

interface MessageContent {
    // TODO 完善
    val message: String
    val sender : MessageSender
}