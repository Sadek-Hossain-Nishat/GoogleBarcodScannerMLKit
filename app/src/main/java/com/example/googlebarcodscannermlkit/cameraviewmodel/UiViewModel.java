package com.example.googlebarcodscannermlkit.cameraviewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UiViewModel extends AndroidViewModel {
    private MutableLiveData<List<String>> listMutableLiveData;

    public UiViewModel(@NonNull Application application) {
        super(application);
    }


    public MutableLiveData<List<String>> getListMutableLiveData() {
        if (listMutableLiveData==null){
            listMutableLiveData=new MutableLiveData<>();
        }
        return listMutableLiveData;
    }
}
