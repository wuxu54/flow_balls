package vm.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import view.FlowBallView;
import vm.viewManager.FlowBallManager;

/**
 * Created by SpongeBob on 2017/9/19.
 */

public class FlowBallService extends Service {
    private FlowBallManager flowBallManager;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
