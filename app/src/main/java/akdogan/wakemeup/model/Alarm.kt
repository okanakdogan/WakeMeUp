package akdogan.wakemeup.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


data class Alarm (
        val id: Int,
        var hour: Int,
        var minute: Int,
        var on_off: Int,
        val days: BooleanArray,
        val melody: Int
): Serializable