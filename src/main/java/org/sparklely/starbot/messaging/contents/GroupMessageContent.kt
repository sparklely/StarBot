package org.sparklely.starbot.messaging.contents

import org.sparklely.starbot.messaging.sender.MessageSender

class GroupMessageContent(override val message: String, override val sender: MessageSender) : MessageContent {
    var groupId: Long = 0
}