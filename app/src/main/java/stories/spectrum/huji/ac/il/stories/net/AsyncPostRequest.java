package stories.spectrum.huji.ac.il.stories.net;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.view.ContextThemeWrapper;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;
import stories.spectrum.huji.ac.il.stories.R;


public class AsyncPostRequest  extends AsyncTask<String, Void, String> {

    private static OkHttpClient client = new OkHttpClient();
    private ProgressDialog progressDialog;
    private AsyncRequestListener asyncRequestListener;
    private static Context curContext;
    private static ArrayList<File> files;


    public AsyncPostRequest(Context context, AsyncRequestListener listener, ArrayList<File> filesToUpload) {
        client = new OkHttpClient.Builder()
            .connectTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(StoryServerURLs.TIME_OUT_SECONDS, TimeUnit.SECONDS)
            .build();

        this.asyncRequestListener = listener;
        curContext = context;
        files = filesToUpload;

        progressDialog = new ProgressDialog(new ContextThemeWrapper(curContext, R.style.progressDialogStories));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(curContext.getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    private static String connect(String url) throws IOException {

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (int i = 0; i < files.size(); i++) {
            builder.addFormDataPart("file" + i, files.get(i).getName(), RequestBody.create(MediaType.parse(getMimeType(Uri.fromFile(files.get(i)))), files.get(i)));
        }

        // Add if need body fields
        //.addFormDataPart("other_field", "other_field_value")

        RequestBody formBody = builder.build();

        Request request = new Request.Builder().url(url).post(formBody).build();

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
            return null;
        }

    }

    @Override
    protected void onPostExecute(String string) {
        asyncRequestListener.onRemoteCallComplete(string);
        progressDialog.dismiss();
    }


    private static String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = curContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
}

