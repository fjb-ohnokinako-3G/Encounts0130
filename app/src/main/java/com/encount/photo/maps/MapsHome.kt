package com.encount.photo.maps

import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.encount.photo.PostList2
import com.encount.photo.R
import com.encount.photo.post.PostDetails
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_maps_home.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.lang.Exception

class MapsHome : Fragment(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mMap: GoogleMap? = null
    private val requestingLocationUpdates = true //フラグ
    private val locationRequest: LocationRequest = LocationRequest.create()
    //private var postList = mutableListOf<MapsList>()
    private var postList = mutableListOf<PostList2>()
    //取得した写真の件数を格納する
    private var cnt = 0
    //マップ上に打つピンを管理するための変数
    private var mmm: Marker? = null
    //下のfor文内で使うカウント変数
    var ccnt = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_maps_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val mapFragment: SupportMapFragment =
            getChildFragmentManager().findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        ivSpotPost.setOnClickListener {

            //スポット詳細画面へ遷移
            val intent = Intent(context, SpotMainActivity::class.java)
            startActivity(intent)
        }

        /**
         * 権限を求める処理を、起動画面にまとめて追加する。
         * 　＋以前に許可されていても、その後に拒否される可能性も考える必要がある。
         */
        // Android 6, API 23以上でパーミッションの確認
        if (Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            checkPermission(permissions, REQUEST_CODE)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        locationRequest.setInterval(10000)   //最遅の更新間隔
        locationRequest.setFastestInterval(5000)   //最速の更新間隔
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)           //バッテリー消費を抑えたい場合、精度は100m程度
        onResume()
    }

    //Update Result
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {

            locationResult ?: return
            for (location in locationResult.locations) {

                if (location != null) {

                    latitude = location.latitude
                    longitude = location.longitude

                    ivSpotPost.visibility = View.VISIBLE

                    //グローバル変数に位置情報を代入

                    mMap!!.setMyLocationEnabled(true)
                    //mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
                    val camPos = CameraPosition.Builder()
                        .target(LatLng(latitude, longitude)) // Sets the new camera position
                        .zoom(19f) // Sets the zoom
                        .bearing(0f) // Rotate the camera
                        .tilt(40f) // Set the camera tilt
                        .build()
                    mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(camPos))

                    //座標から住所変換のテスト
                    val geocoder = Geocoder(context)
                    //val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                    val addressList: List<Address>? =
                        geocoder.getFromLocation(latitude, longitude, 1)
                    val adminArea = addressList?.first()!!.adminArea
                    println(adminArea)

                    //MapPostGet(this,lat,lng).execute()で緯度経度を引数にして渡す
                    //MapPostGet(this@MapsHome).execute()
                    //サーバと通信する処理（インナークラス）を呼び出して実行する
                    SpotPhotoGet(this@MapsHome).execute()

                    Log.d("debug", "現在地の緯度" + location.latitude)
                    Log.d("debug", "現在地の経度" + location.longitude)

                    //写真が１件以上あれば、マップのピンを立てる処理を行う
                    if (cnt >= 1) {

                        ccnt = 0

                        Log.d("debug", "取得した写真の件数 : " + cnt)

                        //取得した写真の件数分ピンを打つ処理
                        //for(i in postList)にすると、初回の写真取得で数値がおかしくなるので、仕方なく変数を用意している。
                      
                        for (i in 0..cnt - 1) {

                            //前回マップ上に打ったピンを全て削除
                            if (mmm != null) {
                                mmm!!.remove()
                            }

                            val spot = LatLng(

                                postList[ccnt].imageLat.toDouble(),
                                postList[ccnt].imageLng.toDouble()
                            )

                            Glide.with(activity)
                                .asBitmap()
                                .load(postList[ccnt].imagePath)
                                .into(object : SimpleTarget<Bitmap>(100, 100) {

                                    //正常に写真取得できればピンを打つ
                                    override fun onResourceReady(
                                        resource: Bitmap?,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        mmm = mMap!!.addMarker(
                                            MarkerOptions()
                                                .position(spot)
                                                .title(postList[i].postId)
                                                .snippet(postList[i].userId)
                                                .icon(BitmapDescriptorFactory.fromBitmap(resource))
                                        )
                                    }
                                })
                            ccnt++
                        }
                    }
                }
            }
        }
    }

    /*fun setPostList(postList: MutableList<MapsList>) {
        this.postList = postList
    }*/
    fun setPostList(postList: MutableList<PostList2>) {
        this.postList = postList
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback, null /* Looper */
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //default location
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //マップのスタイルも変えられるようにしたい
        //mMap!!.setMapStyle(GoogleMap.MAP_TYPE_TERRAIN)
        /*
        val spot = LatLng(35.7044997, 139.9843911)
        val position = CameraPosition.Builder()
            .target(spot) // Sets the new camera position
            .zoom(18f) // Sets the zoom
            .bearing(0f) // Rotate the camera
            .tilt(60f) // Set the camera tilt
            .build() // Creates a CameraPosition from the builder
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(position))*/
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false

        mMap!!.setOnMarkerClickListener { marker ->
            val intent = Intent(context, PostDetails::class.java)
            intent.putExtra("Post_Id",marker.title)
            intent.putExtra("imageLat",postList[ccnt-1].imageLat)
            intent.putExtra("imageLng",postList[ccnt-1].imageLng)
            startActivity(intent)
            true
        }
    }

    //許可されていないパーミッションリクエスト
    fun checkPermission(permissions: Array<String>, request_code: Int) {
        ActivityCompat.requestPermissions(activity!!, permissions, request_code)
    }

    //結果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION) {
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permission", "Added Permission: " + permissions[i])
                    //mMap!!.setMyLocationEnabled(true)
                } else {
                    // パーミッションが拒否された
                    Log.d("Permission", "Rejected Permission: " + permissions[i])
                }
            }
        }
    }

    companion object {
        private val REQUEST_CODE = 1000
        private val REQUEST_PERMISSION = 1000
        //setter
        /*fun setPostList(mapsHome: MapsHome, mutableList: MutableList<MapsList>) {
            mapsHome.postList = mapsHome.postList
            Log.d("debug", "pass" + mapsHome.postList[0].imgpath)
        }*/
        fun setPostList(mapsHome: MapsHome, mutableList: MutableList<PostList2>) {
            mapsHome.postList = mapsHome.postList
            Log.d("debug", "pass" + mapsHome.postList[0])
        }
    }

    /**
     * ここから下はサーバに現在地を表示し、現在地周辺の写真を取得する処理
     */

    private inner class SpotPhotoGet(val activity: MapsHome) : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String {

            val client = OkHttpClient()

            //アクセスするURL
            val url = "https://encount.cf/encount/SpotInfoSend.php"

            //Formを作成
            val formBuilder = FormBody.Builder()

            println("経度" + latitude.toString())
            println("緯度" + longitude.toString())

            //Formに要素を追加
            formBuilder.add("latitude", latitude.toString())
            formBuilder.add("longitude", longitude.toString())

            //リクエスト内容にformを追加
            val form = formBuilder.build()

            //リクエストを生成
            val request = Request.Builder().url(url).post(form).build()

            try {
                val response = client.newCall(request).execute()
                println(url)
                //println(response.body()!!.string())
                return response.body()!!.string()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error"
            }
        }

        override fun onPostExecute(result: String) {
            try {
                var postList = mutableListOf<PostList2>()
                val listType = object : TypeToken<List<PostList2>>() {}.type
                val postData = Gson().fromJson<List<PostList2>>(result, listType)
                var postCount = 0

                for (i in postData) {

                    postCount++

                    postList.add(
                        PostList2(
                            i.imageId,
                            i.userId,
                            i.imagePath,
                            i.imageLat,
                            i.imageLng,
                            i.postId,
                            i.likeFlag
                        )
                    )
                }

                cnt = postCount
                activity.setPostList(postList)


            } catch (e: Exception) {

            }
        }
    }
}