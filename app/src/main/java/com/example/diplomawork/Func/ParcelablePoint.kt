import android.os.Parcel
import android.os.Parcelable
import com.yandex.mapkit.geometry.Point

data class ParcelablePoint(val latitude: Double, val longitude: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ParcelablePoint> {
        override fun createFromParcel(parcel: Parcel): ParcelablePoint {
            return ParcelablePoint(parcel)
        }

        override fun newArray(size: Int): Array<ParcelablePoint?> {
            return arrayOfNulls(size)
        }
    }

    fun toPoint() = Point(latitude, longitude)
}
