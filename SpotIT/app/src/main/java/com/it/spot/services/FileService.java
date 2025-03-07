package com.it.spot.services;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.it.spot.common.Constants;
import com.it.spot.maps.main.SavedSpot;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Bogdan on 22/04/2016.
 */
public class FileService {

	private Context mContext;

	public FileService(Context context) {
		this.mContext = context;
	}

	public void writeSavedSpotFile(SavedSpot spot, String filename) {

		try {
			FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);

			Gson gson = new Gson();
			String jsonString = gson.toJson(spot);

			fos.write(jsonString.getBytes());

			fos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error writing saved spot to file: " + filename);
		}
	}

	public SavedSpot readSavedSpotFile(String filename) {

		SavedSpot result = null;

		try {
			FileInputStream fis = mContext.openFileInput(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			Gson gson = new Gson();
			result = gson.fromJson(reader, SavedSpot.class);

			reader.close();
			fis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error reading saved spot from file: " + filename);
		}

		return result;
	}

	public void writeUserListFile(List<String> userList, String filename) {

		try {
			FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);

			Gson gson = new Gson();
			String jsonString = gson.toJson(userList);

			fos.write(jsonString.getBytes());

			fos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error writing user list to file: " + filename);
		}
	}

	public List<String> readUserListFile(String filename) {

		List<String> result = null;

		try {
			FileInputStream fis = mContext.openFileInput(filename);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			Gson gson = new Gson();
			String[] userArray = gson.fromJson(reader, String[].class);

			result = new ArrayList<>();
			result.addAll(Arrays.asList(userArray));

			reader.close();
			fis.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.d(Constants.APP + Constants.SAVED_SPOT, "Error reading user list from file: " + filename);
		}

		return result;
	}
}
