package com.it.spot.maps;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.it.spot.R;

/**
 * Created by Bogdan on 01/04/2016.
 */
public class ParkingStateButtonsBehavior extends CoordinatorLayout.Behavior<LinearLayout> {

	public ParkingStateButtonsBehavior(Context context, AttributeSet attrs) {
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, LinearLayout child, View dependency) {
		return dependency.getId() == R.id.location_address_bar;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, LinearLayout child, View dependency) {

		float translationY = Math.min(0, dependency.getTranslationY() - dependency.getHeight());
		child.setTranslationY(translationY);

		return true;
	}
}
