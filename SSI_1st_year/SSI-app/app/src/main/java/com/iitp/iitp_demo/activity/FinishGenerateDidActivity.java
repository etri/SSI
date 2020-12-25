package com.iitp.iitp_demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import com.iitp.core.identity.Identity;
import com.iitp.iitp_demo.R;
import com.iitp.iitp_demo.data.IdentityStore;
import com.iitp.iitp_demo.databinding.ActivityFinishGenDidBinding;

public class FinishGenerateDidActivity extends BaseActivity{

    ActivityFinishGenDidBinding layout;
    public ObservableField<String> metaId = new ObservableField<>();
    private Identity identity; //identity

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        layout = DataBindingUtil.setContentView(this, R.layout.activity_finish_gen_did);
        layout.setActivity(this);
        setSupportActionBar(layout.toolbar.appbar);
        if(getSupportActionBar() != null){
            TextView titleText = findViewById(R.id.appbar_title);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            titleText.setText(R.string.gendid);
        }
        identity = IdentityStore.loadIdentity(FinishGenerateDidActivity.this);
        metaId.set(identity.getMetaId());

    }

    public void btnClick(){
        Intent intent = new Intent(FinishGenerateDidActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }
}
