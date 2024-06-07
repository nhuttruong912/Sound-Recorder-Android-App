package com.team18.recordapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.team18.recordapp.fragment.AccountFragment;
import com.team18.recordapp.fragment.AudioSharingFragment;
import com.team18.recordapp.fragment.ListRecordFragment;
import com.team18.recordapp.fragment.LogInDownloadShareFragment;
import com.team18.recordapp.fragment.LogInFragment;
import com.team18.recordapp.fragment.RecordFragment;
import com.team18.recordapp.fragment.ScheduleFragment;
import com.team18.recordapp.fragment.StatisticsFragment;
import com.team18.recordapp.util.Clock;

public class MainActivity extends AppCompatActivity {
    private RecordFragment recordFragment = new RecordFragment();
    private DrawerLayout drawerLayout;
    private ImageButton btn_menu;
    private NavigationView navigationView;
    private TextView tvUserName, tvEmail;
    MenuItem loginItem;
    MenuItem logoutItem;
    MenuItem accountItem;
    MenuItem sharingItem;
    MenuItem downloadSharingItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setControl();
        setEvent();
        isLogin();
        replaceFragment(recordFragment);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.close();
            return;
        }
        super.onBackPressed();
    }


    private void setEvent() {
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                if (itemId == R.id.record_item) {
                    replaceFragment(recordFragment);
                } else if (itemId == R.id.list_record_item) {
                    replaceFragment(ListRecordFragment.newInstance());
                } else if (itemId == R.id.schedule_item) {
                    replaceFragment(ScheduleFragment.newInstance());
                } else if (itemId == R.id.statistics_item) {
                    replaceFragment(StatisticsFragment.newInstance());
                } else if (itemId == R.id.account_item) {
                    replaceFragment(AccountFragment.newInstance());
                } else if (itemId == R.id.login_item) {
                    replaceFragment(LogInFragment.newInstance());
                } else if (itemId == R.id.logout_item) {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();

                } else if (itemId == R.id.sharing_item) {
                    replaceFragment(AudioSharingFragment.newInstance());
                } else if (itemId == R.id.download_sharing_item) {
                    replaceFragment(LogInDownloadShareFragment.newInstance());
                }
                isLogin();
                drawerLayout.open();
                drawerLayout.close();

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        isLogin();
        super.onStart();
    }


    public void isLogin() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        drawerLayout = findViewById(R.id.drawer_layout);
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String name = email.split("@")[0];
            tvUserName.setText(name);
            tvEmail.setText(email);
            setVisibleItem(true);
        } else {
            tvUserName.setText("Guest");
            tvEmail.setText("");
            setVisibleItem(false);
        }

    }

    private void setVisibleItem(boolean flag) {
        loginItem.setVisible(!flag);
        logoutItem.setVisible(flag);
        accountItem.setVisible(flag);
        sharingItem.setVisible(flag);
        downloadSharingItem.setVisible(flag);
    }


    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void setControl() {
        drawerLayout = findViewById(R.id.drawer_layout);
        btn_menu = findViewById(R.id.btn_menu);
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tv_user_name);
        tvEmail = headerView.findViewById(R.id.tv_user_email);
        Menu menu = navigationView.getMenu();
        loginItem = menu.findItem(R.id.login_item);
        logoutItem = menu.findItem(R.id.logout_item);
        accountItem = menu.findItem(R.id.account_item);
        sharingItem = menu.findItem(R.id.sharing_item);
        downloadSharingItem = menu.findItem(R.id.download_sharing_item);
    }
}