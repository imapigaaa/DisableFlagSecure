package io.github.lsposed.disableflagsecure;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

public class MainActivity extends Activity {
    private static final String PREFS_GROUP = "watermark";

    private volatile XposedService service;
    private Switch enableSwitch;
    private EditText contentInput;
    private TextView statusView;

    private final XposedServiceHelper.OnServiceListener serviceListener = new XposedServiceHelper.OnServiceListener() {
        @Override
        public void onServiceBind(@NonNull XposedService s) {
            service = s;
            runOnUiThread(MainActivity.this::loadConfig);
        }

        @Override
        public void onServiceDied(@NonNull XposedService s) {
            service = null;
            runOnUiThread(() -> statusView.setText(R.string.wm_no_service));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var dm = getResources().getDisplayMetrics();
        var density = dm.density;
        int pad = (int) (20 * density);

        var root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        setContentView(root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        var title = new TextView(this);
        title.setText(R.string.wm_settings_title);
        title.setTextSize(20);
        title.setPadding(0, 0, 0, (int) (4 * density));
        root.addView(title);

        statusView = new TextView(this);
        statusView.setText(R.string.wm_connecting);
        statusView.setTextSize(13);
        statusView.setPadding(0, 0, 0, (int) (16 * density));
        root.addView(statusView);

        var hint = new TextView(this);
        hint.setText(R.string.wm_scope_hint);
        hint.setTextSize(13);
        hint.setPadding(0, 0, 0, (int) (16 * density));
        root.addView(hint);

        enableSwitch = new Switch(this);
        enableSwitch.setText(R.string.wm_enable);
        enableSwitch.setTextSize(16);
        enableSwitch.setPadding(0, 0, 0, (int) (12 * density));
        root.addView(enableSwitch);

        contentInput = new EditText(this);
        contentInput.setHint(R.string.wm_content_hint);
        contentInput.setSingleLine(false);
        contentInput.setMinLines(2);
        contentInput.setMaxLines(4);
        root.addView(contentInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        var save = new Button(this);
        save.setText(R.string.wm_save);
        save.setOnClickListener(v -> save());
        root.addView(save, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        XposedServiceHelper.registerListener(serviceListener);
    }

    private void loadConfig() {
        var s = service;
        if (s == null) return;
        try {
            var prefs = s.getRemotePreferences(PREFS_GROUP);
            enableSwitch.setChecked(prefs.getBoolean("enabled", false));
            contentInput.setText(prefs.getString("text", ""));
            statusView.setText(R.string.wm_connected);
        } catch (Throwable t) {
            statusView.setText(R.string.wm_no_service);
        }
    }

    private void save() {
        var s = service;
        if (s == null) {
            Toast.makeText(this, R.string.wm_no_service, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            var prefs = s.getRemotePreferences(PREFS_GROUP);
            prefs.edit()
                    .putBoolean("enabled", enableSwitch.isChecked())
                    .putString("text", contentInput.getText().toString())
                    .apply();
            Toast.makeText(this, R.string.wm_saved, Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, R.string.wm_no_service, Toast.LENGTH_SHORT).show();
        }
    }
}
