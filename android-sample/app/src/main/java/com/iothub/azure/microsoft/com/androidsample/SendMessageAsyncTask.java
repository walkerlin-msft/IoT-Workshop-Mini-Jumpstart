package com.iothub.azure.microsoft.com.androidsample;

import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubMessageResult;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by a-walin on 2016/12/14.
 */

public class SendMessageAsyncTask extends AsyncTask<Void, ArrayList<String>, Void> {

    private static final String TAG = "SendMessageAsyncTask";

    MainActivity activity;
    ProgressUpdateCallback progressUpdateCallback;

    public SendMessageAsyncTask(MainActivity activity, ProgressUpdateCallback progressUpdateCallback) {
        this.activity = activity;
        this.progressUpdateCallback = progressUpdateCallback;
    }

    private DeviceClient createDeviceClient() {

        try {
            DeviceClient client = new DeviceClient(MainActivity.connString, MainActivity.protocol);

            if (MainActivity.protocol == IotHubClientProtocol.MQTT)
            {
                MessageCallbackMqtt callback = new MessageCallbackMqtt();
                Counter counter = new Counter(0);
                client.setMessageCallback(callback, counter);
}
        else
        {
        MessageCallback callback = new MessageCallback();
        Counter counter = new Counter(0);
        client.setMessageCallback(callback, counter);
        }

        return client;
        } catch (URISyntaxException e) {
        Log.e(TAG, "URISyntaxException e="+e.getMessage());
        } catch(Exception e) {
        Log.e(TAG, "Exception e="+e.getMessage());
        }

        return null;
        }

        @Override
        protected Void doInBackground(Void... longs) {

            DeviceClient client = createDeviceClient();

            try {
                client.open();

                int i = 1;
                while (i > 0)
                {
                    if(isCancelled()) {
                        client.close();
                        return null;
                    }

                    String msgStr = "Message " + Integer.toString(i);
                    //  Get UI values
                    JSONObject json= new JSONObject();
                    json.put("WindSpeed", this.activity.getWindSpeed());
                    float depreciation = this.activity.getDepreciation();
                    Log.d(TAG, "depreciation="+depreciation);
                    //depreciation = (float)(Math.round(depreciation*100)/100);
                    //Log.d(TAG, "depreciation2="+depreciation);
                    json.put("Depreciation", depreciation);
                    json.put("MesgId", msgStr);

                    try
                    {
                    Message msg = new Message(json.toString());
                    msg.setProperty("messageCount", Integer.toString(i));

                    EventCallback eventCallback = new EventCallback();
                    client.sendEventAsync(msg, eventCallback, i);

                    publishMessage("SEND", json.toString());
                    }
                    catch (Exception e)
                    {
                    }
                    try {
                    Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    e.printStackTrace();
                    }

                    i++;
                    }
            } catch (IOException e) {
            Log.e(TAG, "IOException e="+e.getMessage());
            } catch(Exception e) {
            Log.e(TAG, "Exception e="+e.getMessage());
            }

            return null;
        }

@Override
protected void onProgressUpdate(ArrayList<String>... values) {
        super.onProgressUpdate(values);

        if(this.progressUpdateCallback != null)
        this.progressUpdateCallback.callback(values[0]);
        }

protected class EventCallback implements IotHubEventCallback {
    public void execute(IotHubStatusCode status, Object context){
        Integer i = (Integer) context;
        Log.d(TAG, "IoT Hub responded to message "+i.toString()
                + " with status " + status.name());
    }
}

// Our MQTT doesn't support abandon/reject, so we will only display the messaged received
// from IoTHub and return COMPLETE
class MessageCallbackMqtt implements com.microsoft.azure.iothub.MessageCallback
{
    public IotHubMessageResult execute(Message msg, Object context)
    {
        Counter counter = (Counter) context;
        String content = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
        String mesg = "Received message " + counter.toString()
                + " with content: " + content;
        Log.d(TAG, "MessageCallbackMqtt "+mesg);

        publishMessage("RECEIVE", content);

        counter.increment();
        return IotHubMessageResult.COMPLETE;
    }
}

protected class MessageCallback implements com.microsoft.azure.iothub.MessageCallback
{
    public IotHubMessageResult execute(Message msg, Object context)
    {
        Counter counter = (Counter) context;
        String content = new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET);
        String mesg = "Received message " + counter.toString()
                + " with content: " + content;
        Log.d(TAG, "MessageCallback "+mesg);

        int switchVal = counter.get() % 3;
        IotHubMessageResult res;
        switch (switchVal)
        {
            case 0:
                res = IotHubMessageResult.COMPLETE;
                break;
            case 1:
                res = IotHubMessageResult.ABANDON;
                break;
            case 2:
                res = IotHubMessageResult.REJECT;
                break;
            default:
                // should never happen.
                throw new IllegalStateException("Invalid message result specified.");
        }

        Log.d(TAG, "Responding to message " + counter.toString() + " with " + res.name());

        publishMessage("RECEIVE", content+" with "+res.name());

        counter.increment();

        return res;
    }
}

    private void publishMessage(String key, String value) {
        ArrayList<String> values = new ArrayList<String>();
        values.add(key);
        values.add(value);
        publishProgress(values);
    }

/** Used as a counter in the message callback. */
protected static class Counter
{
    protected int num;

    public Counter(int num)
    {
        this.num = num;
    }

    public int get()
    {
        return this.num;
    }

    public void increment()
    {
        this.num++;
    }

    @Override
    public String toString()
    {
        return Integer.toString(this.num);
    }
}

public interface ProgressUpdateCallback {
    void callback(ArrayList<String> values);
}
}
