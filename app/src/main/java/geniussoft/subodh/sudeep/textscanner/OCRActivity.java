package geniussoft.subodh.sudeep.textscanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import codeville.rashmi.sudeep.paperscan.R;

public class OCRActivity extends ActionBarActivity {
    /* Some of the code is taken from the Gautam Gupta's ocr project
    here is the link to that project : https://github.com/GautamGupta/Simple-Android-OCR
    Thanks
     */

	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/TextScanner/";
	
	// You should have the trained data file in assets folder
	// You can get them at:
	// http://code.google.com/p/tesseract-ocr/downloads/list
	public static final String lang = "eng";
	private static final String TAG = "textscanner.java";

	protected Button _button,button_gal,bt_save,bt_share;
	// protected ImageView _image;
	protected EditText _field;
	protected String _path;
	protected boolean _taken;
    private ListView list;
    boolean loading = true;
    boolean galleryselected = false;
    public static final String base = "http://access.alchemyapi.com/calls/text/TextGetRankedNamedEntities";
    public static final String key = "apikey";
    public static final String text = "text";
    public static final String mode = "outputMode";
    public static final String apikey = "YourKeyHere";
    public static final String outputMode = "json";
    URL url;

    ArrayList<String> listItems=new ArrayList<String>();
    ArrayList<String> typeItem=new ArrayList<String>();
    ArrayList<String> siteItem=new ArrayList<String>();
    ArrayAdapter<String> adapter;

	protected static final String PHOTO_TAKEN = "photo_taken";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

