package com.example.cleanarchitech_text_0506.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.cleanarchitech_text_0506.R;


public class MtouchDialog extends Dialog {

    private Activity activity;

    private TextView titleTextView;
    private TextView contentTextView;

    private Button mPositiveButton;
    private Button mNegativeButton;

    private View.OnClickListener mPositiveListener;
    private View.OnClickListener mNegativeListener;

    private String titleText = null;
    private String contentText = null;
    private String positiveText = null;
    private String negativeText = null;

    public MtouchDialog(@NonNull Context context) {
        super(context);
        activity = (Activity) context;
    }

    public MtouchDialog(Context context, boolean isCancelable){
        super(context);
        activity = (Activity) context;
        setCancelable(isCancelable);
    }

    public MtouchDialog(@NonNull Context context, View.OnClickListener positiveListener) {
        super(context);
        activity = (Activity) context;
        this.mPositiveListener = positiveListener;
        this.mNegativeListener = null;
        setCancelable(true);
    }

    public MtouchDialog(@NonNull Context context, View.OnClickListener positiveListener, boolean isCancelable) {
        super(context);
        activity = (Activity) context;
        this.mPositiveListener = positiveListener;
        this.mNegativeListener = null;
        setCancelable(isCancelable);
    }

    public MtouchDialog(@NonNull Context context, View.OnClickListener positiveListener, View.OnClickListener negativeListener) {
        super(context);
        activity = (Activity) context;
        this.mPositiveListener = positiveListener;
        this.mNegativeListener = negativeListener;
        setCancelable(true);
    }
    public MtouchDialog(@NonNull Context context, View.OnClickListener positiveListener, View.OnClickListener negativeListener, boolean isCancelable) {
        super(context);
        activity = (Activity) context;
        this.mPositiveListener = positiveListener;
        this.mNegativeListener = negativeListener;
        setCancelable(isCancelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.mtouch_dialog_layout);

        //셋팅
        mPositiveButton=(Button)findViewById(R.id.confirmButton);
        mNegativeButton=(Button)findViewById(R.id.cancelButton);
        titleTextView = findViewById(R.id.titleTextView);
        contentTextView = findViewById(R.id.contentTextView);

        if(positiveText!=null && positiveText.length()>0)
            mPositiveButton.setText(positiveText);
        if(negativeText!=null && negativeText.length()>0)
            mNegativeButton.setText(negativeText);
        if(titleText!=null && titleText.length()>0)
            titleTextView.setText(titleText);
        if(contentText!=null && contentText.length()>0)
            contentTextView.setText(contentText);


        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dismiss();
               if(mPositiveListener!=null) mPositiveListener.onClick(v);
            }
        });
        if(mNegativeListener!=null) {
            mNegativeButton.setVisibility(View.VISIBLE);
            mNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if(mNegativeListener!=null) mNegativeListener.onClick(v);
                }
            });
        }else{
            mNegativeButton.setVisibility(View.GONE);
        }

    }

    public MtouchDialog setTitleText(String titleText) {
        this.titleText =titleText;
        return this;
    }

    public MtouchDialog setContentText(String contentText) {
        this.contentText =contentText;
        return this;
    }
    public MtouchDialog setPositiveButtonText(String text){
        positiveText = text;
        return this;
    }
    public MtouchDialog setNegativeButtonText(String text){
        negativeText = text;
        return this;
    }

    @Override
    public void show() {
        if(!activity.isFinishing()) super.show();
    }
}
