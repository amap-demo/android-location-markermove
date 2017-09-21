package com.amap.android_location_markermove;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.graphics.Color;
import android.util.Log;
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

    public LocOverlay(AMap amap) {
        this.aMap = amap;
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
        float bearing= aMapLocation.getBearing();
        locMarker.setRotateAngle(bearing);
        moveLocationMarker();
        locCircle.setRadius(radius);
    }

    /**
     * 平滑移动动画
     */
    private void moveLocationMarker() {
        final LatLng startPoint  = locMarker.getPosition();
        final LatLng endPoint  = point;
        float rotate = getRotate(startPoint, endPoint);
        locMarker.setRotateAngle(360 - rotate + aMap.getCameraPosition().bearing);
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint);
        anim.addUpdateListener(new AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LatLng target = (LatLng) valueAnimator.getAnimatedValue();
                if (locCircle != null){
                    locCircle.setCenter(target);
                }
                if (locMarker!= null)
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
            locMarker.destroy();
            locMarker = null;
        }
        if (locCircle != null){
            locCircle.remove();
            locCircle = null;
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

    /**
     * 根据经纬度计算需要偏转的角度
     *
     * @param curPos
     * @param nextPos
     * @return
     */
    private float getRotate(LatLng curPos, LatLng nextPos) {
        if(curPos==null||nextPos==null){
            return 0;
        }
        double x1 = curPos.latitude;
        double x2 = nextPos.latitude;
        double y1 = curPos.longitude;
        double y2 = nextPos.longitude;

        float rotate = (float) (Math.atan2(y2 - y1, x2 - x1) / Math.PI * 180);
        return rotate;
    }
}
