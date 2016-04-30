package com.it.spot.maps.report;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import com.it.spot.R;


public class DialogReveal extends Dialog {

	public DialogReveal(Context context) {
		super(context);
	}

	public DialogReveal(Context context, int themeResId) {
		super(context, themeResId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#3b3b58"));
//        colorDrawable.setAlpha(0);
		setContentView(R.layout.report_parking_spot_layout);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		this.setOnShowListener(showListener);

		// Round dialog ---->
//		LinearLayout ll = (LinearLayout) findViewById(R.id.dialog_data);
//		Drawable d = new CircleReportSpotDialogDrawable(Color.parseColor("#3b3b58"));
//		//TODO pick between deprecated and api 16
//		if (Build.VERSION.SDK_INT >= 16) {
//			ll.setBackground(d);
//		}
//		else {
//			ll.setBackgroundDrawable(d);
//		}
		// <---- Round dialog
	}

	public OnShowListener showListener = new OnShowListener() {
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onShow(DialogInterface dialog) {
			enterReveal();
		}
	};

	public OnDismissListener dismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialogInterface) {

		}
	};

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void dismiss() {
		exitReveal();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				DialogReveal.super.dismiss();
			}
		}, 400);
	}

	void enterReveal() {
		final View view = this.findViewById(android.R.id.content);
		// get the center for the clipping circle
		int cx = view.getMeasuredWidth();
		int cy = view.getMeasuredHeight();

		// get the initial radius for the clipping circle
		float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

		Animator anim;

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// create the animator for this view (the start radius is zero)
			anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
			anim.setDuration(1000);
			anim.start();
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//Todo Animate buttons on dialog reveal
//                    LinearLayout ll = (LinearLayout) findViewById(R.id.report_buttons_container);
//                    animateButtonsIn(ll, 1.2f);

					super.onAnimationEnd(animation);
				}
			});
		}
	}

	private void animateButtonsIn(LinearLayout layoutContainerAll, float scale) {
		for (int i = 0; i < layoutContainerAll.getChildCount(); i++) {

			View rowView = layoutContainerAll.getChildAt(i);
//            rowView.animate().setStartDelay(i * 200)
//                    .scaleX(scale).scaleY(scale);

			ScaleAnimation animation = new ScaleAnimation(1, scale, 1, scale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setDuration(400);
			rowView.setAnimation(animation);
			rowView.animate().setStartDelay(i * 200);
		}
	}

	void exitReveal() {
		View view = this.findViewById(android.R.id.content);
		// get the center for the clipping circle
		int cx = view.getMeasuredWidth();
		int cy = view.getMeasuredHeight();

		// get the initial radius for the clipping circle
		float initialRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

		Animator anim;
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// create the animation (the final radius is zero)
			anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
			anim.setDuration(400);
			// make the view invisible when the animation is done
			anim.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
				}
			});

			// start the animation
			anim.start();
		}
	}

	//TODO: circular reveal for sdk < Lollipop
	private Animator createCircularReveal(final ClipRevealFrame view, int x, int y, float startRadius,
	                                      float endRadius) {
		final Animator reveal;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			reveal = ViewAnimationUtils.createCircularReveal(view, x, y, startRadius, endRadius);
		}
		else {
			view.setClipOutLines(true);
			view.setClipCenter(x, y);
			reveal = ObjectAnimator.ofFloat(view, "ClipRadius", startRadius, endRadius);
			reveal.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationEnd(Animator animation) {
					view.setClipOutLines(false);
				}

				@Override
				public void onAnimationCancel(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {

				}
			});
		}
		return reveal;
	}
}