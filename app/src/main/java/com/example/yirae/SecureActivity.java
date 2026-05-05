package com.example.yirae;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public abstract class SecureActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        if (!UserSettingsRepository.isConfigured(this)) {
            return;
        }
        if (!SessionManager.isUnlocked()) {
            Intent intent = new Intent(this, SecurityActivity.class);
            intent.putExtra("mode", "unlock");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
