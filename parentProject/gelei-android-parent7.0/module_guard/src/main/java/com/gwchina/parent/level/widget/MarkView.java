package com.gwchina.parent.level.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.base.utils.android.UnitConverter;
import com.gwchina.lssw.parent.guard.R;

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-30 11:12
 */
public class MarkView extends LinearLayout {

    public MarkView(Context context) {
        this(context, null, 0);
    }

    public MarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.widget_text_round_mark, this);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarkView);
        String text = typedArray.getString(R.styleable.MarkView_mk_text);
        int textColor = typedArray.getColor(R.styleable.MarkView_mk_text_color, Color.WHITE);
        int backgroundColor = typedArray.getColor(R.styleable.MarkView_mk_color, context.getResources().getColor(R.color.green_main));
        float cornerRadius = typedArray.getDimension(R.styleable.MarkView_mk_corner_radius, UnitConverter.dpToPx(15));
        typedArray.recycle();

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(cornerRadius);
        gradientDrawable.setColor(backgroundColor);

        TextView tvMarkText = findViewById(R.id.tvMarkText);
        View vLineLeft = findViewById(R.id.vLineLeft);
        View vLineRight = findViewById(R.id.vLineRight);
        tvMarkText.setText(text);
        tvMarkText.setTextColor(textColor);
        tvMarkText.setBackground(gradientDrawable);
        vLineLeft.setBackgroundColor(backgroundColor);
        vLineRight.setBackgroundColor(backgroundColor);
    }

}
