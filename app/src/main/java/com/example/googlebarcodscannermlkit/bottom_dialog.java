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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.googlebarcodscannermlkit.cameraviewmodel.UiViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class bottom_dialog extends BottomSheetDialogFragment {

    private TextView title,link,btn_visit;
    private ImageView close;
    private String fetcurl;
    private UiViewModel uiViewModel;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.bottom_dialog,container,false);
        title=view.findViewById(R.id.text_title);
        link=view.findViewById(R.id.text_link);
        btn_visit=view.findViewById(R.id.visitlink);
        close=view.findViewById(R.id.close);
        link.setText(fetcurl);
        uiViewModel=new ViewModelProvider(requireActivity()).get(UiViewModel.class);
        final Observer<List<String>> observer=new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                title.setText(strings.get(0).toString());
                link.setText(strings.get(1).toString());
                fetchUrl(strings.get(1));

            }
        };
        uiViewModel.getListMutableLiveData().observe(requireActivity(),observer);




        btn_visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent=new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(fetcurl));
                    startActivity(intent);

                }catch (Exception ignored){
                    ignored.printStackTrace();

                }

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
                if (uri.startsWith("https://")||uri.startsWith("http://")) fetcurl=uri;
                else fetcurl="http://"+uri;



            }
        });

    }
}


