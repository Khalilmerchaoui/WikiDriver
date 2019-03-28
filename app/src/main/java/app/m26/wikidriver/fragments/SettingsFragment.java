package app.m26.wikidriver.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.m26.wikidriver.R;;
import app.m26.wikidriver.activities.ProfileActivity;
import app.m26.wikidriver.activities.SelectAppsActivity;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getTheme().applyStyle(R.style.BottomNavigationViewTheme, true);
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    Settings settings = new Settings();
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.home_frame, settings)
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getTheme().applyStyle(R.style.AppTheme, true);
        try {
            getActivity().getFragmentManager().beginTransaction()
                    .remove(settings)
                    .commit();

        } catch (Exception e) {}
    }

    public static class Settings extends PreferenceFragment {
        public Settings() {}

        PreferenceScreen selectApps, rate, contact, profile;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            selectApps = (PreferenceScreen)findPreference("selectApps");
            rate = (PreferenceScreen) findPreference("rate");
            contact =(PreferenceScreen) findPreference("contact");
            profile =(PreferenceScreen) findPreference("profile");

            rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(String.format("market://details?id=%s", getActivity().getPackageName())));
                    startActivity(intent);
                    return false;
                }
            });

            contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setData(Uri.parse("mailto:" + "Difpridriver@gmail.com"));
                    startActivity(sendIntent);
                    return false;
                }
            });

            selectApps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), SelectAppsActivity.class));
                    return false;
                }
            });

            profile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), ProfileActivity.class));
                    return false;
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }
}
