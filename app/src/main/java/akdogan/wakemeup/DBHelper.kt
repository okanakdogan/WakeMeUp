package akdogan.wakemeup

import akdogan.wakemeup.model.Alarm
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.SparseArray

class DBHelper(val context: Context) : SQLiteOpenHelper(context,DBHelper.DATABASE_NAME,null,DBHelper.DATABASE_VERSION) {
    private val TABLE_NAME="Alarm"

    private val COL_ID = "id"
    private val COL_HOUR = "hour"
    private val COL_MINUTE = "minute"
    private val COL_DAYS = "days"
    private val COL_MELODY = "melody"
    private val COL_ON_OFF = "on_off"


    companion object {
        private val DATABASE_NAME = "ALARM_DATABASE"//database adı
        private val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COL_ID INTEGER, $COL_HOUR  INTEGER,$COL_MINUTE  INTEGER,$COL_DAYS  STRING,$COL_MELODY  INTEGER, $COL_ON_OFF INTEGER)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun addAlarm(alarm: Alarm): Boolean{
        val sqliteDB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_ID, alarm.id)
        contentValues.put(COL_HOUR, alarm.hour)
        contentValues.put(COL_MINUTE, alarm.minute)
        contentValues.put(COL_DAYS, daysToString(alarm.days))
        contentValues.put(COL_MELODY, alarm.melody)
        contentValues.put(COL_ON_OFF, alarm.on_off)

        val result = sqliteDB.insert(TABLE_NAME,null,contentValues)
        return result >= 1
    }
    fun getAlarms(): SparseArray<Alarm> {
        val alarmList = SparseArray<Alarm>()
        val sqliteDB = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val result = sqliteDB.rawQuery(query,null)
        if(result.moveToFirst()){
            do {
                val alarm = Alarm(
                    result.getInt(result.getColumnIndex(COL_ID)),
                    result.getInt(result.getColumnIndex(COL_HOUR)),
                    result.getInt(result.getColumnIndex(COL_MINUTE)),
                    result.getInt(result.getColumnIndex(COL_ON_OFF)),
                    stringToDays(result.getString(result.getColumnIndex(COL_DAYS))),
                    result.getInt(result.getColumnIndex(COL_MELODY))
                )
                alarmList.append(alarm.id,alarm)
            }while (result.moveToNext())
        }
        result.close()
        sqliteDB.close()
        return alarmList

    }

    fun updateAlarm(alarm: Alarm): Boolean {

        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_HOUR, alarm.hour)
        contentValues.put(COL_MINUTE, alarm.minute)
        contentValues.put(COL_DAYS, daysToString(alarm.days))
        contentValues.put(COL_MELODY, alarm.melody)
        contentValues.put(COL_ON_OFF, alarm.on_off)

        val retVal = db.update(TABLE_NAME, contentValues, "$COL_ID = " + alarm.id, null)

        db.close()
        return retVal >= 1
    }

    fun removeAlarm(id: Int):Boolean{
        val sqliteDB = this.writableDatabase
        val result = sqliteDB.delete(TABLE_NAME,"$COL_ID = $id",null)
        sqliteDB.close()
        return result >= 1
    }


    private fun daysToString(days :BooleanArray) :String{
        var s =""
        for (d in days){
            s += if (d) "T" else "F"
        }
        return s
    }

    private fun stringToDays(s :String) :BooleanArray{
        val arr = BooleanArray(7)
        for ((i,c) in s.withIndex()){
            arr[i] = c=='T'
        }
        return arr
    }
}