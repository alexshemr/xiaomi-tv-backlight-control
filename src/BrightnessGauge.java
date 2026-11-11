package local.ha.tvbacklight;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.view.View;

/** A compact system-style vertical backlight indicator. */
public final class BrightnessGauge extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int level;
    public BrightnessGauge(Context context) { super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); }
    public void setLevel(int value) { level = value; invalidate(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float d = getResources().getDisplayMetrics().density;
        float width = getWidth(), height = getHeight();
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
        int ink = dark ? 0xfff7f3e9 : 0xff29323c;
        float cx = width / 2, cy = 24 * d;
        paint.setColor(ink);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.3f * d);
        canvas.drawCircle(cx, cy, 3.5f * d, paint);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            canvas.drawLine(cx + (float)Math.cos(angle)*6*d, cy + (float)Math.sin(angle)*6*d,
                cx + (float)Math.cos(angle)*9*d, cy + (float)Math.sin(angle)*9*d, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        RectF track = new RectF(cx - 2*d, 40*d, cx + 2*d, height - 28*d);
        paint.setColor(dark ? 0xff41474e : 0xffdce1e5);
        canvas.drawRoundRect(track, 2*d, 2*d, paint);
        canvas.save();
        Path clip = new Path();
        clip.addRoundRect(track, 2*d, 2*d, Path.Direction.CW);
        canvas.clipPath(clip);
        paint.setColor(0xff03a9f4);
        canvas.drawRect(track.left, track.bottom - track.height()*level/100f, track.right, track.bottom, paint);
        canvas.restore();
        paint.setColor(ink);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        paint.setTextSize(13*d);
        canvas.drawText(level + "%", cx, height - 12*d, paint);
    }
}
