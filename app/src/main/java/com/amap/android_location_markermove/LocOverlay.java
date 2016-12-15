package com.amap.android_location_markermove;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

/**
 * Created by my94493 on 2016/12/15.
 */

public class LocOverlay {
    private static LocOverlay mlocoverlay;
    private LatLng point;
    private float radius;
    private Marker locMarker;
    private Circle locCircle;
    private AMap aMap;

    private LocOverlay(AMap amap) {
        this.aMap = amap;
    }

    public static LocOverlay getInstance(AMap amap){
        if (mlocoverlay == null) {
            mlocoverlay = new LocOverlay(amap);
        }
        return mlocoverlay;
    }

    /**
     * 位置变化时调用这个方法，实现marker位置变化
     * @param aMapLocation 定位返回的位置类
     */
    public void locationChanged (AMapLocation aMapLocation) {
        LatLng location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
        this.point = location;
        this.radius = aMapLocation.getAccuracy();
        if (locMarker == null) {
            addMarker();
        }
        if (locCircle == null) {
            addCircle();
        }
        moveLocationMarker();
        locCircle.setRadius(radius);
    }

    /**
     * 平滑移动动画
     */
    private void moveLocationMarker() {
        final LatLng startPoint  = locMarker.getPosition();
        final LatLng endPoint  = point;
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint);
        anim.addUpdateListener(new AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LatLng target = (LatLng) valueAnimator.getAnimatedValue();
                locCircle.setCenter(target);
                locMarker.setPosition(target);
            }
        });
        anim.setDuration(1000);
        anim.start();
    }
    /**
     * 添加定位marker
     */
    private void addMarker() {
        BitmapDescriptor des = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        locMarker = aMap.addMarker(new MarkerOptions().position(point).icon(des)
                .anchor(0.5f, 0.5f));

    }
    /**
     * 添加定位精度圈
     */
    private void addCircle() {
        locCircle = aMap.addCircle(new CircleOptions().center(point).radius(radius)
                .fillColor(Color.argb(100, 65, 105, 225))
                .strokeColor(Color.argb(255, 65, 105, 225))
                .strokeWidth(2));
    }
    public void remove(){
        if (locMarker != null){
            locMarker.remove();
        }
        if (locCircle != null){
            locCircle.remove();
        }
    }

    public class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            LatLng startPoint = (LatLng) startValue;
            LatLng endPoint = (LatLng) endValue;
            double x = startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude);
            double y = startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude);
            LatLng point = new LatLng(x, y);
            return point;
        }
    }
}
