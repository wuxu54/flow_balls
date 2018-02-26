package view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Map;

import utils.Constant;
import utils.GetUrl;
import vm.activity.R;

/**
 * Created by SpongeBob on 2017/9/18.
 */

public class FlowBallView extends View {
    private int w = 0;
    private static final String TAG = "FlowBallView";
    private Paint ballPaint;
    private Paint textPaint;

    private String TEXT = "loading..";

    public String url;
    public int width = 150;
    public int height = 150;

    private boolean oneClick;//单击
    private boolean twoClick;//双击

    public boolean isDrag;//悬浮球被拖动

    private int mBorderThickness = 0;
    private Context context;
    private int defaultColor = 0xFFFFFFFF;
    private int mBorderOutsideColor = 0;// 图片的外边界
    private int mBorderInsideColor = 0;// 图片的内边界

    private Paint paint;
    private Paint paint1;
    public Drawable drawable;
    public int textColor = R.color.flowTextColor;
    public RequestQueue requestQueue;
    private  int i = 0;
    private Handler handler = new Handler(){ // 设置定时更新流量信息
        @Override
        public void handleMessage(Message msg) {
            //刷新界面
           // setFlowNumber(null);
//            Log.d(TAG, "handleMessage: ------ i ====" + i++);
//            invalidate();
        }
    };

    public FlowBallView(Context context) {
        this(context, null);
    }

    public FlowBallView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowBallView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        paint.setTypeface(Typeface.DEFAULT_BOLD);//文字加粗

        paint1 = new Paint();
        paint1.setAntiAlias(true);
        paint1.setColor(textColor);
        paint1.setTypeface(Typeface.DEFAULT_BOLD);


    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Drawable drawable = ContextCompat.getDrawable(context, R.drawable.background);
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, R.drawable.background);
           // return;
        }

        if (width == 0 || height == 0) {
            return;
        }
        this.measure(0, 0);

        if (drawable.getClass() == NinePatchDrawable.class)
            return;
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int radius = 0;
        if (mBorderInsideColor != defaultColor
                && mBorderOutsideColor != defaultColor) {// 定义画两个边框，分别为外圆边框和内圆边框
            radius = (width < height ? width
                    : height) / 2 - 2 * mBorderThickness;
            // 画内圆
            drawCircleBorder(canvas, radius + mBorderThickness / 2,
                    mBorderInsideColor);
            // 画外圆
            drawCircleBorder(canvas, radius + mBorderThickness
                    + mBorderThickness / 2, mBorderOutsideColor);
        } else if (mBorderInsideColor != defaultColor
                && mBorderOutsideColor == defaultColor) {// 定义画一个边框
            radius = (width < height ? width
                    : height) / 2 - mBorderThickness;
            drawCircleBorder(canvas, radius + mBorderThickness / 2,
                    mBorderInsideColor);
        } else if (mBorderInsideColor == defaultColor
                && mBorderOutsideColor != defaultColor) {// 定义画一个边框
            radius = (width < height ? width
                    : height) / 2 - mBorderThickness;
            drawCircleBorder(canvas, radius + mBorderThickness / 2,
                    mBorderOutsideColor);
        } else {// 没有边框
            radius = (width < height ? width
                    : height) / 2;
        }
        Bitmap roundBitmap = getCroppedRoundBitmap(bitmap, radius);
        canvas.drawBitmap(roundBitmap, width / 2 - radius, height
                / 2 - radius, null);

        paint.setTextSize(width/7);
        paint1.setTextSize(width/5);

        String text = "剩余流量";
        float textWidth = paint.measureText(text);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseLine = height / 2-(metrics.ascent + metrics.descent) - height/6;


        float textWidth1 = paint1.measureText(TEXT);
        Paint.FontMetrics metrics1 = paint1.getFontMetrics();
        float baseLine1 = height / 2-(metrics1.ascent + metrics1.descent) ;

        canvas.drawText(text,width / 2-textWidth/2,baseLine,paint);
        canvas.drawText(TEXT,width / 2-textWidth1/2,baseLine1,paint1);


       // handler.sendEmptyMessageDelayed(0,1000 * 60); //一分钟,因服务器压力暂缓设置

    }


    /**
     * 获取裁剪后的圆形图片
     * @param radius 半径
     */
    public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius) {
        Bitmap scaledSrcBmp;
        int diameter = radius * 2;

        // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图片
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        int squareWidth = 0, squareHeight = 0;
        int x = 0, y = 0;
        Bitmap squareBitmap;
        if (bmpHeight > bmpWidth) {// 高大于宽
            squareWidth = squareHeight = bmpWidth;
            x = 0;
            y = (bmpHeight - bmpWidth) / 2;
            // 截取正方形图片
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
                    squareHeight);
        } else if (bmpHeight < bmpWidth) {// 宽大于高
            squareWidth = squareHeight = bmpHeight;
            x = (bmpWidth - bmpHeight) / 2;
            y = 0;
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth,
                    squareHeight);
        } else {
            squareBitmap = bmp;
        }

        if (squareBitmap.getWidth() != diameter
                || squareBitmap.getHeight() != diameter) {
            scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter,
                    diameter, true);

        } else {
            scaledSrcBmp = squareBitmap;
        }
        Bitmap output = Bitmap.createBitmap(scaledSrcBmp.getWidth(),
                scaledSrcBmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, scaledSrcBmp.getWidth(),
                scaledSrcBmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(scaledSrcBmp.getWidth() / 2,
                scaledSrcBmp.getHeight() / 2, scaledSrcBmp.getWidth() / 2,
                paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledSrcBmp, rect, rect, paint);
        bmp = null;
        squareBitmap = null;
        scaledSrcBmp = null;
        return output;
    }

    /**
     * 边框
     */
    private void drawCircleBorder(Canvas canvas, int radius, int color) {
        Paint paint = new Paint();
        /* 去锯齿 */
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(color);
        /* 设置paint的　style　为STROKE：空心 */
        paint.setStyle(Paint.Style.STROKE);
        /* 设置paint的外框宽度 */
        paint.setStrokeWidth(mBorderThickness);
        canvas.drawCircle(width / 2, height / 2, radius, paint);
    }

    //view  销毁时调用
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (requestQueue!=null)
        requestQueue.cancelAll(TAG);
    }

    public void setFlowNumber(final Map<String, Object> map) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url ;
                if(GetUrl.URL_SAVE.isEmpty()&&GetUrl.URL_SAVE.equals("")){
                    if(map == null){
                        return;
                    }
                    url = Constant.FLOW_SERVICE + GetUrl.getSignUrl(map,Constant.SECRET);
                    GetUrl.URL_SAVE = url;
                }else {
                    url = GetUrl.URL_SAVE;
                }
              //  Log.i(TAG, "run: getUrl----2 " + url);
                StringRequest re = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.isEmpty()&&!response.equals("")){
                            //解析json
                           // Log.i(TAG, "run: getUrl----2 " + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String code = jsonObject.getString("code");
                                String desc = jsonObject.getString("desc");
                                if(code.equals("0000") ){
                                    Integer busiCode = jsonObject.getInt("busiCode");

                                    if(busiCode == 1){//查询余额数据成功
                                        String balance = jsonObject.getString("balance");
                                        TEXT = balance+" 元";
                                        invalidate();
                                    }
                                    if(busiCode == 2){//请求流量数据成功
                                        String balance = jsonObject.getString("balance");
                                        int index = balance.indexOf(".");
                                        String num;
                                        if(index!= -1){
                                            num = balance.substring(0,index);
                                            Log.d(TAG, "onCreate: num " + num);
                                        }else {
                                            num = balance;
                                        }
                                        /**
                                         * 保留小数点后两位
                                         * a 长度1-3， KB为单位43
                                         * b 长度4-6， MB为单位
                                         * c 长度>6,  GB为单位
                                         */
                                        int flow;
                                        try {
                                            if(num.length()<4 && num.length()>0){
                                                TEXT = balance+"KB";
                                            }else if(num.length() >= 4 && num.length() < 7){
                                                flow = Integer.parseInt(num);
                                                int i = flow/ 1000;
                                                int x = flow/100 - i*10;
                                                int y = flow / 10 - flow / 100 * 10;
                                                TEXT = i+"."+x+y+"MB";
                                            }else{
                                                flow = Integer.parseInt(num) ;
                                                int i = flow  / (1000 * 1000);
                                                int x = flow / (1000 * 100) - i*10;
                                                int y = flow / (1000 * 10) - flow/(1000*100) * 10;
                                                TEXT = i+"."+x+y+"GB";
                                            }
                                        }catch (NumberFormatException e){
                                            e.printStackTrace();
                                        }
                                        invalidate();
                                    }
                                }else{
                                    Toast.makeText(context,desc+":错误代码-"+code,Toast.LENGTH_SHORT).show();
                                    //数据拉取出错
                                    TEXT = "获取失败";
                                    invalidate();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        invalidate();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,"流量查询失败",Toast.LENGTH_SHORT).show();
                        TEXT = "获取失败";
                        invalidate();
                    }
                });
                re.setTag(TAG);
                requestQueue.add(re);

                //GET
