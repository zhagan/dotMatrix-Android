package com.zackhagan.dotmatrix_a;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Thread pause = new Thread() {
			public void run() {
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					FirstRun();
				//Intent stuff = new Intent(Splash.this,
				//			Dm_beatbox.class);
				//	startActivity(stuff);
					//Intent intent = getIntent(getApplicationContext(), Dm_beatbox.class);
					//intent.setAction(Intent.ACTION_MAIN);
					//intent.addCategory(Intent.CATEGORY_LAUNCHER);
					//startActivity(intent);
					
				}
			}
		};
		pause.start();
	}
	
	 private void FirstRun() {
	        SharedPreferences settings = this.getSharedPreferences("Act_main", 0);
	        boolean firstrun = settings.getBoolean("firstrun", true);
	        
	        System.out.println(settings.getBoolean("firstrun", true));
	        
	        if (firstrun) { // Checks to see if we've ran the application b4
	        	
	            SharedPreferences.Editor e = settings.edit();
	            e.putBoolean("firstrun", false);
	            e.commit();
	            // If not, run these methods:
	            setCopyDirectory();
	            Intent home = new Intent(Splash.this, Dm_beatbox.class);
	            startActivity(home);

	        } else { // Otherwise start the application here:

	            Intent home = new Intent(Splash.this, Dm_beatbox.class);
	            startActivity(home);
	        }
	    }
	 private void setCopyDirectory(){
		// copy asset files and paste them to the demos folder
					
					String basepath = Environment.getExternalStorageDirectory()
							.toString() + "/dotMatrix";
					File maindir = new File(basepath);
					
					File demodir = new File(basepath + "/patterns/");
					if(!maindir.exists()){
				        //System.out.println(maindir);

						maindir.mkdirs();
						
					}
					if (!demodir.exists()) {
						demodir.mkdirs();
						copyPatterns();
				File sampledir = new File(basepath + "/samples");
					if (!sampledir.exists()){
						
							
							sampledir.mkdirs();
							copySamples();
					}
					}
	 }	
	 
	 private void copyPatterns() {
			String basepath = Environment.getExternalStorageDirectory().toString()
					+ "/dotMatrix";
			AssetManager assetManager = getResources().getAssets();
			String[] files = null;
			try {
				files = assetManager.list("patterns");
			} catch (Exception e) {
				Log.e("read demo ERROR", e.toString());
				e.printStackTrace();
			}
			for (int i = 0; i < files.length; i++) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open("patterns/" + files[i]);
					out = new FileOutputStream(basepath + "/patterns/" + files[i]);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) {
					Log.e("copy demos ERROR", e.toString());
					e.printStackTrace();
				}
			}
		}
	 private void copySamples() {
			String basepath = Environment.getExternalStorageDirectory().toString()
					+ "/dotMatrix";
			AssetManager assetManager = getResources().getAssets();
			String[] files = null;
			
			try {
				files = assetManager.list("samples");
			} catch (Exception e) {
				Log.e("read demo ERROR", e.toString());
				e.printStackTrace();
			}
			for (int i = 0; i < files.length; i++) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open("samples/" + files[i]);
					out = new FileOutputStream(basepath + "/samples/" + files[i]);
					copyFile(in, out);
					in.close();
					in = null;
					out.flush();
					out.close();
					out = null;
				} catch (Exception e) {
					Log.e("copy demos ERROR", e.toString());
					e.printStackTrace();
				}
			}
		}
		private void copyFile(InputStream in, OutputStream out) throws IOException {
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		}
/*
	private static Intent getIntent(Context context, Class<?> cls) {
	    Intent intent = new Intent(context, cls);
	    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    return intent;
	}
*/	



	@Override
	protected void onPause() {
		super.onPause();
		
		
		finish();
	}


}
