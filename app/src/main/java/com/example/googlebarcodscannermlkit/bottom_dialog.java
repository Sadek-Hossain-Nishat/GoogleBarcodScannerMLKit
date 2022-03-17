package com.example.googlebarcodscannermlkit;

import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class bottom_dialog extends BottomSheetDialogFragment {

    private TextView title,link,btn_visit;
    private ImageView close;
    private String fetcurl;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.bottom_dialog,container,false);
        title=view.findViewById(R.id.text_title);
        link=view.findViewById(R.id.text_link);
        btn_visit=view.findViewById(R.id.visitlink);
        close=view.findViewById(R.id.close);
        title.setText(fetcurl);
        btn_visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(fetcurl));
                startActivity(intent);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dismiss();

            }
        });
        return view;
    }

    public void fetchUrl(String uri){

        ExecutorService executorService= Executors.newSingleThreadExecutor();
        Handler handler=new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                fetcurl=uri;



            }
        });

    }
}


