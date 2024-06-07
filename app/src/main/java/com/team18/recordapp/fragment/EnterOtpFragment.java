package com.team18.recordapp.fragment;

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
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team18.recordapp.User;

import java.util.concurrent.TimeUnit;


public class EnterOtpFragment extends Fragment {
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PASSWORD = "password";

    // TODO: Rename and change types of parameters
    private String email;
    private String password;

    private FirebaseAuth mAuth;
    private EditText edtOtp;
    private Button btnVerifyOtp;
    private String mPhoneNumber;
    private String mVerificationId;
    TextView reSendOtp;
    ProgressDialog mProgress;
    public EnterOtpFragment() {
        // Required empty public constructor
    }


    public static EnterOtpFragment newInstance(String param1, String param2) {
        EnterOtpFragment fragment = new EnterOtpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, param1);
        args.putString(ARG_PASSWORD, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
            password = getArguments().getString(ARG_PASSWORD);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enter_otp, container, false);
        edtOtp = view.findViewById(R.id.edt_otp);
        btnVerifyOtp = view.findViewById(R.id.btn_verify_otp);
        reSendOtp = (TextView) view.findViewById(R.id.reSendOtp);

        mAuth = FirebaseAuth.getInstance();
        getPhone();
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

        reSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPhone(mPhoneNumber);
                Toast.makeText(requireActivity(), "Opt code have been resend", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void getPhone() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("list_users");

        String targetKey = firebaseUser.getUid();

        DatabaseReference childRef = parentRef.child(targetKey);
        mProgress = new ProgressDialog(requireActivity());
        mProgress.setMessage("wait...");
        mProgress.show();
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mPhoneNumber = user.getPhone();
                String phone = "+84"+mPhoneNumber.substring(1);
                verifyPhone(phone);
                Toast.makeText(requireActivity(), "Opt code have been resend", Toast.LENGTH_SHORT).show();
                mProgress.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
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
        mProgress = new ProgressDialog(requireActivity());
        mProgress.setMessage("");
        mProgress.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            logIn();
                        } else {
                            Toast.makeText(requireActivity(), "Double check Opt code or your code is time out.", Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            }
                        }
                    }
                });
    }


//    private void goToAudioSharingActivity() {
//        Intent intent = new Intent(EnterOtpActivity.this, AudioSharingActivity.class);
//        startActivity(intent);
//    }

    private void logIn() {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            MainActivity mainActivity = (MainActivity) requireActivity();
                            mProgress.dismiss();
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}