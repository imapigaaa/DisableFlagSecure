package io.github.lsposed.disableflagsecure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class WatermarkView extends View {
    static final String TAG = "disableflagsecure_watermark";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final String text;

    WatermarkView(Context context, String text) {
        super(context);
        this.text = text == null ? "" : text;
        setTag(TAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        var now = format.format(new Date());
        var line = text.isEmpty() ? now : text + "  " + now;

        var dm = getResources().getDisplayMetrics();
        var density = dm.density;
        var textSize = 15 * density;

        paint.setTextSize(textSize);
        paint.setColor(Color.argb(36, 0, 0, 0));
        paint.setTypeface(Typeface.DEFAULT_BOLD);

        float w = getWidth();
        float h = getHeight();
        float lineWidth = paint.measureText(line);
        float stepX = Math.max(lineWidth * 1.5f, w / 2.2f);
        float stepY = Math.max(textSize * 5f, h / 7f);

        canvas.save();
        canvas.rotate(-25, w / 2f, h / 2f);
        float row = 0;
        for (float y = -h; y < h * 2; y += stepY) {
            float offset = (row % 2) * stepX / 2;
            for (float x = -w * 2 + offset; x < w * 3; x += stepX) {
                canvas.drawText(line, x, y, paint);
            }
            row++;
        }
        canvas.restore();

        // refresh every minute for the clock
        postInvalidateDelayed(60_000);
    }
}
