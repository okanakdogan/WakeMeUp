package akdogan.wakemeup

import akdogan.wakemeup.model.Alarm
import android.app.AlertDialog
import android.content.Context
import android.util.SparseArray
import android.widget.TextView
import androidx.core.util.size
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_alarm.view.*
import kotlinx.android.synthetic.main.text_day.view.*
import android.view.*


class AlarmAdapter(private val alarms : SparseArray<Alarm>,
                   private val dbHelper : DBHelper,
                   private val weekdays: ArrayList<String>,
                   private val  time_click_listener :View.OnClickListener,
                   private val  alarm_cancel_listener :MainActivity.OnCancelAlarmListener
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>(){

    private lateinit var context: Context
    private lateinit var inflater: LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        context = parent.context
        inflater =LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_alarm,parent,false)
        return AlarmViewHolder(v)

    }

    override fun getItemCount(): Int {
        return alarms.size
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[alarms.keyAt(position)]

        holder.itemView.tag = alarm
        holder.itemView.text_time.text = "%02d:%02d".format(alarm.hour, alarm.minute)

        holder.itemView.linear_days.removeAllViews()
        for ((index, day_name) in weekdays.withIndex()){
            val view = inflater.inflate(R.layout.text_day, null)
            view.toggle_day.text = day_name[0].toString()
            setTextColor(view.toggle_day,alarm.days[index])
            holder.itemView.linear_days.addView(view)

//            view.toggle_day.setOnClickListener{
//                val ind = holder.itemView.linear_days.indexOfChild(it)
//                alarm.days[ind] = !alarm.days[ind]
//                setTextColor(it as TextView, alarm.days[ind])
//            }
        }

        setViewTint(holder.itemView.button_on_off,alarm.on_off==1)
        holder.itemView.button_on_off.setOnClickListener {
            alarm.on_off = 1-alarm.on_off
            if (dbHelper.updateAlarm(alarm)){
                setViewTint(it,alarm.on_off==1)
            }else{
                //revert
                alarm.on_off = 1-alarm.on_off
            }

        }
        holder.itemView.setOnClickListener(time_click_listener)
        holder.itemView.setOnLongClickListener {
            val alert = AlertDialog.Builder(context)

            alert.setTitle(R.string.remove_alarm)
            alert.setMessage(R.string.remove_alarm_message)

            alert.setPositiveButton(R.string.yes){ _, _ ->
                val id = (it.tag as Alarm).id
                val result = dbHelper.removeAlarm(id)
                if (result) {
                    alarms.remove((it.tag as Alarm).id)
                    notifyDataSetChanged()
                    alarm_cancel_listener.onCancelAlarm(alarm)
                }
            }
            alert.setNegativeButton(R.string.no){_,_ -> }

            alert.show()
            true
        }
    }

    private fun setViewTint(view: View, isOn: Boolean){
        view.backgroundTintList = context.resources.getColorStateList(
            if(isOn) R.color.on else R.color.off
        )
    }

    private fun setTextColor(view: TextView, isOn: Boolean){
        view.setTextColor( context.resources.getColor(
            if(isOn) R.color.on else R.color.off
        ))
    }

    class AlarmViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)


}