package vm.viewManager;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import android.app.AlertDialog;
import android.app.Dialog;
import android.net.Uri;
import android.provider.Settings;

import utils.Constant;
import utils.GetUrl;
import utils.rom.HuaweiUtils;
import utils.rom.MeizuUtils;
import utils.rom.MiuiUtils;
import utils.rom.QikuUtils;
import utils.rom.RomUtils;
import view.FlowBallView;
import vm.activity.FlowActivity;
import vm.interfaces.FlowBallInterface;

/**
 * Created by SpongeBob on 2017/9/19.
 */

public class FlowBallManager {
    private static final String TAG = "FlowBallManager";
    private static FlowBallManager manager;
    private final Context context;
    private final Application application;
    private Dialog dialog;

    public FlowBallView flowBallView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    FlowBallInterface flowBallInterface;

    private FlowBallManager(Context context,Application application) {
        this.application = application;
        this.context = context;
        init();
    }

    /**
     * 获取FlowBallManager实例对象
     * @param context  上下文
     * @param application  应用Application
     * @return  FlowBallManager实例对象
     */
    public static FlowBallManager getInstance(Context context, Application application) {
        if (manager == null) {
            manager = new FlowBallManager(context,application);
        }
        return manager;
    }
    private void init() {
        windowManager = (WindowManager)application.getSystemService(Context.WINDOW_SERVICE);
        flowBallView = new FlowBallView(context);
        //设置触摸事件
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            //开始位置
            float startX;
            float startY;
            //初始位置
            float downX;
            float downY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = motionEvent.getRawX();
                        startY = motionEvent.getRawY();
                        downX = motionEvent.getRawX();
                        downY = motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        flowBallView.isDrag = true;
                        layoutParams.x = (int) motionEvent.getRawX() - layoutParams.width / 2;
                        layoutParams.y = (int) motionEvent.getRawY() - layoutParams.height;
                        windowManager.updateViewLayout(flowBallView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        flowBallView.isDrag = false;
                        float endX = motionEvent.getRawX();
                        float endY = motionEvent.getRawY();
                        float screenWidth = getScreenWidth();
                        if (endX < screenWidth / 2) {
                            endX = 0;
                        } else {
                            endX = screenWidth - flowBallView.width;
                        }

                        layoutParams.x = (int) endX;
                        windowManager.updateViewLayout(flowBallView, layoutParams);
                        flowBallView.invalidate();
                        //如果结束位置比起始位置  大于 6像素，表示这个触摸事件不是点击事件，拦截下来.否则作为点击事件处理，不进行拦截交给onClckLisener
                        if (Math.abs(endX - downX) > 6 && (Math.abs(endY - downY) > 6)) {
                            return true;
                        }
                        break;
                }
                return false;
            }
        };
        //设置点击事件监听
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //传递参数
                if(GetUrl.PAYURL_SAVE!=null){
                    Log.d(TAG, "pay_url: "+GetUrl.PAYURL_SAVE);
                    Intent intent = new Intent(context,FlowActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(context,"您未设置流量充值界面参数",Toast.LENGTH_SHORT).show();
                }
            }
        };
        flowBallView.setOnTouchListener(touchListener);
        flowBallView.setOnClickListener(clickListener);
    }

    /**
     * 悬浮球在windows中显示
     */
    public void showFlowBall() {
        if (layoutParams == null) {
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.width = flowBallView.width;
            layoutParams.height = flowBallView.height;
            layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
          //layoutParams.type = LayoutParams.TYPE_TOAST;

            String packname = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packname));
            if(permission){
                layoutParams.type  = LayoutParams.TYPE_PHONE;
            }else{
                layoutParams.type  = LayoutParams.TYPE_TOAST;
            }

            layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
            layoutParams.format = PixelFormat.RGBA_8888;
        }
        try {
            windowManager.addView(flowBallView, layoutParams);
            layoutParams.y = (int) (getScreenHeight() / 2);
            layoutParams.x = (int) (getScreenWidth() - flowBallView.width/2);
            windowManager.updateViewLayout(flowBallView,layoutParams);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void hideFlowBall(){
        if(flowBallView!=null&&windowManager!=null){
            try {
                windowManager.removeView(flowBallView);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
//
//    /**
//     * 当完全不使用flowBall时调用
//     */
//    public void closeFlowBall(){
//
//        if(flowBallView!=null&&windowManager!=null){
//            try {
//                windowManager.removeView(flowBallView);
//                flowBallView=null;
//            }catch (Exception e){
//            }
//        }
//    }

    /**
     * @return 获取手机屏幕高度
     */
    public float getScreenHeight() {
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point.y;
    }

    /**
     * @return 获取手机屏幕宽度
     */
    public float getScreenWidth() {
        Point point = new Point();
        windowManager.getDefaultDisplay().getSize(point);
        return point.x;

    }
    /**
     * @return 通过反射获取屏幕状态栏高度
     */
    public int getStatusHeight() {
        Class<?> c = null;
        Object object = null;
        Field field = null;
        int x = 0, statusBarHeight = 38;

        try {
            c = Class.forName("com.android.internal.R$dimen");
            object = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * @param width 流量球的宽度
     * @param height 流量球的高度
     * 小球的最终直径按宽和高的最小值算
     * 小球的高度同时还决定“流量值”TextView 与 “剩余流量”TextView文本之间的距离
     */
    public  void setFlowBallParams(int width,int height){
        if(width>0&&height>0){
            if(flowBallView!=null){
                flowBallView.width = width;
                flowBallView.height = height;
                flowBallView.invalidate();
            }
        }else{
            setFlowBallParams();
        }

    }

    /**
     * 设置流量球  按屏幕的百分比
     * 小球FlowBallView width = 屏幕宽 / 5
     * 小球FlowBallView height = 屏幕高 / 8
     * 小球的最终直径按宽和高的最小值算
     */
    public  void setFlowBallParams(){
        if(flowBallView!=null){
            flowBallView.width = (int) (getScreenWidth()/5);
            flowBallView.height = (int) (getScreenHeight()/8);
            flowBallView.invalidate();
        }
    }
    /**
     * 设置流量请求参数，获取流量值
     * @param map 流量值参数
     */
    public void setFlowNumberParams(Map<String,Object> map) {
        if(flowBallView!=null){
            flowBallView.setFlowNumber(map);
            flowBallView.invalidate();
        }
    }

    /**
     * 设置流量充值参数
     * @param map 流量充值参数
     */
    public void setFlowPayParams(Map<String,Object> map){
        if(map==null){
            return;
        }
        GetUrl.PAYURL_SAVE = Constant.FLOWPAY_SERVICE+GetUrl.getUrl(map);
       // Log.i(TAG, "run: getUrl----2 " + GetUrl.PAYURL_SAVE);
    }

    /**
     * 设置回调接口，用来让使用者自定义流量球背景图以及文字颜色
     * @param flowBallInterface  回调接口
     */
    public void setFlowBallInterface(FlowBallInterface flowBallInterface){
        this.flowBallInterface = flowBallInterface;
    }

    /**
     * 回调流量球背景图及文字颜色定义的方法
     */
    public void setFlowBallViewBackground(){
        flowBallInterface.setBallBackground();
    }


    public void checkPermissionOrShowFlowBall(Context context) {
        if (checkPermission(context)) {
            //showWindow(context);
          // Toast.makeText(context,"权限开启",Toast.LENGTH_SHORT).show();
            this.showFlowBall();
        } else {
            applyPermission(context);
        }
    }

    public boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    public void applyPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            }
        }
        commonROMPermissionApply(context);
    }

    private void ROM360PermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    QikuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void huaweiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void meizuROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MeizuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void miuiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context);
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, new OnConfirmResult() {
                    @Override
                    public void confirmResult(boolean confirm) {
                        if (confirm) {
                            try {
                                Class clazz = Settings.class;
                                Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

                                Intent intent = new Intent(field.get(null).toString());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                context.startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        } else {
                            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION");
                            //需要做统计效果
                        }
                    }
                });
            }
        }
    }

    private void showConfirmDialog(Context context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result);
    }

    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("")
                .setMessage(message)
                .setPositiveButton("现在去开启",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(true);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("暂不开启",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(false);
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }
    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
    }
}
