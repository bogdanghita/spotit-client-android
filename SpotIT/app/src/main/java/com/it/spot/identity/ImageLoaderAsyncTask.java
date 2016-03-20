package com.it.spot.identity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.it.spot.R;
import com.it.spot.common.Constants;

import java.io.InputStream;

/**
 * Created by Bogdan on 20/03/2016.
 */
public class ImageLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

	ImageView imageView;

	public ImageLoaderAsyncTask(ImageView imageView) {
		this.imageView = imageView;
	}

	protected Bitmap doInBackground(String... urls) {

		Bitmap bitmap = null;

		try {
			InputStream inputStream = new java.net.URL(urls[0]).openStream();
			bitmap = BitmapFactory.decodeStream(inputStream);
		}
		catch (Exception e) {
			Log.d(Constants.APP + Constants.SIGN_IN, "Unable to load image.");
			e.printStackTrace();
		}

		return bitmap;
	}

	protected void onPostExecute(Bitmap result) {

		if (result != null) {
			imageView.setImageBitmap(result);
		}
		else {
			Drawable drawable = ContextCompat.getDrawable(imageView.getContext(), R.drawable.ic_account_circle_white_48dp);
			imageView.setImageDrawable(drawable);
		}
	}
}