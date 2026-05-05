package com.example.yirae;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends SecureActivity {
    private EditText etSettingsNickname;
    private EditText etSettingsPassword;
    private TextView tvSettingsPrivacy;
    private Button btnSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etSettingsNickname = findViewById(R.id.etSettingsNickname);
        etSettingsPassword = findViewById(R.id.etSettingsPassword);
        tvSettingsPrivacy = findViewById(R.id.tvSettingsPrivacy);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        etSettingsNickname.setText(UserSettingsRepository.getNickname(this));
        tvSettingsPrivacy.setText(R.string.privacy_notice);

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String nickname = etSettingsNickname.getText().toString().trim();
        String password = etSettingsPassword.getText().toString().trim();

        if (nickname.isEmpty()) {
            etSettingsNickname.setError(getString(R.string.error_nickname_required));
            return;
        }
        if (!password.isEmpty() && password.length() < 4) {
            etSettingsPassword.setError(getString(R.string.error_password_length));
            return;
        }

        UserSettingsRepository.updateNickname(this, nickname);
        if (!password.isEmpty()) {
            UserSettingsRepository.updatePassword(this, password);
        }

        Toast.makeText(this, R.string.settings_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
