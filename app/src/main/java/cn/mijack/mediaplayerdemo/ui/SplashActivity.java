package cn.mijack.mediaplayerdemo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import cn.mijack.mediaplayerdemo.R;
import cn.mijack.mediaplayerdemo.base.BaseActivity;

/**
 * @author Mr.Yuan
 * @date 2017/6/20
 */
public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }, 2000);
    }
}
