package de.berlin.polizei.testanmeldung;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.security.identity.PersonalizationData;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String TOKEN_TYPE_ID = "TOKEN_TYPE_ID";
    public static final String TOKEN_TYPE_ACCESS = "TOKEN_TYPE_ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "TOKEN_TYPE_REFRESH";
    public static final String ACCOUNT_TYPE = "SSO";

    public Button login_btn;
    public Button logout_btn;
    public Button access_btn;

    public TextView txtView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_btn = (Button) findViewById(R.id.login_btn);
        logout_btn = (Button) findViewById(R.id.logout_btn);
        access_btn = (Button) findViewById(R.id.access_btn);
        txtView = (TextView) findViewById(R.id.result);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getLoginIntent();
                startActivityForResult(i,1);
            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getLogoutIntent();
                startActivityForResult(i,2);
            }
        });

        access_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAuthToken();
            }
        });
    }

    @Override
    public void onNewIntent(Intent data)
    {
        super.onNewIntent(data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case 1:
                Log.i(TAG,data.getStringExtra("id_token"));
                Toast.makeText(MainActivity.this,"Login ok",Toast.LENGTH_SHORT).show();

                String CHANNEL_ID = "ForegroundChannelId";
                String CHANNEL_NAME = "ForegroundChannelName";

                Intent notificationIntent = getLogoutIntent();

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                PendingIntent pendingIntent = null;
                // Create a NotificationChannel for Android Oreo and higher versions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("Foreground Notification Channel");
                    channel.enableLights(true);
                    channel.setLightColor(Color.BLUE);
                    notificationManager.createNotificationChannel(channel);
                    pendingIntent =
                            PendingIntent.getActivity(this, 0, notificationIntent,
                                    PendingIntent.FLAG_IMMUTABLE| PendingIntent.FLAG_ONE_SHOT);
                }
                else
                {
                    pendingIntent =
                            PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_ONE_SHOT);
                }

                // Build the notification
                Notification notification = new Notification.Builder(MainActivity.this, CHANNEL_ID)
                        .setContentTitle("Abmeldung")
                        .setContentText("Abmelden")
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .build();

                notificationManager.notify(123,notification);



            break;

            case 2:
                Toast.makeText(MainActivity.this,"Logout ok",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private Intent getLoginIntent() {
        return new Intent("de.mopsdom.adfs.LOGIN_START");
    }

    private Intent getLogoutIntent() {
        return new Intent("de.mopsdom.adfs.LOGOUT_START");
    }

    private void getAuthToken() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

        if (accounts!=null)
        {
            Account acc = accounts[0];

            accountManager.getAuthToken(acc, TOKEN_TYPE_ACCESS, null, true, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Bundle futureResult = future.getResult();

                        if (futureResult.keySet().contains(AccountManager.KEY_AUTHTOKEN)) {
                            String token = futureResult.getString(AccountManager.KEY_AUTHTOKEN);

                            txtView.setText(token);

                            Toast.makeText(MainActivity.this,"Token bekommen! :)",Toast.LENGTH_SHORT).show();
                        } else {
                            Intent i = (Intent) futureResult.get(AccountManager.KEY_INTENT);
                            startActivityForResult(i,1);

                            Toast.makeText(MainActivity.this,"Login nötig :/",Toast.LENGTH_SHORT).show();
                        }
                    } catch (AuthenticatorException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    } catch (OperationCanceledException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }, null);
        }
        else
        {
            Toast.makeText(MainActivity.this,"Login nötig :/",Toast.LENGTH_SHORT).show();
            login_btn.callOnClick();
        }
    }
}