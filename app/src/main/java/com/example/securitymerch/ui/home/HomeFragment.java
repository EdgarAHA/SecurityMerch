package com.example.securitymerch.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.securitymerch.R;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Configurar clics en los CardView
        view.findViewById(R.id.card_bebidas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_Bebidas)
        );

        view.findViewById(R.id.card_botanas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_Botanas)
        );

        view.findViewById(R.id.card_limpieza).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_Limpieza)
        );

        view.findViewById(R.id.card_legumbres).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_Legumbres)
        );

        view.findViewById(R.id.card_galletas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_Galletas)
        );

        return view;
    }
}
