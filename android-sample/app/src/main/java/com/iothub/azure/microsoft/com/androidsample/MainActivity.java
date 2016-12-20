package com.iothub.azure.microsoft.com.androidsample;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.iothub.IotHubClientProtocol;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static String connString = "[device connection string]";

    //public static IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
    public static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private MainActivity activity;
    Context context;
    Button btnSendMessage;
    TextView sendMessage;
    TextView receiveMessage;
    boolean isStartingToSend = false;
    private SendMessageAsyncTask sendTask;
    TextView seekbarLightText;
    TextView seekbarDepreciationText;
    SeekBar seekbarLight;
    SeekBar seekbarDepreciation;
    int light;
    double depreciation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        findAllViews();
    }

    private void findAllViews() {
        btnSendMessage = (Button) findViewById(R.id.btnSend);
        sendMessage = (TextView) findViewById(R.id.textSend);
        receiveMessage = (TextView) findViewById(R.id.textReceive);
        seekbarLightText = (TextView) findViewById(R.id.seekbarLightText);
        seekbarDepreciationText = (TextView) findViewById(R.id.seekbarDepreciationText);
        seekbarLight = (SeekBar) findViewById(R.id.seekbarLight);
        seekbarLight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveAndUpdateLight(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveAndUpdateLight(seekBar.getProgress());
            }
        });

        saveAndUpdateLight(seekbarLight.getProgress());

        seekbarDepreciation = (SeekBar) findViewById(R.id.seekbarDepreciation);
        seekbarDepreciation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                double depreciation = (double)progress / 100;
                saveAndUpdateDepreciation(depreciation);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double depreciation = (double)seekBar.getProgress() / 100;
                saveAndUpdateDepreciation(depreciation);
            }
        });

        getAndUpdateDepreciation();
    }

    private void saveAndUpdateLight(int light) {
        seekbarLightText.setText(getResources().getString(R.string.light)+": "+light);
        setLight(light);
    }

    private void saveAndUpdateDepreciation(double depreciation) {
        seekbarDepreciationText.setText(getResources().getString(R.string.depreciation)+": "+ depreciation);
        setDepreciation(depreciation);
    }

    private void getAndUpdateDepreciation() {
        double depreciation = (double)seekbarDepreciation.getProgress() / 100;
        saveAndUpdateDepreciation(depreciation);
    }

    @Override
    protected void onDestroy() {
        stopSendMessage();

        super.onDestroy();
    }

    public void btnSendOnClick(View v) {
        if (!isStartingToSend)
            sendMessage();
        else
            stopSendMessage();
    }

    private void sendMessage() {
        isStartingToSend = true;

        sendTask = new SendMessageAsyncTask(this, new SendMessageAsyncTask.ProgressUpdateCallback() {
            @Override
            public void callback(ArrayList<String> values) {
                String key = values.get(0);
                String value = values.get(1);
                if (key.equals("SEND"))
                    sendMessage.setText(value);
                else if (key.equals("RECEIVE")) {
                    receiveMessage.setText(value);

                    Toast.makeText(getContext(),
                            value,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        sendTask.execute();

        btnSendMessage.setText("Stop");
    }

    private Context getContext() {
        return this.context;
    }

    private void stopSendMessage() {
        isStartingToSend = false;

        if (sendTask != null && sendTask.getStatus() != AsyncTask.Status.FINISHED)
            sendTask.cancel(true);

        sendMessage.setText(sendMessage.getText() + "(stopped)");
        btnSendMessage.setText("Run");
    }

    public double getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(double depreciation) {
        this.depreciation = depreciation;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }
}
