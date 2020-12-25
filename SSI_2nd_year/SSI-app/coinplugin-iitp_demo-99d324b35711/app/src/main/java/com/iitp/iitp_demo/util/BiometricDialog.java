package com.iitp.iitp_demo.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;

import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.databinding.DialogAlertBioBinding;


public class BiometricDialog extends Dialog implements View.OnClickListener, DialogInterface{
    private DialogAlertBioBinding binding;

    private CancellationSignal cancellationSignal = new CancellationSignal();
    private int errorCount = 0;
    private OnClickListener positiveButtonListener;
    private OnClickListener negativeButtonListener;
    private OnAuthenticationListener listener;
    private Context context;


    public BiometricDialog(@NonNull Context context, String title, String description, OnAuthenticationListener listener, int type){
        super(context);

        binding = DialogAlertBioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        this.context= context;
        binding.titleTextView.setText(title);
        binding.messageTextView.setText(description);
        if(type == 0){
            binding.pinBtn.setOnClickListener(v -> {
                PrintLog.e("posion = " + 1);
                negativeButtonListener.onClick(this, BUTTON_NEGATIVE);
            });
            binding.cancelBtn.setOnClickListener(v -> {
                positiveButtonListener.onClick(this, BUTTON_POSITIVE);
                PrintLog.e("posion = " + 2);
            });
        }else if(type == 1){
            binding.oneBtn.setVisibility(View.VISIBLE);
            binding.twoBtn.setVisibility(View.GONE);
            binding.cancel1.setText(R.string.cancel);
            binding.cancel1.setOnClickListener(v -> cancel());

        }
        this.listener = listener;
    }

    @Override
    public void onClick(View v){
        int position = (int) v.getTag();
        if(position == BUTTON_NEGATIVE && negativeButtonListener != null){
            PrintLog.e("posion = " + 1);
            negativeButtonListener.onClick(this, BUTTON_NEGATIVE);
        }else if(position == BUTTON_POSITIVE && positiveButtonListener != null){
            positiveButtonListener.onClick(this, BUTTON_POSITIVE);
            PrintLog.e("posion = " + 2);
        }
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();

        if(!cancellationSignal.isCanceled()){
            cancellationSignal.cancel();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        authenticate();
    }

    @Override
    public void cancel(){
        super.cancel();
        cancellationSignal.cancel();
    }

    @SuppressWarnings("deprecation")
    private void authenticate(){
        FingerprintManager fingerprintManager = (FingerprintManager) getContext().getSystemService(Context.FINGERPRINT_SERVICE);
        if(fingerprintManager == null){
            dismiss();
            return;
        }

//        FingerprintManager.CryptoObject cryptoObject = KeyStoreUtils.getCryptoObjectCipher(KEYSTORE_ALIAS_BIO_ENCRYPT, Cipher.DECRYPT_MODE);
        try{
            fingerprintManager.authenticate(null
                    , cancellationSignal
                    , 0
                    , new FingerprintManager.AuthenticationCallback(){
                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString){
                            AlertDialog alertDialog = null;
                            if(!cancellationSignal.isCanceled()){
                                if(errorCode == 10){//s10 에러 처리
                                    cancellationSignal.cancel();
                                }else{
                                    if(errorCode == 7){
                                        alertDialog = dialogShow(errString.toString());
                                    }else if(errorCode == 9){
                                        alertDialog = dialogShow(errString.toString());
                                    }else if(errorCode == 5){
                                        // 지문 입력 취소
                                    }else{
                                        alertDialog = dialogShow(errString.toString());
                                    }
                                }
                            }

                            if(listener != null){
                                listener.onError(errorCode, errString, alertDialog);
                            }
                            dismiss();
                        }

                        @Override
                        public void onAuthenticationHelp(int helpCode, CharSequence helpString){
                            AlertDialog alertDialog = null;
                            ToastUtils.custom(Toast.makeText(getContext(), helpString, Toast.LENGTH_SHORT)).show();
//                            binding.fingerprint.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake));
                        }

                        @Override
                        public void onAuthenticationSucceeded(final FingerprintManager.AuthenticationResult result){
                            errorCount = 0;
                            new Handler().postDelayed(() -> {
                                dismiss();

                                if(listener != null){
                                    listener.onSuccess(result);
                                }
                            }, 500);
                        }

                        @Override
                        public void onAuthenticationFailed(){
                            ToastUtils.custom(Toast.makeText(getContext(), "fingerprint fail", Toast.LENGTH_SHORT)).show();
//                            errorCount++;
//                            binding.fingerprint.setBackgroundResource(R.drawable.fingerprint_off);
//                            if (errorCount < 5) {
//                                binding.fingerprintDesc2.setText(getContext().getString(R.string.bio_setting_desc2_error, errorCount));
//                                binding.fingerprintDesc2.setTextColor(ResourcesCompat.getColor(getContext().getResources(), R.color.errorColor, null));
//                            }else{
//                                errorCount = 0;
//                            }
                        }
                    }
                    , null
            );
        }catch(Exception e){
            Log.e("test", "error", e);
        }
    }



    private AlertDialog dialogShow(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(text);
        builder.setPositiveButton(getContext().getString(R.string.ok),
                (dialog, which) -> dialog.dismiss())
                .setCancelable(false);
        return builder.show();
    }

    public void setPositiveButton(OnClickListener listener){
        positiveButtonListener = listener;
    }

    /**
     * 부정 버튼 설정
     *
     * @param text     text
     * @param listener click listener
     */
    public void setNegativeButton(OnClickListener listener){
        negativeButtonListener = listener;
    }


    public interface OnAuthenticationListener{
        void onSuccess(FingerprintManager.AuthenticationResult result);

        void onError(int errorCode, CharSequence errString, AlertDialog errorDialog);

        @Deprecated
        void onCancel();
    }
}