//                OkHttpClient client = new OkHttpClient();
//                Request request = new Request.Builder().url(url).build();
//                Response response = null;
//                try {
//                    response = client.newCall(request).execute();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
//                if (response.isSuccessful()) {
//                    String json = "";
//                    try {
//                         json = response.body().string();
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    }
//                    if(!json.isEmpty()&&!json.equals("")){
//                        //解析json
//                        Log.i(TAG, "run: getUrl----2 " + response);
//                        try {
//                            JSONObject jsonObject = new JSONObject(json);
//                            String code = jsonObject.getString("code");
//                            String desc = jsonObject.getString("desc");
//                            if(code.equals("0000") ){
//                                Integer busiCode = jsonObject.getInt("busiCode");
//
//                                if(busiCode == 1){//查询余额数据成功
//                                    String balance = jsonObject.getString("balance");
//                                    TEXT = balance+" 元";
//                                }else if(busiCode == 2){//请求流量数据成功
//                                    String balance = jsonObject.getString("balance");
//                                    TEXT = balance+" KB";
//
//                                }
//
//                            }else{
//                                Toast.makeText(context,desc+":错误代码-"+code,Toast.LENGTH_SHORT).show();
//                                //数据拉取出错
//                                TEXT = "获取失败";
//                                invalidate();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    invalidate();
//                }else {
////                Toast.makeText(context,"流量查询失败",Toast.LENGTH_SHORT).show();
//                    TEXT = "获取失败";
//                    invalidate();
//                }
         }
     }).start();
    }
}
