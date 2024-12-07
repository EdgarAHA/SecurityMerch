package com.example.securitymerch.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.securitymerch.Bebidas;
import com.example.securitymerch.Botanas;
import com.example.securitymerch.Dulces;
import com.example.securitymerch.Galletas;
import com.example.securitymerch.Legumbres;
import com.example.securitymerch.Limpieza;
import com.example.securitymerch.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configurar clics en los CardView
        view.findViewById(R.id.card_bebidas).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Bebidas.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_botanas).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Botanas.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_limpieza).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Limpieza.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_legumbres).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Legumbres.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_galletas).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Galletas.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_dulces).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Dulces.class);
            startActivity(intent);
        });

        return view;
    }
}
