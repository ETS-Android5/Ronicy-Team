package com.adeasy.advertise.ui.addphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.adeasy.advertise.R;
import com.adeasy.advertise.ViewModel.AddNewPhoneViewModel;
import com.google.android.material.textfield.TextInputEditText;
/**
 * Created by Manuka yasas,
 * University Sliit
 * Email manukayasas99@gmail.com
 **/
public class AddNewNumber extends AppCompatActivity {

    FrameLayout frameLayout;
    Toolbar toolbar;

    //fragments
    EnterNumber enterNumber;
    EnterCode enterCode;

    String phoneNumberEntered;

    AddNewPhoneViewModel addNewPhoneViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manuka_activity_add_new_number);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        frameLayout = findViewById(R.id.commonFrame);

        //fragments
        enterNumber = new EnterNumber();
        enterCode = new EnterCode();

        addNewPhoneViewModel = ViewModelProviders.of(this).get(AddNewPhoneViewModel.class);

        startEnterNewNumber();

        addNewPhoneViewModel.getPhoneNumber().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String phoneNumber) {
                phoneNumberEntered = phoneNumber;
                startEnterCode();
            }
        });
        addNewPhoneViewModel.getVerifiedNumber().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer != null){
                    Intent intentEnd = new Intent();
                    intentEnd.putExtra("phone", integer);
                    setResult(RESULT_OK,intentEnd);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFragment() instanceof EnterNumber)
            finish();
        else
            super.onBackPressed();
    }

    private Fragment getCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(frameLayout.getId());
        return currentFragment;
    }


    private void startEnterNewNumber() {
        getSupportActionBar().setTitle("Enter phone");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(frameLayout.getId(), enterNumber);
        fragmentTransaction.commit();
    }

    private void startEnterCode() {
        Bundle bundle = new Bundle();
        bundle.putString("phone", phoneNumberEntered);
        enterCode.setArguments(bundle);
        getSupportActionBar().setTitle("Enter Code");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(EnterNumber.class.getName());
        fragmentTransaction.replace(frameLayout.getId(), enterCode);
        fragmentTransaction.commit();
    }

}