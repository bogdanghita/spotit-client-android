package com.it.spot.intro;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.it.spot.R;
import com.it.spot.maps.main.MapsActivity;

public class IntroActivity extends AppIntro {
	private String directions = "directions.png";
	private String reportSpot = "report_spot.png";
	@Override
	public void init(Bundle savedInstanceState) {

		// Add your slide's fragments here.
		// AppIntro will automatically generate the dots indicator and buttons.
		addSlide(SampleSlide.newInstance(R.layout.intro_spots_slide, directions));
		addSlide(SampleSlide.newInstance(R.layout.intro_colors_slide, reportSpot));
		addSlide(SampleSlide.newInstance(R.layout.intro_report_spot_slide, reportSpot));
		addSlide(SampleSlide.newInstance(R.layout.intro_save_location_slide,directions));
		addSlide(SampleSlide.newInstance(R.layout.intro_directions_slide, directions));


		showSkipButton(false);
		setBarColor(Color.parseColor("#f9f9f9"));

//		setSeparatorColor(Color.parseColor("#ff4e5c"));
		setSeparatorColor(Color.parseColor("#f9f9f9"));

//		setIndicatorColor(Color.parseColor("#3b3b58"), Color.parseColor("#252545"));
		setIndicatorColor(Color.parseColor("#ff4e5c"), Color.parseColor("#e54652"));

		setDoneText("Get Started");
		setColorDoneText(Color.parseColor("#ff4e5c"));
//		setNextArrowColor(Color.parseColor("#ff4e5c"));
		setNextArrowColor(Color.parseColor("#f9f9f9"));

		nextButton.setVisibility(View.GONE);
	}
	public void setNextArrowColor(@ColorInt final int color) {
		ImageButton nextButton = (ImageButton) findViewById(R.id.next);
		nextButton.setColorFilter(color);
	}

	public void setDoneArrowColor(@ColorInt final int color) {
		Button doneButton = (Button) findViewById(R.id.done);
//		nextButton.setColorFilter(color);
	}
	@Override
	public void onSkipPressed() {

	}


	@Override
	public void onDonePressed() {
		// Do something when users tap on Done button.
		startMainActivity();
	}

	@Override
	public void onSlideChanged() {
		// Do something when the slide changes.
		nextButton.setVisibility(View.GONE);
	}

	@Override
	public void onNextPressed() {
		// Do something when users tap on Next button.
	}

	private void startMainActivity() {

		Intent intent = new Intent(this, MapsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		// Disable activity end transition
		overridePendingTransition(0, 0);
	}

}
