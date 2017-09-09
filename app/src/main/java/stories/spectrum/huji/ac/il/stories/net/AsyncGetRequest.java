package stories.spectrum.huji.ac.il.stories.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import stories.spectrum.huji.ac.il.stories.R;

public class AsyncGetRequest extends AsyncTask<String, Void, String>{

    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .build();

    private ProgressDialog progressDialog ;
    private AsyncRequestListener asyncRequestListener;
    private Context curContext;

    public AsyncGetRequest(Context context, AsyncRequestListener listener){

        this.asyncRequestListener = listener;
        curContext = context;

        progressDialog = new ProgressDialog(new ContextThemeWrapper(curContext, R.style.progressDialogStories));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(curContext.getString(R.string.loading));
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);

    }

    private static String connect(String url) throws IOException
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public void onPreExecute() {
        progressDialog.show();

    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            return connect(urls[0]);
        } catch (IOException e) {
            Log.e("##NET##", e.getMessage());
            return null;
        }

    }

    @Override
    protected void onPostExecute(String string ) {
        asyncRequestListener.onRemoteCallComplete(string);
        progressDialog.dismiss();
    }
}