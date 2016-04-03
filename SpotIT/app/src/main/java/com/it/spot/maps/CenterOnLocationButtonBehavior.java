package com.it.spot.maps;

import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.it.spot.R;

/**
 * Created by Bogdan on 01/04/2016.
 */
public class CenterOnLocationButtonBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

	public CenterOnLocationButtonBehavior(Context context, AttributeSet attrs) {
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
		return dependency.getId() == R.id.directions_fab;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {

		if (dependency.getVisibility() == View.VISIBLE) {

			// TODO: this is R.dimen.fab_margin, but it returns huge value if accessed from code
			float fab_margin = 8;
			float spacing_dp = 2 * fab_margin;

			float spacing_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, spacing_dp, child.getResources().getDisplayMetrics());

			child.setY(dependency.getY() - dependency.getHeight() - spacing_px);
		}
		else {
			child.setY(dependency.getY());
		}

		return true;
	}
}