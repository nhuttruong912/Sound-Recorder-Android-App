package com.team18.recordapp.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team18.recordapp.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team18.recordapp.User;

public class AccountFragment extends Fragment {
    Button changePass;
    Button changeSharePass;
    RelativeLayout changePasswordCover;
    Button registerChangeBtn;
    EditText oldPass;
    EditText newPass;
    Boolean state = true;
    ProgressDialog mProgress;


    public AccountFragment() {
        // Required empty public constructor
    }


    public static AccountFragment newInstance() {
        AccountFragment fragment = new AccountFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        changePass = (Button) view.findViewById(R.id.changePasswordBtn);
        changeSharePass = (Button) view.findViewById(R.id.changePasswordShareBtn);
        registerChangeBtn = (Button) view.findViewById(R.id.registerChangeBtn);
        changePasswordCover = (RelativeLayout) view.findViewById(R.id.changePasswordCover);
        oldPass = (EditText) view.findViewById(R.id.oldPass);
        newPass = (EditText) view.findViewById(R.id.newPass);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordCover.setVisibility(View.VISIBLE);
                state = false;
            }
        });

        changeSharePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordCover.setVisibility(View.VISIBLE);
                state = true;
            }
        });

        changePasswordCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePasswordCover.setVisibility(View.INVISIBLE);
            }
        });

        registerChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                handleChange(firebaseUser.getUid());
            }
        });
        return view;
    }

    private void handleChange(String id) {
        if(state) {
            changeSharePass(id);
        } else {
            changePassword(id);
        }
    }

    private void changePassword(String id) {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("");
        mProgress.show();


        String stringNew = newPass.getText().toString().trim();
        String stringOld = oldPass.getText().toString().trim();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("list_users");
        DatabaseReference childRef = parentRef.child(id);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("list_users/" +id);
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(user.getPassword().equals(stringOld) && stringNew.length() >= 6) {
                    firebaseUser.updatePassword(stringNew)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Your password is changed", Toast.LENGTH_SHORT).show();
                                        user.setPassword(stringNew);
                                        ref.setValue(user, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                                            }
                                        });
                                        resetEditTextPassword();
                                    } else {
                                        Toast.makeText(requireContext(), "Your password is not updated yet", Toast.LENGTH_SHORT).show();
                                    }
                                    mProgress.dismiss();
                                }
                            });

                } else {
                    Toast.makeText(requireContext(), "Check your password again", Toast.LENGTH_SHORT).show();
                }
                mProgress.dismiss();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void resetEditTextPassword() {
        oldPass.setText("");
        newPass.setText("");
    }

    private void changeSharePass(String id) {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("");
        mProgress.show();

        String stringNew = newPass.getText().toString().trim();
        String stringOld = oldPass.getText().toString().trim();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("list_users/" +id);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.getPassShare().equals(stringOld) && stringNew.length() > 0) {
                    user.setPassShare(stringNew);
                    ref.setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError error, @NonNull DatabaseReference ref) {
                            mProgress.dismiss();
                            Toast.makeText(requireContext(), "your password is changed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mProgress.dismiss();
                    Toast.makeText(requireContext(), "check your password again", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}