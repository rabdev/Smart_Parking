package hu.bitnet.smartparking;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Attila on 2017.11.01..
 */

public class DetectActivity extends IntentService {

    public DetectActivity() {
        super("DetectActivity");
    }

    public DetectActivity(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e( "ActivityRecogition", "In Vehicle: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Autózol");
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e( "ActivityRecogition", "On Bicycle: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Biciklizel");
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e( "ActivityRecogition", "On Foot: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Gyalog vagy");
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Futsz");
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Állsz");
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e( "ActivityRecogition", "Tilting: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "fogalmam sincs");
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    Toast.makeText(getApplicationContext(), "AUTÓ", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "sétálsz");
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you walking?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    break;
                }
            }
        }
    }
}
