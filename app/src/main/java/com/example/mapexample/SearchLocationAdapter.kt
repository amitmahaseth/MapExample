package com.example.mapexample

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class SearchLocationAdapter(
    private val placesClient: PlacesClient, val context: Context,
    private val placesList: MutableList<AutocompletePrediction>,
    private val onLocationRvItemsClicked: OnLocationRvItemsClicked
) : RecyclerView.Adapter<SearchLocationAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.location_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

//    @SuppressLint("NotifyDataSetChanged")
//    fun addItems(list: List<AutocompletePrediction>) {
//        this.placesList.clear()
//        this.placesList.addAll(list)
//        notifyDataSetChanged()
//    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.tvName.text = placesList[position].getPrimaryText(null)

        holder.itemView.setOnClickListener {
            try {
                val placeFields = listOf(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
                val request =
                    FetchPlaceRequest.builder(placesList[position].placeId, placeFields).build()
                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    val place = response.place
                    onLocationRvItemsClicked.onLocationsItemClicked(
                        placesList[position].getPrimaryText(
                            null
                        ).toString() + " " + placesList[position].getSecondaryText(
                            null
                        ).toString(), place.latLng
                    )
                }.addOnFailureListener { Log.e("Exception", "" + it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)

    }
}

interface OnLocationRvItemsClicked {
    fun onLocationsItemClicked(place: String, latLng: LatLng?)

}