package com.iothub.azure.microsoft.com.androidsample;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.iothub.IotHubClientProtocol;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static String connString = "HostName=iothubworkshop.azure-devices.net;DeviceId=AndroidDevice;SharedAccessKey=mxuTlxoqnaqyCAOoU00OY9S296FFup7oQFkHyzOwA1g=";
    //public static IotHubClientProtocol protocol = IotHubClientProtocol.HTTPS;
    public static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    private MainActivity activity;
    Context context;
    Button btnSendMessage;
    TextView sendMessage;
    TextView receiveMessage;
    boolean isStartingToSend = false;
    private SendMessageAsyncTask sendTask;
    TextView seekbarSpeedText;
    TextView seekbarDepreciationText;
    SeekBar seekbarSpeed;
    SeekBar seekbarDepreciation;
    int windSpeed;
    float depreciation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        findAllViews();


    }

    private void findAllViews() {
        btnSendMessage = (Button)findViewById(R.id.btnSend);
        sendMessage = (TextView)findViewById(R.id.textSend);
        receiveMessage = (TextView)findViewById(R.id.textReceive);
        seekbarSpeedText = (TextView)findViewById(R.id.seekbarSpeedText);
        seekbarDepreciationText = (TextView)findViewById(R.id.seekbarDepreciationText);
        seekbarSpeed = (SeekBar)findViewById(R.id.seekbarSpeed);
        seekbarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekbarSpeedText.setText("Wind Speed: "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setWindSpeed(seekBar.getProgress());
            }
        });
        setWindSpeed(seekbarSpeed.getProgress());
        seekbarDepreciation = (SeekBar)findViewById(R.id.seekbarDepreciation);
        seekbarDepreciation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Float f = (float)progress/100;
                seekbarDepreciationText.setText("Depreciation: "+f.toString());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Float f = (float)seekBar.getProgress()/100;
                setDepreciation(f.floatValue());
            }
        });
        Float f = (float)seekbarDepreciation.getProgress()/100;
        setDepreciation(f.floatValue());
    }

    @Override
    protected void onDestroy() {
        stopSendMessage();

        super.onDestroy();
    }

    public void btnSendOnClick(View v) {
        if(!isStartingToSend)
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
                if(key.equals("SEND"))
                    sendMessage.setText(value);
                else if(key.equals("RECEIVE")) {
                    receiveMessage.setText(value);

                    Toast.makeText(getContext(),
                            value,
                            Toast.LENGTH_LONG).show();
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

        if(sendTask != null && sendTask.getStatus() != AsyncTask.Status.FINISHED)
            sendTask.cancel(true);

        sendMessage.setText(sendMessage.getText()+"(stopped)");
        btnSendMessage.setText("Run");
    }

    public float getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(float depreciation) {
        this.depreciation = depreciation;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }
}
