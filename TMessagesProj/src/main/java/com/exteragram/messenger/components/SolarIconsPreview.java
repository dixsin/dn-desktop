package com.exteragram.messenger.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.exteragram.messenger.ExteraConfig;
import com.exteragram.messenger.ExteraUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Easings;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class SolarIconsPreview extends FrameLayout {

    private final int ICON_WIDTH = AndroidUtilities.dp(28);

    private final FrameLayout preview;

    private final RectF rect = new RectF();
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final ValueAnimator[] animator = new ValueAnimator[6];

    private static final int[] iconsRes = new int[] {
            R.drawable.msg_sticker,
            R.drawable.msg_link2,
            R.drawable.msg_voicechat,
            R.drawable.msg_pin,
            R.drawable.msg_photos,
            R.drawable.msg_folder
    };

    private static final Drawable[] icons = new Drawable[iconsRes.length];
    private final float[] iconChangingProgress = new float[] {
            1f, 1f, 1f, 1f, 1f, 1f
    };

    public SolarIconsPreview(Context context) {
        super(context);
        setWillNotDraw(false);

        setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(15), AndroidUtilities.dp(21), AndroidUtilities.dp(21));
        preview = new FrameLayout(context) {
            @SuppressLint("DrawAllocation")
            @Override
            protected void onDraw(Canvas canvas) {
                int color = Theme.getColor(Theme.key_switchTrack);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float w = getMeasuredWidth();
                float h = getMeasuredHeight();

                for (int i = 0; i < iconsRes.length; i++) {
                    icons[i] = ContextCompat.getDrawable(context, iconsRes[i]);
                    icons[i].setColorFilter(new PorterDuffColorFilter(ColorUtils.blendARGB(0x00, Theme.getColor(Theme.key_chats_menuItemIcon), iconChangingProgress[i]), PorterDuff.Mode.SRC_IN));
                }

                outlinePaint.setStyle(Paint.Style.STROKE);
                outlinePaint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_switchTrack), 0x3F));
                outlinePaint.setStrokeWidth(Math.max(2, AndroidUtilities.dp(0.5f)));

                rect.set(0, 0, w, h);
                Theme.dialogs_onlineCirclePaint.setColor(Color.argb(20, r, g, b));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), Theme.dialogs_onlineCirclePaint);

                float stroke = outlinePaint.getStrokeWidth() - Math.max(1, AndroidUtilities.dp(0.25f));
                rect.set(stroke, stroke, w - stroke, h - stroke);
                canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), outlinePaint);

                float allIconsWidth = ICON_WIDTH * icons.length;
                float distance = (w - allIconsWidth) / (icons.length + 1);
                for (int i = 0; i < icons.length; i++) {
                    int startX = (int) (distance * (i + 1) + ICON_WIDTH * i);
                    int startY = (int) (h / 2 - ICON_WIDTH / 2);
                    Drawable icon = icons[i];
                    icon.setBounds((int) ((startX + (ICON_WIDTH / 2)) - (ICON_WIDTH / 2 - AndroidUtilities.dp(3) + AndroidUtilities.dp(3) * iconChangingProgress[i])), startY, (int) ((startX + (ICON_WIDTH / 2)) + (ICON_WIDTH / 2 - AndroidUtilities.dp(3) + AndroidUtilities.dp(3) * iconChangingProgress[i])), startY + ICON_WIDTH);
                    icon.draw(canvas);
                }
            }
        };
        preview.setWillNotDraw(false);
        addView(preview, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        updateIcons(false);
    }

    @SuppressLint("Recycle")
    public void updateIcons(boolean animate) {
        for (int i = 0; i < icons.length; i++) {
            if (animate) {
                animator[i] = ValueAnimator.ofFloat(1f, 0f).setDuration(250);
                animator[i].setStartDelay(15L * i);
                animator[i].setInterpolator(Easings.easeInOutQuad);
                int finalI = i;
                animator[i].addUpdateListener(animation -> {
                    iconChangingProgress[finalI] = (Float) animation.getAnimatedValue();
                    invalidate();
                });
                animator[i].addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (finalI == 5) {
                            ExteraConfig.editor.putBoolean("useSolarIcons", ExteraConfig.useSolarIcons ^= true).apply();
                            reloadResources();
                        }
                        animator[finalI].setFloatValues(0f, 1f);
                        animator[finalI].removeAllListeners();
                        animator[finalI].start();
                    }
                });
                animator[i].start();
            } else {
                iconChangingProgress[i] = 1f;
                invalidate();
            }
        }
    }

    protected void reloadResources() {
    }

    @Override
    public void invalidate() {
        super.invalidate();
        preview.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!ExteraConfig.disableDividers)
            canvas.drawLine(AndroidUtilities.dp(21), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(110), MeasureSpec.EXACTLY));
    }
}
