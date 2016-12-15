package com.amap.android_location_markermove;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.TranslateAnimation;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener, LocationSource, AMapLocationListener {
    private AMap aMap;
    private MapView mapView;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;

    private LatLng mylocation;
    private LocOverlay mylocationoverlay;

    private AMapLocation myaMapLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
        //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
        // MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapClickListener(this);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);
        mylocationoverlay = LocOverlay.getInstance(aMap);//自定义定位overlay
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        deactivate();
        mylocationoverlay.remove();
    }

    /**
     * 地图点击回调
     * @param latLng 单击点的经纬度坐标
     */
    @Override
    public void onMapClick(LatLng latLng) {
        //手动模拟位置改变效果,仅用于测试
//        myaMapLocation.setLatitude(latLng.latitude);
//        myaMapLocation.setLongitude(latLng.longitude);
//        mylocationoverlay.locationChanged(myaMapLocation);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        startlocation();
        if (mylocation != null)
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 17));
    }

    @Override
    public void deactivate() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 开始定位。
     */
    private void startlocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置为单次定位
            mLocationOption.setOnceLocation(false);
            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
    }

    /**
     * 定位回调方法
     * @param aMapLocation 定位结果类。
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        myaMapLocation = aMapLocation;
        if (aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mylocation = new LatLng(aMapLocation.getLatitude(),
                        aMapLocation.getLongitude());
                mylocationoverlay.locationChanged(aMapLocation);//设置定位图标、精度圈以及移动效果
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": "
                        + aMapLocation.getErrorInfo();
                Toast.makeText(MainActivity.this, errText, Toast.LENGTH_SHORT).show();
                Log.e("AmapErr", errText);
            }
        }
    }
}
