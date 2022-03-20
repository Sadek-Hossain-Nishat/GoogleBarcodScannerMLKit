package com.example.googlebarcodscannermlkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;

import com.example.googlebarcodscannermlkit.cameraviewmodel.UiViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    // creating some objects of some classe
    private ListenableFuture cameraProviderFuture; // for  asynchronous computation
    private ExecutorService cameraExecutor; //to manage the future asynchronous computation
    private PreviewView previewView;
    private MyImageAnalyzer analyzer;
    private UiViewModel uiViewModel;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        previewView = findViewById(R.id.priviewviewid);
        this.getWindow().setFlags(1024, 1024); // setting the screen for proper resolution
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this); // for attaching camerax lifecycle properties
        // with application running process

        //initialize my custom ViewModel class
        uiViewModel= new ViewModelProvider(this).get(UiViewModel.class);








        analyzer=new MyImageAnalyzer(getSupportFragmentManager());


        // for  asynchronous computation
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // run time permission check
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!=(PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                                Manifest.permission.CAMERA
                        }, 101);

                    }
                    else {
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindpreview(processCameraProvider);

                    }


                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(this));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0) {

            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            bindpreview(processCameraProvider);

        }

    }



    private void bindpreview(ProcessCameraProvider processCameraProvider) {

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        processCameraProvider.unbindAll();
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis=new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1240,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor,analyzer);
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);


    }

    //for analysizing the image

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private FragmentManager fragmentManager;
        private bottom_dialog bd; //bottomsheet dialog object


        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            bd = new bottom_dialog();
        }


        @Override
        public void analyze(@NonNull ImageProxy image) {
            scanbarcode(image);
        }


        // for the scanning the barcode image


        private void scanbarcode(ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError") Image image1 = image.getImage();
            assert image1 != null;
            InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                    Barcode.FORMAT_ALL_FORMATS
                            )
                            .build();

            BarcodeScanner scanner = BarcodeScanning.getClient(options);


            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {


                            renderBarcodeData(barcodes);
                            // Task completed successfully
                            // ...
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });




        }

        //results after scanning the barcode

        private void renderBarcodeData(List<Barcode> barcodes) {
            Snackbar snackbar=null;
            List<String> list=null;


            for (Barcode barcode : barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                String rawValue = barcode.getRawValue();

                int valueType = barcode.getValueType();
                // See API reference for complete list of supported types
                switch (valueType) {


                    case Barcode.TYPE_WIFI:


                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;



                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }

                        String ssid = barcode.getWifi().getSsid();
                        String password = barcode.getWifi().getPassword();
                        list=new ArrayList<>();
                        list.add(ssid);
                        list.add(password);
                        uiViewModel.getListMutableLiveData().setValue(list);
                        break;
                    case Barcode.TYPE_URL:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;





                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }



//                        bd.fetchUrl(Objects.requireNonNull(barcode.getUrl()).getUrl());

                        String title = barcode.getUrl().getTitle();
                        String url = barcode.getUrl().getUrl();
                        list=new ArrayList<>();
                        list.add(title);
                        list.add(url);
                        uiViewModel.getListMutableLiveData().setValue(list);
                        break;
                    case Barcode.TYPE_CALENDAR_EVENT:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;



                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }



                        String calenderevent = "Calender Event";
                        String summary = barcode.getRawValue().toString();
                        list=new ArrayList<>();
                        list.add(calenderevent);
                        list.add(summary);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    case Barcode.TYPE_CONTACT_INFO:


                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;



                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titlecontact = "Contact Info";
                        String contactvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titlecontact);
                        list.add(contactvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    case Barcode.TYPE_DRIVER_LICENSE:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;


                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titledriveer = "Driver License";
                        String licensevalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titledriveer);
                        list.add(licensevalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    case Barcode.TYPE_EMAIL:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;





                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titleemail = "Email";
                        String emaildata =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titleemail);
                        list.add(emaildata);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    case Barcode.TYPE_GEO:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;



                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titlegeo = "Geo";
                        String geovalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titlegeo);
                        list.add(geovalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    case Barcode.TYPE_ISBN:


                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;




                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }

                        String titleisbn = "ISBN";
                        String isbnvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titleisbn);
                        list.add(isbnvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;


                    case Barcode.TYPE_PHONE:

                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;




                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titlephone = "Phone";
                        String phonevalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titlephone);
                        list.add(phonevalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;


                    case Barcode.TYPE_PRODUCT:


                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;




                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titleproduct = "Product";
                        String productvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titleproduct);
                        list.add(productvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);
                        break;

                    case Barcode.TYPE_SMS:

                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;





                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        String titlesms = "Sms";
                        String smsvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titlesms);
                        list.add(smsvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;


                    case Barcode.TYPE_TEXT:


                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;



                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }



                        String titletext = "Text";
                        String textvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titletext);
                        list.add(textvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;


                    case Barcode.TYPE_UNKNOWN:



                            try {

                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;


                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }



                        String titleunknown = "Unknown";
                        String unknownvalue =barcode.getRawValue().toString() ;
                        list=new ArrayList<>();
                        list.add(titleunknown);
                        list.add(unknownvalue);
                        uiViewModel.getListMutableLiveData().setValue(list);

                        break;

                    default:


                            try {
                                if (!bd.isAdded()){
                                    bd.show(fragmentManager,"");

                                }
                                else  return;




                            }catch (IllegalStateException e) {
                                e.printStackTrace();
                            }


                        list=new ArrayList<>();
                        list.add("Not Recognized");
                        list.add("Not Recognized");
                        uiViewModel.getListMutableLiveData().setValue(list);
                        break;

                }
            }


        }
    }
}