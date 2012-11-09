package android.projectA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.projectA.R;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.*;

public class projectAActivity extends Activity {
	final static String TAG = "Main";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //TextView display = new TextView(this);
        Button button1 = (Button)findViewById(R.id.button1);

        button1.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		TextView text1 = (TextView)findViewById(R.id.textView1);
        		EditText urledit = (EditText)findViewById(R.id.editText1);

        		text1.setText(urledit.getText());

        		getContext(urledit.getText().toString());
        	}
        });

    }
    protected void getContext(String url){
		DownloadProgressBroadcastReceiver dlpbr = new DownloadProgressBroadcastReceiver();
		IntentFilter inf = new IntentFilter();
		inf.addAction("DOWNLOAD_PROGRESS_ACTION");
		registerReceiver(dlpbr, inf);
    	Intent dli = new Intent(this.getBaseContext(), OlyDownload.class);
    	dli.putExtra(OlyDownload.URL,url);
		startService(dli);
    }

	public String doPost( String url, String params ){
	    try{

	    	HttpPost method = new HttpPost( url );

	        DefaultHttpClient client = new DefaultHttpClient();

	        // POST データの設定
	        StringEntity paramEntity = new StringEntity( params );
	        paramEntity.setChunked( false );
	        paramEntity.setContentType( "application/x-www-form-urlencoded" );
	        method.setEntity( paramEntity );

	        HttpResponse response = client.execute( method );
	        int status = response.getStatusLine().getStatusCode();
	        if ( status != HttpStatus.SC_OK )
	            throw new Exception( "" );
	        return EntityUtils.toString( response.getEntity(), "UTF-8" );
	    }catch ( Exception e ){
	        return null;
	    }
	}

    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        //if (requestCode == PICK_CONTACT_REQUEST) {
         //   if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
           //     startActivity(new Intent(Intent.ACTION_VIEW, data));
         //   }
       // }
    }

    class DownloadProgressBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO 自動生成されたメソッド・スタブ
			// Show Progress
			Log.d(TAG, "braodcast receive.");
			Bundle bundle = intent.getExtras();
			int completePercent = bundle.getInt("completePercent");
			int totalByte = bundle.getInt("totalByte");
			String progressString = totalByte + " byte read.";
			if(0 < completePercent){
				progressString += "[" + completePercent + "%]";
			}
			TextView ptv = (TextView)findViewById(R.id.ProgText);
			ptv.setText(progressString);

			// If completed, show the picture.
			if(completePercent == 100){
				Log.d(TAG, "file download complete.");
				String fname = intent.getExtras().getString("filename");
				Log.d(TAG, fname);
		    	File f = context.getFileStreamPath(fname);
				Log.d(TAG,intent.getExtras().getString("mimetype"));
		    	Log.d(TAG, f.getPath());
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setDataAndType(Uri.parse("file://"+f.getPath()), intent.getExtras().getString("mimetype"));
		    	startActivity(i);
			}
		}
    }
}