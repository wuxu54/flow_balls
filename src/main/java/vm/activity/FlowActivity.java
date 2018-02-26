package vm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import utils.GetUrl;
import vm.base.BaseActivity;
import vm.viewManager.FlowBallManager;

/**
 * Created by SpongeBob on 2017/9/20.
 */
public class FlowActivity extends BaseActivity implements View.OnClickListener {
    private WebView webView;
    private ProgressBar webLoad;
    private FlowBallManager flowBallManager;
    private LinearLayout rl_goBack;
    private RelativeLayout ll_more;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = findViewById(R.id.flowWebView);
        webLoad = findViewById(R.id.web_load);
        ll_more = findViewById(R.id.ic_more);
        rl_goBack = findViewById(R.id.rl_goBack);
        init();
    }

    private void init() {
        flowBallManager = FlowBallManager.getInstance(FlowActivity.this, getApplication());
        flowBallManager.hideFlowBall();
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//设置无缓存模式

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //起调微信。要补充支付宝
                // 获取上下文, H5PayDemoActivity为当前页面
                if (url!=null && !url.equals("") && !url.startsWith("weixin") &&!url.startsWith("alipay")) {
                    return super.shouldOverrideUrlLoading(webView, url);
                }
                final Activity context = FlowActivity.this;
                if (url.startsWith("weixin")) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context,"未检测到微信客户端，请安装后重试。",Toast.LENGTH_SHORT).show();
                        webView.loadUrl(GetUrl.PAYURL_SAVE); // 回到首页
                        webView.clearHistory(); // 清除
                    }
                    return true;
                } else if (url.startsWith("alipays:") || url.startsWith("alipay")) {
                    try {
                        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                    } catch (Exception e) {
                        new AlertDialog.Builder(context)
                                .setMessage("未检测到支付宝客户端，请安装后重试。")
                                .setPositiveButton("立即安装", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Uri alipayUrl = Uri.parse("https://d.alipay.com");
                                        context.startActivity(new Intent("android.intent.action.VIEW", alipayUrl));
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                webView.loadUrl(GetUrl.PAYURL_SAVE); // 回到首页
                                webView.clearHistory(); // 清除
                            }
                        }).show();
                    }
                    return true;
                }
                // ------- 处理结束 -------
                if (!(url.startsWith("http") || url.startsWith("https"))) {
                    return true;
                }
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webLoad.setProgress(newProgress);
                if (newProgress == 100) {
                    //网页加载完成
                    showProgress(false);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        //流量直充界面
        webView.loadUrl(GetUrl.PAYURL_SAVE);


        ll_more.setOnClickListener(this);
        rl_goBack.setOnClickListener(this);
    }

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.rl_goBack) {
            finish();

        } else if (i == R.id.ic_more) {
            Log.d("FLOW", "onClick: 点击more");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        flowBallManager.showFlowBall();
        flowBallManager.setFlowNumberParams(null);
        //清除缓存数据
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
    }

    /**
     * 复写退出事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();//webView 返回上一个网页
                return true;
            } else {
                finish();//退出本Activity
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param isShow true 表示展示progress
     */
    private void showProgress(boolean isShow) {
        webView.setVisibility(!isShow ? View.VISIBLE : View.GONE);
        webLoad.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
