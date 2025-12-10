package com.example.bs.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.bs.ui.LoginActivity;
import com.example.bs.util.SessionManager;

public abstract class BaseFragment extends Fragment {

    protected SessionManager sessionManager;
    protected long currentUserId = -1;
    protected boolean isUserLoggedIn = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        sessionManager = new SessionManager(context);
        checkUserSession();
    }

    protected void checkUserSession() {
        isUserLoggedIn = sessionManager.isLoggedIn();
        if (isUserLoggedIn) {
            currentUserId = sessionManager.getUserId();
        } else {
            currentUserId = -1;
        }
    }

    protected void showNotAuthorizedMessage() {
        Toast.makeText(requireContext(), "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show();
    }

    protected void navigateToLogin() {
        startActivity(new Intent(requireContext(), LoginActivity.class));
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Проверяем сессию при каждом возобновлении
        checkUserSession();
    }
}