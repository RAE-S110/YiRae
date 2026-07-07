package com.example.yirae;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends SecureActivity {
    private EditText etSettingsNickname;
    private TextView tvSettingsPrivacy;
    private Button btnSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        etSettingsNickname = findViewById(R.id.etSettingsNickname);
        tvSettingsPrivacy = findViewById(R.id.tvSettingsPrivacy);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        etSettingsNickname.setText(UserSettingsRepository.getNickname(this));
        tvSettingsPrivacy.setText(R.string.privacy_notice);

        btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        String nickname = etSettingsNickname.getText().toString().trim();

        if (nickname.isEmpty()) {
            etSettingsNickname.setError(getString(R.string.error_nickname_required));
            return;
        }

        UserSettingsRepository.updateNickname(this, nickname);

        Toast.makeText(this, R.string.settings_saved_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}
