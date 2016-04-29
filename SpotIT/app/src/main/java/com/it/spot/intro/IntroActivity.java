package com.it.spot.intro;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;
import com.it.spot.R;
import com.it.spot.maps.main.MapsActivity;

public class IntroActivity extends AppIntro {

	@Override
	public void init(Bundle savedInstanceState) {

		// Add your slide's fragments here.
		// AppIntro will automatically generate the dots indicator and buttons.
		addSlide(SampleSlide.newInstance(R.layout.intro_spots_slide));
		addSlide(SampleSlide.newInstance(R.layout.intro_colors_slide));
		addSlide(SampleSlide.newInstance(R.layout.intro_report_spot_slide));
		addSlide(SampleSlide.newInstance(R.layout.intro_save_location_slide));
		addSlide(SampleSlide.newInstance(R.layout.intro_directions_slide));
//		addSlide(second_fragment);
//		addSlide(third_fragment);
//		addSlide(fourth_fragment);
//
//		// Instead of fragments, you can also use our default slide
//		// Just set a title, description, background and image. AppIntro will do the rest.
//		addSlide(AppIntroFragment.newInstance(title, description, image, background_colour));

		// OPTIONAL METHODS
		// Override bar/separator color.
		//setBarColor(Color.parseColor("#3F51B5"));
		//setSeparatorColor(Color.parseColor("#2196F3"));

		// Hide Skip/Done button.
//		showSkipButton(false);
//		//setProgressButtonEnabled(false);
//
		setBarColor(Color.parseColor("#f9f9f9"));
		setSeparatorColor(Color.parseColor("#ff4e5c"));
		setIndicatorColor(Color.parseColor("#3b3b58"), Color.parseColor("#252545"));

		View view = findViewById(com.github.paolorotolo.appintro.R.id.next);
		view.setBackgroundColor(Color.parseColor("#f9f9f9"));
		View view2 = findViewById(com.github.paolorotolo.appintro.R.id.done);
		view2.setBackgroundColor(Color.parseColor("#f9f9f9"));

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
