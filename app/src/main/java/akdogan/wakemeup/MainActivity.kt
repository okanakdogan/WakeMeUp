package akdogan.wakemeup

import akdogan.wakemeup.model.Alarm
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.util.isEmpty
import androidx.core.util.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormatSymbols
import java.util.*
import kotlin.collections.ArrayList
import android.app.AlarmManager
import android.app.PendingIntent

class MainActivity : AppCompatActivity() {

    private val NEW_ALARM_REQUEST = 1
    private val EDIT_ALARM_REQUEST = 2

    private var alarmRecyclerView :RecyclerView? = null
    private var alarmAdapter :AlarmAdapter? = null
    private var emptyView :TextView? = null
    private var addButton : ImageButton? =null

    private var dbHelper :DBHelper? = null
    private lateinit var alarms :SparseArray<Alarm>

    companion object{
        val weekdays = DateFormatSymbols().weekdays.toCollection(ArrayList()).also { it.removeAt(0) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()

        dbHelper = DBHelper(this)

        alarms = dbHelper!!.getAlarms()
        checkAlarmListForEmptyState(alarms)

        alarmAdapter = AlarmAdapter(alarms,dbHelper as DBHelper,weekdays,View.OnClickListener {
            startAlarmActivity(it.tag as Alarm,isNew=false)
        },object : OnCancelAlarmListener {
            override fun onCancelAlarm(alarm: Alarm) {
                cancelAlarm(alarm)
            }
        })
        alarmRecyclerView?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL,false)
        alarmRecyclerView?.adapter = alarmAdapter


    }

    private fun bindViews(){
        alarmRecyclerView = findViewById(R.id.recycler_alarm)
        emptyView = findViewById(R.id.text_empty_list)
        addButton = findViewById(R.id.ibutton_add)

        addButton?.setOnClickListener {
            startAlarmActivity(Alarm(alarms.size,0,0,1,BooleanArray(7),0),isNew = true)
        }
    }

    private fun startAlarmActivity(alarm: Alarm,isNew:Boolean){
        val intent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(AlarmActivity.ALARM_PARAM, alarm)
        }
        startActivityForResult(intent,
            if(isNew)NEW_ALARM_REQUEST else EDIT_ALARM_REQUEST)
    }
    private fun checkAlarmListForEmptyState(alarms :SparseArray<Alarm>){
        if (alarms.isEmpty()) {
            alarmRecyclerView?.visibility=View.GONE
            emptyView?.visibility=View.VISIBLE
        }
        else {
            alarmRecyclerView?.visibility = View.VISIBLE
            emptyView?.visibility = View.GONE
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == NEW_ALARM_REQUEST){
            if(resultCode== Activity.RESULT_OK){
                val newAlarm = data?.extras?.getSerializable(AlarmActivity.ALARM_PARAM) as Alarm
                if(!safeAddAlarm(newAlarm)){
                    Toast.makeText(this,"Alarm already exists.",Toast.LENGTH_SHORT).show()
                }else{
                    alarmAdapter?.notifyDataSetChanged()
                    checkAlarmListForEmptyState(alarms)
                }
            }
        }else if (requestCode == EDIT_ALARM_REQUEST){

            if (resultCode== Activity.RESULT_OK){
                val newAlarm = data?.extras?.getSerializable(AlarmActivity.ALARM_PARAM) as Alarm
                safeReplaceAlarm(newAlarm)
                alarmAdapter?.notifyDataSetChanged()
                checkAlarmListForEmptyState(alarms)
            }

        }
    }

    private fun safeAddAlarm(alarm: Alarm):Boolean{
        if (!checkDuplicateAlarm(alarm)){
            val result = dbHelper!!.addAlarm(alarm)
            if (result){
                setAlarm(alarm,0)
                alarms.append(alarm.id, alarm)

            }
            return result
        }
        return false
    }

    private fun safeReplaceAlarm(alarm: Alarm): Boolean{
        if (!checkDuplicateAlarm(alarm)){
            val result = dbHelper!!.updateAlarm(alarm)
            if (result){
                setAlarm(alarm, PendingIntent.FLAG_UPDATE_CURRENT)
                alarms.setValueAt(alarm.id, alarm)
            }
            return result
        }
        return false
    }

    private fun checkDuplicateAlarm(alarm: Alarm):Boolean{
        for(i in  0 until alarms.size){
            if (alarms[i].id != alarm.id && alarms[i].hour==alarm.hour && alarms[i].minute == alarm.minute){
                return true
            }
        }
        return false
    }

    private fun sendBroadcastIntent(alarm: Alarm, intentType: String) {
        val intent = Intent(this, AlarmReceiver::class.java).apply{
            putExtra("intentType", intentType)
            putExtra("AlarmId", alarm.id)
        }

        sendBroadcast(intent)
    }

    private fun setAlarm(alarm: Alarm, flags: Int) {

        val myCalendar = Calendar.getInstance()
        val calendar = myCalendar.clone()  as Calendar

        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)

        //TODO consider alarm.days array to set next alarm

        // plus one day if the time set less than the the Calendar current time
        if (calendar <= myCalendar) {
            calendar.add(Calendar.DATE, 1)
        }


        val intent = Intent(this, AlarmReceiver::class.java)
        // put intent type to check which intent trigger add or cancel
        intent.putExtra("intentType", getString(R.string.add_intent))
        // put id to intent
        intent.putExtra("pendingId", alarm.id)
        // this pendingIntent include alarm id  to manage
        val alarmIntent = PendingIntent.getBroadcast(this, alarm.id, intent, flags)
        // create alarm manager ALARM_SERVICE
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis, AlarmManager.INTERVAL_DAY, alarmIntent)

    }

    private fun cancelAlarm(alarm :Alarm) {

        alarm.on_off=0
        if (dbHelper!!.updateAlarm(alarm)){
            deleteCancel(alarm)
            // if alarm is triggered and ringing, send this alarm detail to AlarmReceiver
            // then AlarmReceiver send detail to service to stop music
            sendBroadcastIntent(alarm, getString(R.string.off_intent))
        }
    }


    private fun deleteCancel(alarm :Alarm) {

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(this, alarm.id, intent, 0)
        alarmManager.cancel(alarmIntent)
    }

    interface OnCancelAlarmListener{
        fun onCancelAlarm(alarm: Alarm)
    }
}
