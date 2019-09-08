package akdogan.wakemeup

import akdogan.wakemeup.model.Alarm
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TimePicker
import android.app.Activity
import android.content.Intent
import android.widget.CheckBox


class AlarmActivity : AppCompatActivity() {
    private lateinit var alarm :Alarm

    private var timePicker :TimePicker? = null
    private var applyButton :Button? = null
    private val checkBoxDays = ArrayList<CheckBox>()

    companion object {
        const val ALARM_PARAM = "alarm"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        bindViews()

        intent.extras?.let {
            alarm = it.getSerializable(ALARM_PARAM) as Alarm
            setTimepickerTime(alarm.hour,alarm.minute,timePicker)
            for(t in alarm.days zip checkBoxDays ){
                t.second.isChecked = t.first
            }
        }

        // day names
        for(t in checkBoxDays zip MainActivity.weekdays){
            t.first.text = t.second
        }

        applyButton?.setOnClickListener {

            //set data
            val hourNminute = getTimepickerTime(timePicker)
            alarm.hour = hourNminute.first
            alarm.minute = hourNminute.second

            for((i,box) in checkBoxDays.withIndex()){
                alarm.days[i]=box.isChecked
            }

            // send data back
            val resultIntent = Intent()
            resultIntent.putExtra(ALARM_PARAM, alarm)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }

    private fun bindViews(){
        timePicker = findViewById(R.id.time_picker)
        timePicker?.setIs24HourView(true)

        applyButton = findViewById(R.id.button_apply)

        checkBoxDays.addAll(arrayListOf(
            findViewById(R.id.check_day_0),
            findViewById(R.id.check_day_1),
            findViewById(R.id.check_day_2),
            findViewById(R.id.check_day_3),
            findViewById(R.id.check_day_4),
            findViewById(R.id.check_day_5),
            findViewById(R.id.check_day_6)
        ))
    }

    private fun getTimepickerTime(timePicker: TimePicker?):Pair<Int,Int>{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Pair(timePicker!!.hour,timePicker.minute)
        }else{
            Pair(timePicker!!.currentHour, timePicker.currentMinute)
        }
    }

    private fun setTimepickerTime(hour:Int, minute:Int, timePicker: TimePicker?){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker!!.hour=hour
            // after first access nullable object is successful, no need to call ?x operator (maybe using smart cast)
            timePicker.minute = minute

        }else{
            timePicker!!.currentHour = hour
            timePicker.currentMinute = minute
        }
    }
}
