package com.team18.recordapp.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.team18.recordapp.MainActivity;
import com.team18.recordapp.R;


import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.team18.recordapp.User;

import java.util.concurrent.TimeUnit;

public class LogUpFragment extends Fragment {
    private FirebaseAuth mAuth;
    private Button btnVerifyOtp, btnLogUp;
    private String mPhoneNumber, mVerificationId;
    private TextView reSendOtp;
    private ProgressDialog mProgress;
    private EditText edtEmail, edtPassword, edtPhone, edtSharePass, edtOtp, edtPasswordConfirm, edtSharePassConfirm;
    private RelativeLayout showUpEnterOpt;
    private SharedPreferences loginPreferences, loginSharePreferences;
    private SharedPreferences.Editor loginPrefsEditor, loginSharePrefsEditor;
    private Boolean saveLogin, saveLoginShare;


    public LogUpFragment() {
        // Required empty public constructor
    }

    public static LogUpFragment newInstance() {
        LogUpFragment fragment = new LogUpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_log_up, container, false);
        edtEmail = view.findViewById(R.id.username);
        edtPassword = view.findViewById(R.id.password);
        edtPhone = view.findViewById(R.id.numPhone);
        edtSharePass = view.findViewById(R.id.sharePass);
        edtPasswordConfirm = view.findViewById(R.id.password_confirm);
        edtSharePassConfirm = view.findViewById(R.id.sharePass_confirm);

        loginPreferences = requireContext().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        loginSharePreferences = requireContext().getSharedPreferences("loginSharePrefs", MODE_PRIVATE);
        loginSharePrefsEditor = loginSharePreferences.edit();
        saveLoginShare = loginSharePreferences.getBoolean("saveLoginShare", false);

        btnLogUp = view.findViewById(R.id.logUpBtn);
        btnLogUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s= edtPassword.getText().toString().trim();
                if(s.length() >= 6 && edtPhone.getText().toString().trim().length() >= 10 && edtSharePass.length() > 0
                        && (edtEmail.getText().toString().trim().toLowerCase().contains("@")
                        && edtEmail.getText().toString().trim().toLowerCase().contains("gmail")
                        && edtEmail.getText().toString().trim().toLowerCase().contains(".com"))
                ) {
                    checkIfUserExisted();
                } else {
                    Toast.makeText(requireActivity(), "Check your info again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edtOtp = view.findViewById(R.id.edtOtpLogUp);
        btnVerifyOtp = view.findViewById(R.id.btnVerifyOtpLogUp);
        reSendOtp = (TextView) view.findViewById(R.id.reSendOtpLogUp);
        showUpEnterOpt = (RelativeLayout) view.findViewById(R.id.showUpEnterOpt);
        mAuth = FirebaseAuth.getInstance();

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strOtp = edtOtp.getText().toString().trim();
                if(strOtp.length() == 6) {
                    onClickSendOtp(strOtp);
                } else {
                    Toast.makeText(requireActivity(), "Enter Opt code.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        showUpEnterOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpEnterOpt.setVisibility(View.INVISIBLE);
            }
        });


        reSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(requireActivity(), "Opt code have been resend", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private boolean isValidInput() {
        if (edtEmail.getText().toString().trim().isEmpty()
                || edtPassword.getText().toString().trim().isEmpty()
                || edtPhone.getText().toString().trim().isEmpty()
                || edtSharePass.getText().toString().trim().isEmpty()
                || edtSharePass.getText().toString().trim().length() < 3)
        {
            Toast.makeText(requireActivity(), "Bạn nhập chưa đủ thông tin", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isPasswordValid(edtPassword, edtPasswordConfirm)) {
            return false;
        }

        if (!isPasswordValid(edtSharePass, edtSharePassConfirm)) {
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(EditText edtPassword, EditText edtPasswordConfirm) {
        String password = edtSharePass.getText().toString().trim();
        String confirmPassword = edtSharePassConfirm.getText().toString().trim();
        if (!password.equals(confirmPassword)) {
            edtPasswordConfirm.setError("Mật khẩu nhập lại không đúng");
            return false;
        }
        return true;
    }

    private void checkIfUserExisted() {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Registering...");
        mProgress.show();

        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list_users");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user.getEmail().trim().equals(email) || user.getPhone().trim().equals(phone)) {
                        mProgress.dismiss();
                        Toast.makeText(requireContext(), "email or number phone is existed", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        mProgress.dismiss();
                        getPhone();
                        showUpEnterOpt.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    private void logUp() {
        mAuth = FirebaseAuth.getInstance();

        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Registering...");
        mProgress.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            saveUser(firebaseUser.getUid());
                            clearRememberMe();
                            Toast.makeText(requireActivity(), "Register successfully", Toast.LENGTH_SHORT).show();
                            MainActivity mainActivity = (MainActivity) requireActivity();
                            mainActivity.replaceFragment(RecordFragment.newInstance());
                            mProgress.dismiss();
                        } else {
                            Toast.makeText(requireActivity(), "Authentication failed. Your email password need 6 chars!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUser(String uid) {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String sharePass = edtSharePass.getText().toString().trim();
        User user = new User(uid, email, password, phone, sharePass);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list_users");

        myRef.child(uid).setValue(user);
    }

    private void getPhone() {
        mPhoneNumber = edtPhone.getText().toString().trim();
        String phone = "+84" + mPhoneNumber.substring(1);
        verifyPhone(phone);
    }

    private void onClickSendOtp(String strOtp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, strOtp);
        signInWithPhoneAuthCredential(credential);
    }


    private void verifyPhone(String strPhoneNum) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(strPhoneNum)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(requireActivity(), "invalid", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verificationId, forceResendingToken);
                                mVerificationId = verificationId;
                            }
                        }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireActivity(), "Log up", Toast.LENGTH_SHORT).show();
                            logUp();
                        } else {
                            Toast.makeText(requireActivity(), "Double check Opt code or your code is time out.", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            }
                        }
                    }
                });
    }

    private void clearRememberMe() {
        loginPrefsEditor.clear();
        loginPrefsEditor.commit();
        loginSharePrefsEditor.clear();
        loginSharePrefsEditor.commit();
    }
}