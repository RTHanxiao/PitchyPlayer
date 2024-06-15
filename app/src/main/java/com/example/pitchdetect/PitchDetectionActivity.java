package com.example.pitchdetect;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class PitchDetectionActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 44100;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private ImageView startButton;
    private TextView pitchTextView;
    private TextView noteTextView;
    private Thread recordingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pitchdetect);

        // Hide ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        startButton = findViewById(R.id.imageView2);
        pitchTextView = findViewById(R.id.pitchTextView);
        noteTextView = findViewById(R.id.noteTextView);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void startRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int powerOfTwoBufferSize = getNextPowerOfTwo(bufferSize);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, powerOfTwoBufferSize);
        audioRecord.startRecording();
        isRecording = true;
        startButton.setImageResource(R.drawable.ic_outline_pause_56); // Change button icon to stop

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                detectPitch();
            }
        });
        recordingThread.start();
    }

    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            startButton.setImageResource(R.drawable.ic_outline_play_arrow_56); // Change button icon to start
        }
    }

    private void detectPitch() {
        //读入buffer李德音频信息
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int powerOfTwoBufferSize = getNextPowerOfTwo(bufferSize);
        short[] buffer = new short[powerOfTwoBufferSize];
        double[] audioData = new double[powerOfTwoBufferSize];
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

        while (isRecording) {
            audioRecord.read(buffer, 0, bufferSize);

            // 转换与0填充
            for (int i = 0; i < bufferSize; i++) {
                audioData[i] = buffer[i];
            }
            for (int i = bufferSize; i < powerOfTwoBufferSize; i++) {
                audioData[i] = 0.0;
            }

            // 准备FFT，转换数据
            Complex[] complexData = fft.transform(audioData, TransformType.FORWARD);

            // 找到峰值频率
            double maxMagnitude = -1;
            int maxIndex = -1;
            for (int i = 0; i < complexData.length / 2; i++) {
                double magnitude = complexData[i].abs();
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                    maxIndex = i;
                }
            }

            // 计算频率
            // 采样率/缓存大小 = 每个峰值的最小间隔
            //因为buffer足够小，不考虑极端情况，一段buffer一般只会有一个峰值
            //峰值索引*分辨率即为每个峰值的时间间隔即为频率
            final double pitch = maxIndex * SAMPLE_RATE / powerOfTwoBufferSize;
            final String note = frequencyToNoteName(pitch);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pitchTextView.setText(String.format("Pitch: %.2f Hz", pitch));
                    noteTextView.setText(String.format("Note: %s", note));
                }
            });
        }
    }

    //频率转化为音高
    private String frequencyToNoteName(double frequency) {
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        //标准音
        double A4 = 440.0;
        //音高与频率是对数关系，计算当前频率与标准音的比值，乘12是因为12平均律.
        //频率没大一倍，音高则高一个八度，一个八度内有12个音符
        //由于这里是对标准音A4进行对比，A4为第69个音符，故编号时+69
        //这里最终得到当前时第几个音符
        int noteNumber = (int) Math.round(12 * Math.log(frequency / A4) / Math.log(2)) + 69;
        //计算是第几个八度
        int octave = noteNumber / 12 - 1;
        //计算是当前八度的第几个音符
        int noteIndex = noteNumber % 12;
        //组合
        return noteNames[noteIndex] + octave;
    }

    //计算2的N次，以便近似计算log2
    private int getNextPowerOfTwo(int number) {
        int power = 1;
        while (power < number) {
            power *= 2;
        }
        return power;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}
