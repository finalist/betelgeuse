package com.gamaray.arex.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.gamaray.arex.R;
import com.gamaray.arex.eventbus.EventBus;
import com.gamaray.arex.eventbus.ReloadDimensionEvent;

public class WebviewDialog extends Dialog {
    
    private final WebView webView; 

    public WebviewDialog(Context context,String url) {
        super(context,R.style.webdialogtheme);
        this.setContentView(R.layout.webviewdialog);
        webView = (WebView) findViewById(R.id.webview);
        Button button = (Button) findViewById(R.id.closebutton);

        button.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                EventBus.get().fire(new ReloadDimensionEvent());
            }
            
        });
        webView.getSettings().setJavaScriptEnabled(false);
        loadUrl(url);
    }
    
    public void loadUrl(String url){
        webView.loadUrl(url);
    }
    
    

}
