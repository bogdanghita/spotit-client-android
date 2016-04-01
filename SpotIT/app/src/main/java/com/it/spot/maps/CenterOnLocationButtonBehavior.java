package com.it.spot.maps;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
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
			child.setY(dependency.getY() - dependency.getHeight());
		}
		else {
			child.setY(dependency.getY());
		}

		return true;
	}
}