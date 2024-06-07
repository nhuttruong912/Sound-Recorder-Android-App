package com.team18.recordapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.team18.recordapp.Audio;
import com.team18.recordapp.AudioAdapter;
import com.team18.recordapp.R;

import androidx.annotation.NonNull;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadShareFragment extends Fragment {

    TextView txt;
    ListView audioList;
    List<Audio> audioFile;
    Button btnDownloadShare;
    List<String> audioToDownload;
    ImageButton deleteBtn;
    ProgressDialog mProgress;
    private static String ARG_EMAIL = "email";
    private String email;

    public DownloadShareFragment() {
        // Required empty public constructor
    }


    public static DownloadShareFragment newInstance(String email) {
        DownloadShareFragment fragment = new DownloadShareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_download_share, container, false);
        audioList = view.findViewById(R.id.listViewDownloadShare);
        btnDownloadShare = view.findViewById(R.id.downloadShareBtn);
        deleteBtn = (ImageButton) view.findViewById(R.id.deleteAudioBtn);
        txt = (TextView) view.findViewById(R.id.shareFolder);
        txt.setText(email);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(email.equals(firebaseUser.getEmail())) {
            deleteBtn.setVisibility(View.VISIBLE);
        }
        btnDownloadShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked();
                checkPermissionToDownload();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChecked();
                deleteAudio();
                displayAudioFiles();
                displayAudioFiles();
                displayAudioFiles();
            }
        });
        displayAudioFiles();
        return view;
    }

    private boolean isAudioFile(String fileName) {
        String[] audioExtensions = {".mp3", ".wav", ".ogg"};  // Add more extensions as needed
        for (String extension : audioExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private void displayAudioFiles() {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(email);

        storageRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<StorageReference> allFiles = listResult.getItems();
                        audioFile = new ArrayList<>();

                        for (StorageReference fileRef : allFiles) {
                            String fileName = fileRef.getName();
                            if (isAudioFile(fileName)) {
                                audioFile.add(new Audio(fileName, "",false,1));
                            }
                        }

                        AudioAdapter adapter = new AudioAdapter(requireContext(), R.layout.audio_item, audioFile);
                        audioList.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Can't display", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void isChecked() {
        audioToDownload = new ArrayList<>();
        for (int i = 0; i < audioList.getCount(); ++i) {
            if(audioFile.get(i).isCheck()) {
                audioToDownload.add(audioFile.get(i).getName());
            }
        }
    }

    private void checkPermissionToDownload() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(requireActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, 1);
            } else {
                downloadAudio();
            }
        } else {
            downloadAudio();
        }
    }

    public void downloadFile (String fileName, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(requireContext(), "recordings", fileName);
        request.setTitle(fileName);
        downloadmanager.enqueue(request);
    }

    private void downloadAudio(){
        StorageReference storage = FirebaseStorage.getInstance().getReference();

        for(String i: audioToDownload) {
            Toast.makeText(requireContext(), "Downloading " + i, Toast.LENGTH_SHORT).show();
            StorageReference pathReference  = storage.child(email + "/" + i);
            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess (Uri uri){
                    String url = uri.toString();
                    File internalStorageDir = requireContext().getFilesDir();
                    String subdirectory = "recordings"; // Example subdirectory name
                    File recordingsDir = new File(internalStorageDir, subdirectory);
                    if (!recordingsDir.exists()) {
                        recordingsDir.mkdirs();
                    }
                    downloadFile(i, recordingsDir.getAbsolutePath(), url);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure (@NonNull Exception e) {
                }
            });
        }
    }

    private void deleteAudio() {
        mProgress = new ProgressDialog(requireContext());
        mProgress.setMessage("Deleting audio...");
        mProgress.show();
        for(String i:audioToDownload) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference desertRef = storageRef.child(email + "/" +i);
            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mProgress.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    mProgress.dismiss();
                    Toast.makeText(requireContext(), "Delete fail", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadAudio();
            }
        }
    }
}