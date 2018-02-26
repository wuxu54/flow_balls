package vm.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import vm.interfaces.FlowBallInterface;
import vm.viewManager.FlowBallManager;

/**
 * 使用范例
 */
public class MainActivity extends Activity implements FlowBallInterface {
    FlowBallManager flowBallManager;

    private Dialog dialog = null;

    private final String[] permissions = {Manifest.permission.SYSTEM_ALERT_WINDOW};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flowBallManager = FlowBallManager.getInstance(this, getApplication());
        flowBallManager = FlowBallManager.getInstance(this, getApplication());
        flowBallManager.setFlowBallInterface(this);
        flowBallManager.setFlowBallViewBackground();
        flowBallManager.setFlowBallParams(200, 200);//先设置params

        //此部分隐藏掉，参数也经过处理了，这个是公司业务所需参数，可忽略
       /* Map<String, Object> map = new HashMap<>();
        map.put("timestamp", "20170120135203528");
        map.put("merchantNo", "st0102201");
        map.put("busiCode", "2");
        map.put("phoneNo", "15822361111");
        flowBallManager.setFlowNumberParams(map);*/

 /*       Map<String, Object> maps = new HashMap<>();
        maps.put("merchantNo", "1223456");
        maps.put("token", "78dab357d232252e");
        maps.put("password", "Np12122");
        flowBallManager.setFlowPayParams(maps);*/


       // flowBallManager.showFlowBall();
        flowBallManager.checkPermissionOrShowFlowBall(this);
        /*//检查权限
        flowBallManager.checkPermission(this);
        //申请权限
        flowBallManager.applyPermission(this);*/

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(flowBallManager.checkPermission(this)){
            //权限已开启
            flowBallManager.showFlowBall();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        flowBallManager.hideFlowBall();
    }
    @Override
    public void setBallBackground() {
        flowBallManager.flowBallView.drawable = ContextCompat.getDrawable(this, R.drawable.logo);
        flowBallManager.flowBallView.textColor = Color.RED;
    }
}
