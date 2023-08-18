package com.example.cleanarchitech_text_0506.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.cleanarchitech_text_0506.R;

import java.util.ArrayList;

public class MtouchInstallmentDialog extends Dialog {

    public interface OnListClickListener {
        void onItemClick(String index);
    }

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private TextView titleTextView;
    private Button mPositiveButton;

    private OnListClickListener onListClickListener;
    private int maxInstallment = 1;

    private String titleText = null;
    private String positiveText = null;

    public MtouchInstallmentDialog(@NonNull Context context, OnListClickListener onListClickListener) {
        super(context);
        this.onListClickListener = onListClickListener;
        setCancelable(true);
    }

    public MtouchInstallmentDialog(@NonNull Context context, OnListClickListener onListClickListener, boolean isCancelable) {
        super(context);
        this.onListClickListener = onListClickListener;
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

        setContentView(R.layout.mtouch_dialog_list_layout);

        recyclerViewAdapter = new RecyclerViewAdapter();
        recyclerViewAdapter.addItem(new InstallData().setChecked(true).setIndexString("0"));
        for (int i = 2; i < maxInstallment+1; i++) {
            recyclerViewAdapter.addItem(new InstallData().setChecked(false).setIndexString(i+""));
        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setAdapter(recyclerViewAdapter);


        //셋팅
        mPositiveButton = (Button) findViewById(R.id.confirmButton);
        titleTextView = findViewById(R.id.titleTextView);

        if (positiveText != null && positiveText.length() > 0)
            mPositiveButton.setText(positiveText);
        if (titleText != null && titleText.length() > 0)
            titleTextView.setText(titleText);


        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onListClickListener != null)
                    onListClickListener.onItemClick(recyclerViewAdapter.getIndexString());
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(v->{
            dismiss();
        });

    }

    public MtouchInstallmentDialog setTitleText(String titleText) {
        this.titleText = titleText;
        return this;
    }

    public MtouchInstallmentDialog setPositiveButtonText(String text) {
        positiveText = text;
        return this;
    }

    public MtouchInstallmentDialog setMaxInstallment(int installment) {
        this.maxInstallment = installment;
        return this;
    }

    class InstallData {
        private boolean isChecked = false;
        private String indexString = "0";

        public boolean isChecked() {
            return isChecked;
        }

        public InstallData setChecked(boolean checked) {
            isChecked = checked;
            return this;
        }

        public String getIndexString() {
            return indexString;
        }

        public InstallData setIndexString(String indexString) {
            this.indexString = indexString;
            return this;
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MtouchDialogViewHolder> {
        private ArrayList<InstallData> list = new ArrayList<>();

        public void addItem(InstallData installData) {
            list.add(installData);
        }

        @NonNull
        @Override
        public MtouchDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MtouchDialogViewHolder(View.inflate(parent.getContext(), R.layout.item_installment_layout, null));
        }

        @Override
        public void onBindViewHolder(@NonNull MtouchDialogViewHolder holder, int position) {
            InstallData item = list.get(position);
            if (item.getIndexString().equals("0")) {
                holder.installmentTextView.setText("일시불");
            } else {
                holder.installmentTextView.setText(item.getIndexString() + "개월");
            }

            holder.radioButton.setChecked(item.isChecked);
            holder.itemView.setTag(item);

        }

        @Override
        public int getItemCount() {
            if (list == null) {
                return 0;
            } else {
                return list.size();
            }
        }

        public String getIndexString() {
            for (InstallData item : list) {
                if (item.isChecked) {
                    return item.getIndexString();
                }
            }
            return "0";
        }
        private void resetData(InstallData installData){
            for(InstallData item :list){
                if(installData!=null && installData == item){
                    item.setChecked(true);
                }else {
                    item.setChecked(false);
                }
            }
            notifyDataSetChanged();
        }

        class MtouchDialogViewHolder extends RecyclerView.ViewHolder {

            private RadioButton radioButton;
            private TextView installmentTextView;

            public MtouchDialogViewHolder(@NonNull View itemView) {
                super(itemView);

                radioButton = itemView.findViewById(R.id.radioButton);
                installmentTextView = itemView.findViewById(R.id.installmentTextView);

                itemView.setOnClickListener(v->{
                    InstallData item = (InstallData) v.getTag();
                    resetData(item);
                });
            }
        }
    }
}
