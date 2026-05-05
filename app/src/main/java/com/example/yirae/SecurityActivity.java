package com.example.yirae;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecurityActivity extends AppCompatActivity {
    private TextView tvSecurityTitle;
    private TextView tvPrivacyHint;
    private EditText etNickname;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnConfirm;
    private boolean setupMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        tvSecurityTitle = findViewById(R.id.tvSecurityTitle);
        tvPrivacyHint = findViewById(R.id.tvPrivacyHint);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirmSecurity);

        setupMode = !UserSettingsRepository.isConfigured(this);

        if (setupMode) {
            renderSetupMode();
        } else {
            renderUnlockMode();
        }

        btnConfirm.setOnClickListener(v -> {
            if (setupMode) {
                saveInitialSettings();
            } else {
                unlockApp();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (setupMode) {
            moveTaskToBack(true);
            return;
        }
        super.onBackPressed();
    }

    private void renderSetupMode() {
        tvSecurityTitle.setText(R.string.security_setup_title);
        tvPrivacyHint.setText(R.string.privacy_notice);
        etNickname.setVisibility(View.VISIBLE);
        etNickname.setText(UserSettingsRepository.getNickname(this));
        etNickname.setHint(R.string.hint_nickname);
        etConfirmPassword.setVisibility(View.VISIBLE);
        etPassword.setText("");
        etPassword.setHint(R.string.hint_lock_password);
        etConfirmPassword.setText("");
        etConfirmPassword.setHint(R.string.hint_confirm_lock_password);
        btnConfirm.setText(R.string.save_and_enter);
    }

    private void renderUnlockMode() {
        tvSecurityTitle.setText(getString(R.string.security_unlock_title, UserSettingsRepository.getNickname(this)));
        tvPrivacyHint.setText(R.string.privacy_notice_short);
        etNickname.setVisibility(View.GONE);
        etConfirmPassword.setVisibility(View.GONE);
        etPassword.setText("");
        etPassword.setHint(R.string.input_password_to_unlock);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        btnConfirm.setText(R.string.unlock_app);
    }

    private void saveInitialSettings() {
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (nickname.isEmpty()) {
            etNickname.setError(getString(R.string.error_nickname_required));
            return;
        }
        if (password.length() < 4) {
            etPassword.setError(getString(R.string.error_password_length));
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_not_match));
            return;
        }

        UserSettingsRepository.saveInitialSettings(this, nickname, password);
        SessionManager.unlock();
        Toast.makeText(this, R.string.settings_saved_success, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void unlockApp() {
        String password = etPassword.getText().toString().trim();
        if (!UserSettingsRepository.verifyPassword(this, password)) {
            etPassword.setError(getString(R.string.error_password_incorrect));
            return;
        }

        SessionManager.unlock();
        openHome();
    }

    private void openHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
