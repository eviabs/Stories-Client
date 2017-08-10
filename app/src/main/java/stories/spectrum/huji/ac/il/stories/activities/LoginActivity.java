package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Session;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesButton;
import stories.spectrum.huji.ac.il.stories.views.StoriesEditText;
import stories.spectrum.huji.ac.il.stories.views.StoriesTextView;


public class LoginActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        if (getIntent().getBooleanExtra(RegisterActivity.NAME_OF_PUT_EXTRA_PARAM_REG, false)) {
            showSnack(getString(R.string.prompt_login), Snackbar.LENGTH_INDEFINITE, getString(R.string.dismiss));
        }

        session.apllySettings();

        if (session.isLoggedIn()) {
            startNextActivity();
        }

        StoriesButton login;
        StoriesTextView register;
        StoriesTextView forgotPassword;
        final StoriesEditText username;
        final StoriesEditText pass;

        login = (StoriesButton) findViewById(R.id.login);
        register = (StoriesTextView) findViewById(R.id.TextViewCreateAccount);
        forgotPassword = (StoriesTextView) findViewById(R.id.TextViewForgotPassword);
        username = (StoriesEditText) findViewById(R.id.username);
        pass = (StoriesEditText) findViewById(R.id.pass);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (username.getText().toString().equals("") || pass.getText().toString().equals("")) {
                    showSnack(getString(R.string.no_user_pass), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
                } else {
                    AsyncCheckLogin(username.getText().toString(), pass.getText().toString());
                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterActivity();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSnack("נסה להיזכר", Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            }
        });
    }

    private void successfulLogin(String username, int userID) {
        session.logIn(username, userID);
        startNextActivity();
    }

    private void startNextActivity() {
        Intent it = new Intent(LoginActivity.this, NearStoriesActivity.class);
        startActivity(it);
        finish();
    }

    private void startRegisterActivity() {
        Intent it = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(it);
    }

    private void AsyncCheckLogin(final String username, final String pass) {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                JSONArray arr = null;
                try {

                    if (jsonFromNet != null) {
                        arr = new JSONArray(jsonFromNet);
                        if (arr.length() > 0) {
                            int userID = arr.getJSONObject(0).getInt(StoryServerURLs.USER_USER_ID);
                            successfulLogin(username, userID);
                        } else {
                            showSnack(getString(R.string.failed_login), Snackbar.LENGTH_SHORT, "dismiss");
                        }

                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncCheckLogin(username, pass);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getUserLogindURL(username, pass));
    }
}
