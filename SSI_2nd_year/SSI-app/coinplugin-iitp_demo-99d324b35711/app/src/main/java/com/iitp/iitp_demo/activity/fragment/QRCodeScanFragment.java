package com.iitp.iitp_demo.activity.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.activity.request.RequestPostVpActivity;
import com.iitp.iitp_demo.util.PrintLog;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.ViewfinderView;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;


/**
 * QR-CODE scan 화면
 */
public class QRCodeScanFragment extends Fragment{
    /**
     * 이미지 가져오기 요청 코드.
     */
    private final static int REQUEST_CODE_CAMERA_PERMISSION = 0x0103;
    private DecoratedBarcodeView barcodeView;
    /**
     * scanner
     */
    CaptureManager capture;
    private List<String> vcList;
    private String presentationRequestId = null;
    public static QRCodeScanFragment newInstance(){
        Bundle args = new Bundle();
        QRCodeScanFragment fragment = new QRCodeScanFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
        }
    }

    @Override
    public void onResume(){
        PrintLog.e("barcode view resume");
        if(barcodeView != null){
            barcodeView.resume();
        }
        super.onResume();

    }

    @Override
    public void onPause(){
        PrintLog.e("barcode view pause");
        if(barcodeView != null){
            barcodeView.pause();
        }
        super.onPause();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_qrcode_scan, container, false);
        barcodeView = view.findViewById(R.id.zxing_barcode_scanner);
        openCameraWithPermission();
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Arrays.asList(BarcodeFormat.QR_CODE)));
        barcodeView.decodeContinuous(callback);
        ViewfinderView viewFinder = barcodeView.getViewFinder();
        try{
            Field scannerAlphaField = viewFinder.getClass().getDeclaredField("SCANNER_ALPHA");
            scannerAlphaField.setAccessible(true);
            scannerAlphaField.set(viewFinder, new int[1]);
        }catch(Exception e){
            PrintLog.e("onCreateView error");
            // not found SCANNER_ALPHA member variable
        }
//        capture = new CaptureManager(getActivity(), barcodeView);
//        capture.initializeFromIntent(integrator.createScanIntent(), savedInstanceState);
//        capture.decode();

        return view;
    }

    /**
     * qrcode call back
     */
    private BarcodeCallback callback = new BarcodeCallback(){
        @Override
        public void barcodeResult(BarcodeResult result){
            if(result.getText() != null){
                String resutl = result.getText();
                PrintLog.e("text = " + resutl);
                if(resutl.contains(".") ||resutl.contains("{") ){
                    Intent intent = new Intent(getContext(), RequestPostVpActivity.class);
                    intent.putExtra("jwt",resutl);
                    startActivity(intent);
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints){
        }
    };

    private void openCameraWithPermission(){
        int result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if(result != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
        }else{
            barcodeView.resume();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode == REQUEST_CODE_CAMERA_PERMISSION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // permission was granted
                barcodeView.resume();
            }
        }
    }
}
