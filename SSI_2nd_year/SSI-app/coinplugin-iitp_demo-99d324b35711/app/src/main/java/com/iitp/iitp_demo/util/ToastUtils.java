package com.iitp.iitp_demo.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;

import com.iitp.iitp_demo.R;


/**
 * toast utils
 */
public class ToastUtils{
    /**
     * 기본 Toast TextView 의 배경색/글자색/글자크기/padding 을 변경한다.
     * @param toast 변경할 Toast
     * @return 변경된 Toast
     */
    public static Toast custom(Toast toast) {
        View toastView = toast.getView();

        toastView.setBackgroundResource(R.drawable.toast_background);

        TextView textView = toastView.findViewById(android.R.id.message);
        String strColor = "#666666";
        textView.setBackgroundColor( Color.parseColor(strColor));
        textView.setTextColor(ResourcesCompat.getColor(toastView.getContext().getResources(), R.color.toast_text, null));
        textView.setPadding((int) ViewUtils.dp2px(toastView.getContext(), 16), 0, (int) ViewUtils.dp2px(toastView.getContext(), 16), 0);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        return toast;
    }


    public static void show(Context context, String message) {
        custom(Toast.makeText(context, message, Toast.LENGTH_SHORT)).show();
    }

    public static void show(Context context, @StringRes int messageId) {
        show(context, context.getString(messageId));
    }
}
