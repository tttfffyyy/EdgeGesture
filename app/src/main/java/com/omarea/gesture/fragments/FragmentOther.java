package com.omarea.gesture.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.omarea.gesture.AdbProcessExtractor;
import com.omarea.gesture.DialogAppSwitchExclusive;
import com.omarea.gesture.Gesture;
import com.omarea.gesture.R;
import com.omarea.gesture.SpfConfig;
import com.omarea.gesture.StartActivity;
import com.omarea.gesture.shell.KeepShellPublic;

public class FragmentOther extends FragmentSettingsBase {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gesture_other_options, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        final Activity activity = getActivity();

        final PackageManager p = activity.getPackageManager();
        final ComponentName startActivity = new ComponentName(activity.getApplicationContext(), StartActivity.class);
        final CompoundButton hide_start_icon = activity.findViewById(R.id.hide_start_icon);
        hide_start_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (hide_start_icon.isChecked()) {
                        p.setComponentEnabledSetting(startActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    } else {
                        p.setComponentEnabledSetting(startActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    }
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        int activityState = p.getComponentEnabledSetting(startActivity);
        hide_start_icon.setChecked(activityState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED && activityState != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        bindCheckable(R.id.game_optimization, SpfConfig.GAME_OPTIMIZATION, SpfConfig.GAME_OPTIMIZATION_DEFAULT);
        bindCheckable(R.id.low_power, SpfConfig.LOW_POWER_MODE, SpfConfig.LOW_POWER_MODE_DEFAULT);

        activity.findViewById(R.id.home_animation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeAnimationPicker();
            }
        });

        setHomeAnimationText();

        // 使用ROOT获取最近任务
        Switch root_get_recents = activity.findViewById(R.id.root_get_recents);
        root_get_recents.setChecked(config.getBoolean(SpfConfig.ROOT, SpfConfig.ROOT_DEFAULT));
        root_get_recents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Checkable ele = (Checkable) v;
                if (ele.isChecked()) {
                    if (KeepShellPublic.checkRoot()) {
                        config.edit().putBoolean(SpfConfig.ROOT, true).apply();
                        new AdbProcessExtractor().updateAdbProcessState(getActivity(), true);
                        restartService();
                    } else {
                        ele.setChecked(false);
                        Gesture.toast(getString(R.string.no_root), Toast.LENGTH_SHORT);
                    }
                } else {
                    config.edit().putBoolean(SpfConfig.ROOT, false).apply();
                    restartService();
                }
            }
        });

        // 跳过切换
        getActivity().findViewById(R.id.app_switch_exclusive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAppSwitchExclusive().openDialog(getActivity());
            }
        });

        updateView();
    }

    private void updateView() {
        setHomeAnimationText();
    }

    @Override
    protected void restartService() {
        updateView();

        super.restartService();
    }

    private void homeAnimationPicker() {
        String[] options = new String[]{getString(R.string.animation_mode_default), getString(R.string.animation_mode_basic), getString(R.string.animation_mode_custom)};
        new AlertDialog.Builder(getActivity()).setTitle(R.string.animation_mode)
                .setSingleChoiceItems(options,
                        config.getInt(SpfConfig.HOME_ANIMATION, SpfConfig.HOME_ANIMATION_DEFAULT),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                config.edit().putInt(SpfConfig.HOME_ANIMATION, which).apply();
                                restartService();
                                dialog.dismiss();
                            }
                        })
                .create()
                .show();
    }

    private void setHomeAnimationText() {
        int modeIndex = config.getInt(SpfConfig.HOME_ANIMATION, SpfConfig.HOME_ANIMATION_DEFAULT);
        Button button = getActivity().findViewById(R.id.home_animation);
        switch (modeIndex) {
            case SpfConfig.HOME_ANIMATION_DEFAULT: {
                button.setText(getString(R.string.animation_mode_default));
                break;
            }
            case SpfConfig.HOME_ANIMATION_BASIC: {
                button.setText(getString(R.string.animation_mode_basic));
                break;
            }
            case SpfConfig.HOME_ANIMATION_CUSTOM: {
                button.setText(getString(R.string.animation_mode_custom));
                break;
            }
        }
    }
}
