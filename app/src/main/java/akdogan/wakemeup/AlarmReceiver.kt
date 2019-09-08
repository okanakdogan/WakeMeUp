package akdogan.wakemeup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver :BroadcastReceiver() {
    // this hold pendingIntend id if one pendingIntend trigger. The PendingIntent'id is alarm'id
    companion object {
        var pendingId :Int=0
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent != null && context!=null) {
            val intentToService = Intent(context, AlarmService::class.java)
            val intentType = intent.extras?.getString("intentType")

            val ADD_INTENT = context.getString(R.string.add_intent)
            val OFF_INTENT = context.getString(R.string.off_intent)

            when(intentType){
                ADD_INTENT->{
                    pendingId = intent.extras?.getInt("PendingId") as Int
                    intentToService.putExtra("ON_OFF", ADD_INTENT)
                    context.startService(intentToService)
                }
                OFF_INTENT->{
                    val alarmId = intent.extras?.getInt("AlarmId")
                    // sending to AlarmService
                    intentToService.putExtra("ON_OFF", OFF_INTENT)
                    intentToService.putExtra("AlarmId", alarmId)
                    context.startService(intentToService)
                }
            }
        }
    }
}