		for (String path : paths) {
			File dir = new File(path);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
					return;
				} else {
					Log.v(TAG, "Created directory " + path + " on sdcard");
				}
			}

		}
		
		// lang.traineddata file with the app (in assets folder)
		// You can get them at:
		// http://code.google.com/p/tesseract-ocr/downloads/list
		// This area needs work and optimization
		if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
			try {

				AssetManager assetManager = getAssets();
				InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
				//GZIPInputStream gin = new GZIPInputStream(in);
				OutputStream out = new FileOutputStream(DATA_PATH
						+ "tessdata/" + lang + ".traineddata");

				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				//while ((lenf = gin.read(buff)) > 0) {
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				in.close();
				//gin.close();
				out.close();
				
				Log.v(TAG, "Copied " + lang + " traineddata");
			} catch (IOException e) {
				Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
			}
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// _image = (ImageView) findViewById(R.id.image);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(OCRActivity.this);
        alertDialog.setTitle("Note");

        alertDialog.setMessage("Performance of application depends completely upon the quality of the image and the device processor");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();

		_field = (EditText) findViewById(R.id.field);
        _field.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent event) {
                // TODO Auto-generated method stub
                if (view.getId() ==R.id.field) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()&MotionEvent.ACTION_MASK){
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
		_button = (Button) findViewById(R.id.button);
		_button.setOnClickListener(new ButtonClickHandler());

        button_gal = (Button) findViewById(R.id.button_gallery);

        bt_save =(Button) findViewById(R.id.button_save);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File sdCard = Environment.getExternalStorageDirectory();
                File directory = new File (sdCard.getAbsolutePath() + "/PaperScan");
                directory.mkdirs();

//Now create the file in the above directory and write the contents into it
                File file = new File(directory, "mysdfile.txt");
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                OutputStreamWriter osw = new OutputStreamWriter(fOut);

                try {
                    osw.write(_field.getText().toString());
                    osw.flush();
                    osw.close();
                    Toast.makeText(getBaseContext(),
                            "Done writing SD 'mysdfile.txt'",
                            Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        bt_share = (Button)findViewById(R.id.button_share);
        bt_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, _field.getText().toString());
                startActivity(Intent.createChooser(shareIntent, "Choose one"));
            }
        });

        button_gal = (Button)findViewById(R.id.button_gallery);
        button_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(OCRActivity.this);
                alertDialog.setTitle("Alert");

                alertDialog.setMessage("Please select the Image form Gallery only. Selecting from other sources like file manager will crash the application\n\nSorry for inconvenience");
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent,
                                "Select Picture"),1);
                        galleryselected = true;
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();


            }
        });

        list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = null;
                if(!loading)
                intent = new Intent(OCRActivity.this,Details.class);
                intent.putExtra("name",listItems.get(position));
                intent.putExtra("type",typeItem.get(position));
                intent.putExtra("site",siteItem.get(position));
                startActivity(intent);
            }
        });

		_path = DATA_PATH + "/ocr.jpg";
	}

	public class ButtonClickHandler implements View.OnClickListener {
		public void onClick(View view) {
			Log.v(TAG, "Starting Camera app");
            galleryselected =  false;
			startCameraActivity();
		}
	}

	// Simple android photo capture:
	// http://labs.makemachine.net/2010/03/simple-android-photo-capture/

	protected void startCameraActivity() {
		File file = new File(_path);
		Uri outputFileUri = Uri.fromFile(file);

		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "resultCode: " + resultCode);

		if (resultCode == -1) {
            Uri selectedImageUri;
            if(galleryselected){
                selectedImageUri = data.getData();
                _path = getPath(selectedImageUri);
                Log.e(TAG,"path = "+selectedImageUri);
            }else{
                _path = DATA_PATH + "/ocr.jpg";
            }
			onPhotoTaken();

		} else {
			Log.v(TAG, "User cancelled");
		}
	}

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(OCRActivity.PHOTO_TAKEN, _taken);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.i(TAG, "onRestoreInstanceState()");
		if (savedInstanceState.getBoolean(OCRActivity.PHOTO_TAKEN)) {
			onPhotoTaken();
		}
	}

	protected void onPhotoTaken() {

		_taken = true;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );
		
		Log.v(TAG, "Before baseApi");


		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

		// You now have the text in recognizedText var, you can do anything with it.
		// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
		// so that garbage doesn't make it to the display.

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		/*if ( lang.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		//recognizedText = recognizedText.trim();*/

		if ( recognizedText.length() != 0 ) {
			_field.setText(_field.getText().toString().length() == 0 ? recognizedText : _field.getText() + " " + recognizedText);
			_field.setSelection(_field.getText().toString().length());

            Uri builtUri = Uri.parse(base)
                    .buildUpon()
                    .appendQueryParameter(key, apikey)
                    .appendQueryParameter(mode, outputMode)
                    .appendQueryParameter(text, _field.getText().toString())
                    .build();
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.e("url","url = " +url);
            new AsyncHttpTask().execute(url.toString());
            String loading[] = {"Analysing text ..."};
            Log.e(TAG,"async called");
            adapter=new ArrayAdapter<String>(OCRActivity.this,
                    android.R.layout.simple_list_item_1,loading
                    );
            list.setAdapter(adapter);

		}

        // Cycle done.
	}


    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            //setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            Integer result = 0;
            HttpURLConnection urlConnection = null;

            try {
                {
                    /* forming th java.net.URL object */
                    URL url = new URL(params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    //Log.e(TAG, response.toString());
                    parseResult(response.toString());
                    result = 1; // Successful
                }



            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            //setProgressBarIndeterminateVisibility(false);
            /* Download complete. Lets update UI */
            //result = 1;
            if (result == 1) {
                Log.e(TAG, "fetch data!");
                // searchtext.startAnimation(animup);
                adapter=new ArrayAdapter<String>(OCRActivity.this,
                        android.R.layout.simple_list_item_1,
                        listItems);
                list.setAdapter(adapter);
                loading = false;


            } else {
                Log.e(TAG, "Failed to fetch data!");
                //Toast.makeText(MainActivity.this, "Loading Failed.. Please Try again", Toast.LENGTH_LONG).show();

            }

        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            Log.e("result","json = " + result);
            JSONArray enitity = response.getJSONArray("entities");
            for (int i = 0; i < enitity.length(); i++) {
                JSONObject post = enitity.optJSONObject(i);
                String type = "";
                String title = "";
                String website = "";
                if(post.has("type"))
                    type = post.optString("type");
                if(post.has("text"))
                    title = post.optString("text");
                if(post.has("disambiguated") && post.getJSONObject("disambiguated").has("website"))
                    website= post.getJSONObject("disambiguated").optString("website");
                listItems.add(title);
                typeItem.add(type);
                siteItem.add(website);



            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
