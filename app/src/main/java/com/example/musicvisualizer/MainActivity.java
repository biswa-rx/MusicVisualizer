package com.example.musicvisualizer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    Visualizer visualizer;
    GraphView graphView;
    private static final String TAG = "MainActivity";
    byte[] fftSamples = new byte[1024];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphView = (GraphView) findViewById(R.id.graph);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mediaPlayer = MediaPlayer.create(this, R.raw.tokio_drift);

        visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                // Handle the waveform data (optional)
//                System.out.println(samplingRate+"Piyush wav"+waveform.length);
//                updateSpectrumGraph(waveform,graphView);
//                fftSamples = waveform;
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
//                updateSpectrumGraph(fft,graphView);
//                System.out.println(samplingRate+"Piyush fft"+fft.length);
                fftSamples = fft;

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        visualizer.setEnabled(true);
        mediaPlayer.start();
//        visualizer.getCaptureSize();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
//                    if(mediaPlayer.isPlaying()){
                    SystemClock.sleep(80);
                        updateSpectrumGraph(fftSamples,graphView);
//                    }

                }
            }
        }).start();
    }


    double[] previousData = new double[10000];
    private void updateSpectrumGraph(byte[] samples, GraphView graphView) {
        double[] fftSamples = new double[samples.length / 2];
        for (int i = 0; i < fftSamples.length; i++) {
            int sample = (samples[2 * i + 1] << 8) | (samples[2 * i] & 0xff);
            fftSamples[i] = Math.abs(sample);
        }

        DataPoint[] frequencyDomain = new DataPoint[fftSamples.length];
        for(int dataIndex=0;dataIndex<fftSamples.length;dataIndex++){
            frequencyDomain[dataIndex] = new DataPoint(dataIndex,fftSamples[dataIndex]);
//            previousData[dataIndex] = Math.max(samples[dataIndex],previousData[dataIndex]-100);
        }
        frequencyDomain[0] = new DataPoint(0,Short.MAX_VALUE);
        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(frequencyDomain);
        graphView.removeAllSeries();
        graphView.addSeries(series);

//        System.out.println(frequencyDomain.length);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        visualizer.setEnabled(false);
        visualizer.release();
    }
}