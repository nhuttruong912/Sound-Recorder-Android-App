package com.team18.recordapp.fragment;

import static android.content.Context.MODE_PRIVATE;

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
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team18.recordapp.User;

public class LogInDownloadShareFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    EditText edtEmail;
    EditText edtPassword;
    ProgressDialog mProgress;
    CheckBox checkbox;
    private Button btnLogInDownloadShare;
    private SharedPreferences loginSharePreferences;
    private SharedPreferences.Editor loginSharePrefsEditor;
    private Boolean saveLoginShare;

    public LogInDownloadShareFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static LogInDownloadShareFragment newInstance() {
        LogInDownloadShareFragment fragment = new LogInDownloadShareFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log_in_download_share, container, false);
        checkbox = (CheckBox) view.findViewById(R.id.checkboxShare);
        edtEmail = (EditText) view.findViewById(R.id.emailEdt);
        edtPassword = (EditText) view.findViewById(R.id.passwordEdt);
        btnLogInDownloadShare = (Button) view.findViewById(R.id.btn_log_in_download_share);
        btnLogInDownloadShare.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         logInDownloadShare(view);
                                                     }
                                                 });
                loginSharePreferences = requireContext().getSharedPreferences("loginSharePrefs", MODE_PRIVATE);
        loginSharePrefsEditor = loginSharePreferences.edit();

        saveLoginShare = loginSharePreferences.getBoolean("saveLoginShare", false);
        if (saveLoginShare) {
            edtEmail.setText(loginSharePreferences.getString("usernameShare", ""));
            edtPassword.setText(loginSharePreferences.getString("passwordShare", ""));
            checkbox.setChecked(true);
        }

        return view;
    }

    private void savePassword() {
        String password = edtPassword.getText().toString();
        String username = edtEmail.getText().toString();
        loginSharePrefsEditor.putBoolean("saveLoginShare", true);
        loginSharePrefsEditor.putString("usernameShare", username);
        loginSharePrefsEditor.putString("passwordShare", password);
        loginSharePrefsEditor.commit();
    }

    public void logInDownloadShare(View view) {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("");
        mProgress.show();

        edtEmail = (EditText) view.findViewById(R.id.emailEdt);
        edtPassword = (EditText) view.findViewById(R.id.passwordEdt);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("list_users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFound = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user.getEmail().trim().equals(edtEmail.getText().toString().trim())) {
                        if(user.getPassShare().trim().equals(edtPassword.getText().toString().trim())) {
                            if(checkbox.isChecked()) {
                                savePassword();
                            } else {
                                Toast.makeText(requireContext(), "false", Toast.LENGTH_SHORT).show();

                                loginSharePrefsEditor.clear();
                                loginSharePrefsEditor.commit();
                            }
                            isFound = true;
                            mProgress.dismiss();
                            goToDownloadShareAudioActivity(user.getEmail());
                            break;
                        }
                    }
                }
                if (!isFound) {
                    mProgress.dismiss();
                    Toast.makeText(requireContext(), "false to register", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "false to register", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToDownloadShareAudioActivity(String email) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.replaceFragment(DownloadShareFragment.newInstance(email));
    }
}