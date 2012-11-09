package android.projectA;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpException;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class OlyDownload extends IntentService{
	final static String TAG = "OlyDownload";
	public final static String URL = "url";
	public final static String ACT_NAME = "DOWNLOAD_PROGRESS_ACTION";

	public OlyDownload() {
		// TODO 自動生成されたコンストラクター・スタブ
		super(TAG);
	}

	/**
	 * Intentに予め"URL"パラメータを設定しておくこと。
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		try {

			Bundle bundle = intent.getExtras();
			if(bundle == null){
				Log.d(TAG, "bundle == null");
				return;
			}
			String urlString = bundle.getString(URL);

			// HTTP Connection
			URL url = new URL(urlString);
			String fileName = getFilenameFromURL(url);
			Log.d(TAG, fileName);
			URLConnection conn = url.openConnection();

			HttpURLConnection httpConn = (HttpURLConnection)conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			Log.d(TAG, "HTTP Connect Success");
			int response = httpConn.getResponseCode();

			// Check Response
			switch(response){
				case HttpURLConnection.HTTP_OK:
					Log.d(TAG, "Status OK");
					break;
				case HttpURLConnection.HTTP_NOT_FOUND:
					Log.d(TAG, "File Not Found exception");
					throw new HttpException("File Not Found");
				case HttpURLConnection.HTTP_CLIENT_TIMEOUT:
					Log.d(TAG, "Client timeout exception");
					throw new HttpException("connection timeout");
				default :
					throw new HttpException("other http error status.[error code:"+response+"]");
			}

			int contentLength = httpConn.getContentLength();
			Log.d(TAG, "contentLength => "+contentLength);

			InputStream in = httpConn.getInputStream();
			DataInputStream dis = new DataInputStream(in);

			FileOutputStream os = this.openFileOutput(fileName, MODE_WORLD_READABLE);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

			// Read Data
			byte[] b= new byte[4096];
			int rb, tb = 0;

			while(-1 != (rb = dis.read(b))){
				dos.write(b, 0, rb);
				tb += rb;
				this.sendProgressBroadcast(contentLength, tb, fileName);
			}

			dis.close();
			dos.close();

			if(contentLength < 0){
				this.sendProgressBroadcast(tb, tb, fileName);
			}
			//Log.d(TAG, "File Saved.");
		} catch (IOException e) {
			Log.d(TAG, "IOException");
			Log.d(TAG, e.getLocalizedMessage());
		} catch (HttpException e) {
			Log.d(TAG, "HttpException");
			Log.d(TAG, e.getLocalizedMessage());
		}
	}

	protected void sendProgressBroadcast(int contentLength, int totalByte, String filename){
		Intent progress = new Intent();
		String mimetype =MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this.getFileStreamPath(filename).getPath()));
		int completePercent = contentLength < 0 ? -1 : ((totalByte * 1000) / (contentLength * 10));
		Log.d(TAG, "mimetype => "+mimetype);
		Log.d(TAG, "completePercent = " + completePercent);
		Log.d(TAG, "totalByte = " + totalByte);
		Log.d(TAG, "fileName = " + filename);

		progress.putExtra("mimetype", mimetype);
		progress.putExtra("completePercent", completePercent);
		progress.putExtra("totalByte", totalByte);
		progress.putExtra("filename", filename);
		progress.setAction(ACT_NAME);
		this.getBaseContext().sendBroadcast(progress);
	}

	protected String getFilenameFromURL(URL url){
		String[] p = url.getFile().split("/");
		String s = p[p.length-1];
		if(s.indexOf("?") > -1){
			return s.substring(0, s.indexOf("?"));
		}
		return s;
	}
}
