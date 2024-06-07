package com.team18.recordapp.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team18.recordapp.MainActivity;
import com.team18.recordapp.R;
import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LogInFragment extends Fragment {

    TextView txt;
    Button btn;
    TextView edtEmail;
    TextView edtPassword;
    TextView trueChartTxt;
    TextView falseChartTxt;
    CheckBox checkbox;
    Button logUp;
    ProgressDialog mProgress;
    private FirebaseAuth mAuth;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    private int i1 = 75;
    private int i2 = 25;
    public LogInFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in, container, false);

        txt = view.findViewById(R.id.textView);
        btn = view.findViewById(R.id.logBtn);
        trueChartTxt = view.findViewById(R.id.trueChartTxt);
        falseChartTxt = view.findViewById(R.id.fasleChartTxt);
        checkbox = view.findViewById(R.id.checkbox);
        logUp = view.findViewById(R.id.logUpBtn);
        edtEmail = view.findViewById(R.id.username);
        edtPassword = view.findViewById(R.id.password);

        loginPreferences = view.getContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            edtEmail.setText(loginPreferences.getString("email", ""));
            edtPassword.setText(loginPreferences.getString("password", ""));
            checkbox.setChecked(true);
        } else {
            FirebaseUser auth = mAuth.getInstance().getCurrentUser();
            if(auth != null) {
                edtEmail.setText(auth.getEmail().toString().trim());
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });



        logUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogUpActivity();
            }
        });

        return view;
    }

    private void logIn() {
        mAuth = FirebaseAuth.getInstance();
        String checkEmail = "";

        if(mAuth.getCurrentUser() != null) {
            checkEmail = mAuth.getCurrentUser().getEmail();
        }

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("");
        mProgress.show();

        String finalCheckEmail = checkEmail;
        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if (checkbox.isChecked()) {
                                    loginPrefsEditor.putBoolean("saveLogin", true);
                                    loginPrefsEditor.putString("email", email);
                                    loginPrefsEditor.putString("password", password);
                                    loginPrefsEditor.commit();
                                } else {
                                    loginPrefsEditor.clear();
                                    loginPrefsEditor.commit();
                                }

                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if(firebaseUser != null && firebaseUser.getEmail().equals(finalCheckEmail)) {
                                    Intent intent = new Intent(requireContext(), MainActivity.class);
                                    startActivity(intent);
                                    requireActivity().finish();
                                } else {
                                    goToEnterOtpActivity(email, password);
                                }

                                mProgress.dismiss();
                            } else {
                                mProgress.dismiss();
                                Toast.makeText(requireActivity(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void goToEnterOtpActivity(String email, String password) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.replaceFragment(EnterOtpFragment.newInstance(email, password));
    }


    private void goToLogUpActivity() {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.replaceFragment(LogUpFragment.newInstance());
    }
}