package stories.spectrum.huji.ac.il.stories.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import stories.spectrum.huji.ac.il.stories.R;
import stories.spectrum.huji.ac.il.stories.Session;
import stories.spectrum.huji.ac.il.stories.net.AsyncGetRequest;
import stories.spectrum.huji.ac.il.stories.net.AsyncRequestListener;
import stories.spectrum.huji.ac.il.stories.net.StoryServerURLs;
import stories.spectrum.huji.ac.il.stories.views.StoriesButton;
import stories.spectrum.huji.ac.il.stories.views.StoriesEditText;
import stories.spectrum.huji.ac.il.stories.views.StoriesTextView;


public class RegisterActivity extends BaseActivity {

    StoriesButton register;
    StoriesEditText username;
    StoriesEditText pass;
    StoriesEditText passVerify;
    StoriesEditText email;
    public static final String NAME_OF_PUT_EXTRA_PARAM_REG = "was_registered";


    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        register = (StoriesButton) findViewById(R.id.register);
        username = (StoriesEditText) findViewById(R.id.username);
        email  = (StoriesEditText) findViewById(R.id.email);
        pass = (StoriesEditText) findViewById(R.id.pass);
        passVerify = (StoriesEditText) findViewById(R.id.passVerify);

        final RegisterActivity me = this;
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = me.getCurrentFocus();

                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (validateForm()) {
                    AsyncTryRegister();
                }

            }
        });
    }

    private boolean validateForm() {
        if (username.getText().toString().equals("")) {

            showSnack(getString(R.string.no_username), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (email.getText().toString().equals("")) {

            showSnack(getString(R.string.no_email), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        // !TextUtils.isEmpty(email.getText()) && android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText()).matches();

        if (pass.getText().toString().equals("") || passVerify.getText().toString().equals("")) {

            showSnack(getString(R.string.no_pass), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        if (!pass.getText().toString().equals(passVerify.getText().toString())) {

            showSnack(getString(R.string.pass_not_equal), Snackbar.LENGTH_SHORT, getString(R.string.dismiss));
            return false;
        }

        return true;
    }
    private void successfulRegister(String username, int userID) {
        //session.logIn(username, userID);
        startNextActivity();
    }

    private void startNextActivity() {
        Intent it = new Intent(RegisterActivity.this, LoginActivity.class);
        it.putExtra(NAME_OF_PUT_EXTRA_PARAM_REG, true);
        startActivity(it);
        finish();
    }

    private void AsyncTryRegister() {

        // Create the client, and instantiate an inner class as listener
        AsyncGetRequest client = new AsyncGetRequest(context, new AsyncRequestListener() {

            @Override
            public void onRemoteCallComplete(String jsonFromNet) {

                JSONObject json = null;
                try {

                    if (jsonFromNet != null) {
                        if (!jsonFromNet.equals(StoryServerURLs.USER_TAKEN)) {
                            json = new JSONObject(jsonFromNet);
                            int userID = json.getInt(StoryServerURLs.USER_USER_ID);
                            successfulRegister(username.getText().toString(), userID);

                        } else {
                            showSnack(getString(R.string.username_taken), Snackbar.LENGTH_INDEFINITE, getString(R.string.dismiss));
                        }
                    } else {
                        showSnack(getString(R.string.no_network), Snackbar.LENGTH_INDEFINITE, getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AsyncTryRegister();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("MYAPP", "unexpected JSON exception", e);
                }
            }
        });

        // Execute the call
        client.execute(StoryServerURLs.getRegisterdURL(username.getText().toString(), pass.getText().toString(), email.getText().toString()));
    }
}
