package stories.spectrum.huji.ac.il.stories.recorder;

import android.app.Activity;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import stories.spectrum.huji.ac.il.stories.R;

public class RecorderComponent {

    public View.OnTouchListener onTouchListener = null;
    private Activity activity;
    private TextView recordTimeText;
    private ImageButton audioSendButton;
    private View recordPanel;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private long startTime = 0L;
    private Timer timer;
    private MediaRecorder mRecorder;
    private String audioFilePath = "";
    private String audioFileExt = ".3gp";
    public File lastRecordedFile = null;
    View slideText = null;

    public RecorderComponent(Activity activity, View view) {
        // Set Activity
        this.activity = activity;

        // Record to the external cache directory for visibility
        audioFilePath = activity.getExternalCacheDir().getAbsolutePath() + "/audiorecordtest";

        recordPanel = view.findViewById(R.id.record_panel);
        recordTimeText = (TextView) view.findViewById(R.id.recording_time_text);
        slideText = view.findViewById(R.id.slideText);
        if (slideText != null) {
            slideText.setVisibility(View.GONE);
        }
        audioSendButton = (ImageButton) view.findViewById(R.id.chat_audio_send_button);
        TextView textView = (TextView) view.findViewById(R.id.slideToCancelTextView);
        textView.setText("החלק לביטול");

        onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                            .getLayoutParams();
                    params.leftMargin = dp(30);
                    slideText.setLayoutParams(params);
                    ViewProxy.setAlpha(slideText, 1);
                    startedDraggingX = -1;
                    // startRecording();
                    startRecording();
                    audioSendButton.getParent()
                            .requestDisallowInterceptTouchEvent(true);
                    recordPanel.setVisibility(View.VISIBLE);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    startedDraggingX = -1;
                     stopRecording(false);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = motionEvent.getX();
                    if (x < -distCanMove) {
                         stopRecording(true);
                    }
                    x = x + ViewProxy.getX(audioSendButton);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideText
                            .getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        params.leftMargin = dp(30) + (int) dist;
                        slideText.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        ViewProxy.setAlpha(slideText, alpha);
                    }
                    if (x <= ViewProxy.getX(slideText) + slideText.getWidth()
                            + dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (recordPanel.getMeasuredWidth()
                                    - slideText.getMeasuredWidth() - dp(48)) / 2.0f;
                            if (distCanMove <= 0) {
                                distCanMove = dp(80);
                            } else if (distCanMove > dp(80)) {
                                distCanMove = dp(80);
                            }
                        }
                    }
                    if (params.leftMargin > dp(30)) {
                        params.leftMargin = dp(30);
                        slideText.setLayoutParams(params);
                        ViewProxy.setAlpha(slideText, 1);
                        startedDraggingX = -1;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        };
    }

    private void startRecordingToFile() {
        String currentFileName  = getNewFileName();
        lastRecordedFile = new File(currentFileName);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(currentFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("RECORDER", "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecordingToFile(boolean isCanceled) {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

        if (isCanceled) {
            if (lastRecordedFile != null) {
                lastRecordedFile.delete();
                lastRecordedFile = null;
            }
        }
    }

    private void startRecording() {
        // TODO Auto-generated method stub
        if (slideText != null) {
            slideText.setVisibility(View.VISIBLE);
        }

        startTime = SystemClock.uptimeMillis();
        timer = new Timer();
        MyTimerTask myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 1000, 1000);
        vibrate();
        recordTimeText.setText("00:00");
        startRecordingToFile();
    }

    private void stopRecording(boolean isCanceled) {
        // TODO Auto-generated method stub
        if (slideText != null) {
            slideText.setVisibility(View.GONE);
        }

        if (timer != null) {
            timer.cancel();
        }
        if (recordTimeText.getText().toString().equals("00:00")) {
            return;
        }

        if (isCanceled) {
            recordTimeText.setText("00:00");
        }
        vibrate();
        //Toast.makeText(activity, "Is: " + isCanceled, Toast.LENGTH_SHORT).show();
        stopRecordingToFile(isCanceled);
    }

    private void vibrate() {
        // TODO Auto-generated method stub
        try {
            Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    private class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            long timeInMilliseconds = 0L;
            long timeSwapBuff = 0L;
            long updatedTime = 0L;

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            final String hms = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(updatedTime)
                            - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
                            .toHours(updatedTime)),
                    TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                            .toMinutes(updatedTime)));
            long lastsec = TimeUnit.MILLISECONDS.toSeconds(updatedTime)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                    .toMinutes(updatedTime));
            System.out.println(lastsec + " hms " + hms);
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (recordTimeText != null)
                            recordTimeText.setText(hms);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                }
            });
        }
    }

    private String getNewFileName() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
       return audioFilePath + ts + audioFileExt;
    }
}
