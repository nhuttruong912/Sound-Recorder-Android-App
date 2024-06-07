package com.team18.recordapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team18.recordapp.Audio;
import com.team18.recordapp.AudioAdapter;
import com.team18.recordapp.R;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team18.recordapp.Record;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioSharingFragment extends Fragment {
    ListView audioList;
    List<Audio> audioFile;
    private FirebaseAuth mAuth;
    ImageButton btn;
    ProgressDialog mProgress;
    List<String> audioToUpload;


    public AudioSharingFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AudioSharingFragment newInstance() {
        AudioSharingFragment fragment = new AudioSharingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_sharing, container, false);
        audioList = view.findViewById(R.id.listView);
        btn = view.findViewById(R.id.btn);
        mAuth = FirebaseAuth.getInstance();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked();
                uploadAudio();
            }
        });


        checkPermissionToDisplayAudio();

        if(mAuth.getCurrentUser() != null) {
            Toast.makeText(requireContext(), mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "null", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void isChecked() {
        audioToUpload = new ArrayList<>();
        for (int i = 0; i < audioList.getCount(); ++i) {
            if(audioFile.get(i).isCheck()) {
                audioToUpload.add(audioFile.get(i).getPath());
            } else {
            }
        }
    }

    private void checkPermissionToDisplayAudio() {
        if (ContextCompat.checkSelfPermission (requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED)  {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);}
            else {
                ActivityCompat.requestPermissions (requireActivity(),
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
            }
        } else {
            getMusic();
        }
    }

    private void uploadAudio() {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Uploading audio...");
        mProgress.show();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Toast.makeText(requireContext(), firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
        Toast.makeText(requireContext(), audioToUpload.get(0).toString(), Toast.LENGTH_LONG).show();
        for(String filePath: audioToUpload) {
            int indexOfFile = filePath.lastIndexOf("/");
            String fileName = filePath.substring(indexOfFile + 1);

            Uri fileUri = Uri.fromFile(new File(filePath));
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(firebaseUser.getEmail()+"/"+fileName);
            UploadTask uploadTask = storageRef.putFile(fileUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgress.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Upload failed
                    Log.w("TAG", "Audio upload failed:", e);
                }
            });
        }
    }

    private void dowloadAudio(){
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference pathReference  = storage.child("audio/example.mp3");

        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess (Uri uri){
                String url = uri.toString();
                downloadFile(requireContext(), "firebase", ".mp3", "recordings", url);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure (@NonNull Exception e) {
            }
        });
    }

    public void downloadFile (Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);
        downloadmanager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dowloadAudio();
                getMusic();
            }
        }
    }

    public void getMusic() {
        File internalStorageDir = requireContext().getExternalFilesDir("recordings");

        // List recorded files in the directory
        File[] files = internalStorageDir.listFiles();
        audioFile = new ArrayList<>();
        if (files != null) {
            audioFile.clear();
            for (File file : files) {
                try {
                    wait(500);
                }
                catch (Exception e) {

                }
                Audio record = new Audio(file.getName().split("~")[0], file.getAbsolutePath(), false, 1);
                audioFile.add(record);
            }
            AudioAdapter adapter = new AudioAdapter(requireContext(), R.layout.audio_item, audioFile);
            audioList.setAdapter(adapter);
        }
    }

}