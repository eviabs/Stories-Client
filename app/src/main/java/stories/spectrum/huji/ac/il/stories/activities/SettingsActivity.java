package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.text.DecimalFormat;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Session;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesButton;
import stories.spectrum.huji.ac.il.stories.views.StoriesEditText;
import stories.spectrum.huji.ac.il.stories.views.StoriesTextView;

import static android.support.v7.appcompat.R.id.info;


public class SettingsActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int getLayoutWidthHeight() { return ViewGroup.LayoutParams.MATCH_PARENT; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCacheSize();

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);

            ((TextView) findViewById(R.id.TextViewVersionNumber)).setText(info.versionName + " (" + info.versionCode + ")");

        } catch (PackageManager.NameNotFoundException e) {
            ((TextView) findViewById(R.id.TextViewVersionNumber)).setText("1.0");
        }

        ((TextView) findViewById(R.id.TextViewSiteUrl)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + getString(R.string.website)));
                startActivity(browserIntent);
            }
        });

        final TextView serverTextView = ((TextView) findViewById(R.id.TextViewStateServer));
        serverTextView.setText(session.getSettingsServer() ? getString(R.string.settings_on) : getString(R.string.settings_off));

        Switch serverSwitch = ((Switch) findViewById(R.id.switchServer));
        serverSwitch.setChecked(session.getSettingsServer());
        serverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                session.setSettingsServer(isChecked);
                serverTextView.setText(isChecked ? getString(R.string.settings_on) : getString(R.string.settings_off));
                session.apllySettings();
            }
        });

        final TextView navigationTextView = ((TextView) findViewById(R.id.TextViewStateNavigation));
        navigationTextView.setText(session.getSettingsNavigation() ? getString(R.string.settings_on) : getString(R.string.settings_off));

        Switch navigationSwitch = ((Switch) findViewById(R.id.switchNavigation));
        navigationSwitch.setChecked(session.getSettingsNavigation());
        navigationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                session.setSettingsNavigation(isChecked);
                navigationTextView.setText(isChecked ? getString(R.string.settings_on) : getString(R.string.settings_off));
                session.apllySettings();
            }
        });

        final TextView showAllRecordingsTextView = ((TextView) findViewById(R.id.TextViewStateShowAllRecordings));
        showAllRecordingsTextView.setText(session.getSettingsShowAllRecordings() ? getString(R.string.settings_on) : getString(R.string.settings_off));

        Switch showAllRecordingsSwitch = ((Switch) findViewById(R.id.switchShowAllRecordingsTextView));
        showAllRecordingsSwitch.setChecked(session.getSettingsShowAllRecordings());
        showAllRecordingsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                session.setSettingsShowAllRecordings(isChecked);
                showAllRecordingsTextView.setText(isChecked ? getString(R.string.settings_on) : getString(R.string.settings_off));
                session.apllySettings();
            }
        });

        TextView clearCacheTextView = ((TextView) findViewById(R.id.TextViewCacheClear));
        clearCacheTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                clearCache();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                // Do nothing
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getString(R.string.settings_cache_delete_prompt)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();

            }
        });
    }

    @Override
    protected void refresh() {
        setCacheSize();
    }

    private void setCacheSize() {
        long size = 0;
        size += getDirSize(getCacheDir());
        size += getDirSize(getExternalCacheDir());
        ((TextView) findViewById(R.id.TextViewStateCache)).setText(readableFileSize(size));
    }

    public long getDirSize(File dir){
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public void clearCache() {
        deleteCache(context);
        setCacheSize();

    }
}
