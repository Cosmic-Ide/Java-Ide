package org.cosmic.ide;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import org.cosmic.ide.ui.utils.CustomThemeHelper;
import org.cosmic.ide.ui.utils.DarkThemeHelper;
import org.cosmic.ide.ui.utils.UiUtilsKt;

public abstract class BaseActivity extends AppCompatActivity {

    private boolean isDelegateCreated = false;

    protected SharedPreferences ui_settings;
    protected SharedPreferences compiler_settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomThemeHelper.apply(this);
        super.onCreate(savedInstanceState);

        ui_settings = getSharedPreferences("ui_settings", MODE_PRIVATE);
        compiler_settings = getSharedPreferences("compiler_settings", MODE_PRIVATE);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        UiUtilsKt.addSystemWindowInsetToPadding(getRootActivityView(), true, false, true, false);
    }

    @Override
    public AppCompatDelegate getDelegate() {
        var delegate = super.getDelegate();
        if (!isDelegateCreated) {
            isDelegateCreated = true;
            DarkThemeHelper.apply(this);
        }
        return delegate;
    }

    @NonNull
    private View getRootActivityView() {
        return getWindow().getDecorView().findViewById(android.R.id.content);
    }

    protected boolean isDarkMode() {
        int uiMode = getResources().getConfiguration().uiMode;
        return (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }
}
