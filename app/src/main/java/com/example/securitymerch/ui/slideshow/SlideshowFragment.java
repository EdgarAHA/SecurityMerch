package com.example.securitymerch.ui.slideshow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.securitymerch.CambiarContraseña;
import com.example.securitymerch.MainActivity;
import com.example.securitymerch.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SlideshowFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private Button btnChangePassword, btnLogout;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        tvUserName = root.findViewById(R.id.tv_user_name);
        tvUserEmail = root.findViewById(R.id.tv_user_email);
        btnChangePassword = root.findViewById(R.id.btn_change_password);
        btnLogout = root.findViewById(R.id.btn_logout);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario");
            tvUserEmail.setText(currentUser.getEmail());
        }

        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(getActivity(), CambiarContraseña.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        });

        return root;
    }
}
