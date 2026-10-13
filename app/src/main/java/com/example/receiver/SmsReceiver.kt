package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.BakriApplication
import com.example.data.model.ClientSmsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages: Array<SmsMessage>? = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Telephony.Sms.Intents.getMessagesFromIntent(intent)
                } else {
                    val bundle = intent.extras
                    val pdus = bundle?.get("pdus") as? Array<*>
                    pdus?.mapNotNull {
                        @Suppress("DEPRECATION")
                        SmsMessage.createFromPdu(it as ByteArray)
                    }?.toTypedArray()
                }
            } catch (e: Exception) {
                null
            }

            if (!messages.isNullOrEmpty()) {
                val fullBody = StringBuilder()
                var sender = ""
                for (sms in messages) {
                    sender = sms.displayOriginatingAddress ?: ""
                    fullBody.append(sms.displayMessageBody ?: "")
                }

                if (sender.isNotBlank()) {
                    val msgText = fullBody.toString()
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            BakriApplication.instance.repository.insertSmsMessage(
                                ClientSmsMessage(
                                    senderPhone = sender,
                                    messageBody = msgText,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        } catch (_: Exception) {
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
