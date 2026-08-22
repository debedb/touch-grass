package com.debedb.touchgrass;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

/**
 * Single screen with two states: idle (TOUCH GRASS + photo) and response.
 * A touch swaps to the response state, which reverts on its own after
 * reset_delay_ms. Touches during the response state are ignored so the
 * timer is never extended or restarted.
 */
public class MainActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable resetToIdle = this::showIdle;

    private View idleView;
    private View responseView;
    private long resetDelayMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        idleView = findViewById(R.id.idle);
        responseView = findViewById(R.id.response);
        resetDelayMs = getResources().getInteger(R.integer.reset_delay_ms);

        findViewById(R.id.root).setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                onScreenTouched();
            }
            return true;
        });

        showIdle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Any restart, wake or recovery lands on the idle state.
        showIdle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(resetToIdle);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemBars();
        }
    }

    private void onScreenTouched() {
        if (responseView.getVisibility() == View.VISIBLE) {
            return;
        }
        idleView.setVisibility(View.GONE);
        responseView.setVisibility(View.VISIBLE);
        handler.postDelayed(resetToIdle, resetDelayMs);
    }

    private void showIdle() {
        handler.removeCallbacks(resetToIdle);
        responseView.setVisibility(View.GONE);
        idleView.setVisibility(View.VISIBLE);
    }

    private void hideSystemBars() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller == null) {
            return;
        }
        controller.hide(WindowInsets.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
}
