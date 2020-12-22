package com.example.mouseremote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    public static final int PORT = 8855;
    private String host = "192.168.1.84";
    Socket socket;
    ObjectOutputStream objectOutputStream;
    Date date;
    long previousTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        date = new Date();
        previousTime = date.getTime();

        Button leftClick = findViewById(R.id.buttonLeft);
        leftClick.setOnClickListener(v -> {
            try {
                sendMessage(new Message(0,0,true, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Button space = findViewById(R.id.buttonSpace);
        space.setOnClickListener(v -> {
            try {
                sendMessage(new Message(0,0,false, true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener((angle, strength) -> {
            long currentTime = date.getTime();
            boolean shouldSend = true; //currentTime - previousTime > 500;
            if (shouldSend && angle != 0) {
                boolean xTolerance = (angle >= 60 && angle <= 120) || (angle >= 240 && angle <= 300);
                boolean yTolerance = (angle >= 320 || angle <= 30) || (angle >= 160 && angle <= 210);
                int x = xTolerance ? 0 :
                        (angle >= 0 && angle <= 90) || (angle <= 360 && angle >= 270) ? 1 : -1;
                int y = yTolerance ? 0 :
                        angle >= 0 && angle <= 180 ? -1 : 1;

                Log.i("ANGLE", String.valueOf(angle));
                Log.i("X", String.valueOf(x));
                Log.i("Y", String.valueOf(y));
                Log.i("xTolerance", String.valueOf(xTolerance));
                Log.i("yTolerance", String.valueOf(yTolerance));

                Message message = new Message(x, y, false, false);
                try {
                    sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                previousTime = currentTime;
            }
        });

        EditText ipAddress = findViewById(R.id.ipAddress);
        ipAddress.setText(host);
        ipAddress.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                Toast.makeText(MainActivity.this, ipAddress.getText(), Toast.LENGTH_SHORT).show();
                host = ipAddress.getText().toString().trim();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            objectOutputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(final Message message) throws ExecutionException, InterruptedException {
        MyTask task = new MyTask(message);
        ConnectivityManager cm = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        task.execute().get();
    }

    private class MyTask extends AsyncTask<Void, Void, String> {
        Message message;

        public MyTask(Message message) {
            this.message = message;
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                Socket socket = new Socket(InetAddress.getByName(host), PORT);
                System.out.println("Connected!");

                // get the output stream from the socket.
                // create an object output stream from the output stream so we can send an object through it
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                objectOutputStream.writeObject(Collections.singletonList(message));

                objectOutputStream.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            return "task finished";
        }
    }
}
