package com.ipol.leporellotest;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

@SuppressLint("NewApi")
public class LeporelloActivity extends Activity {
	private static final int FOLD_AMOUNT = 3;
	private static final int FOLD_WIDTH = 20;
	private static final int LESSER_FOLD_WIDTH = 5;
	private static final int FOLD_DISTANCE_WIDTH = FOLD_WIDTH - LESSER_FOLD_WIDTH;
	private static final int RIGHT_BORDER_WIDTH = 2;
	private static final int LEFT_BORDER_WIDTH = 4;
	private static final int LEFT_LESSER_BORDER_WIDTH = 2;

	private static final float FLING_MIN_DISTANCE_DP = 20f;
	private static final float FLING_THRESHOLD_VELOCITY_DP = 40f;

	private static final float SCALE_DEFAULT = 0.95f;
	private static final float SCALE_STEP = 0.03f;
	private static final float SCALE_LESSER_STEP = 0.01f;

	private int screenWidth;

	private GestureDetector gestureDetector;
	private int flingMinDistance, flingThresholdVelocity;

	private RelativeLayout layCards;
	private CardView layPrev, layCurr;

	private int sections;
	private int currSection = 0;
	private List<CardView> containers;
	private List<View> borders;
	private int containersSize;
	private float firstX, lastX, currX, totalDiffX;
	private float foldDiffX;
	private float firstScaleIncrement, normalScaleIncrement, lesserScaleIncrement;

	private float layPrevTranslationXOld;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		screenWidth = metrics.widthPixels;

		final float scale = metrics.density;
		flingMinDistance = (int) (FLING_MIN_DISTANCE_DP * scale + 0.5f);
		flingThresholdVelocity = (int) (FLING_THRESHOLD_VELOCITY_DP * scale + 0.5f);

		// Init gesture detector.
		gestureDetector = new GestureDetector(this, new GestureListener());

		// Hard coded number of sections.
		sections = 6;

		initRoot();
		initContainers();
		initBorders();
		addContainersToView();

	}

	private void initRoot() {
		layCards = new RelativeLayout(this);
		layCards.setBackgroundColor(Color.WHITE);
		layCards.setLayoutParams(new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	private void initContainers() {
		containers = new ArrayList<CardView>();

		for (int i = 0, containerWidth = 0, folds = 0, lesserFolds = 0, borders = 0; i < sections; i++) {
			final CardView layContainer = new CardView(this);
			// Container width gets smaller for each card left to the last one.
			// Example margins: screenWidth - AxFOLD_WIDTH - BxLESSER_FOLD_WIDTH
			// - CxRIGHT_BORDER_WIDTH
			// - DxLEFT_LESSER_BORDER_WIDTH - ExLEFT_BORDER_WIDTH
			// 0: screenWidth - 3x20 - 2x5 - 6x2 - 0x2 - 0x4
			// 1: screenWidth - 3x20 - 1x5 - 5x2 - 0x2 - 1x4
			// 2: screenWidth - 3x20 - 0x5 - 4x2 - 2x2 - 1x4
			// 3: screenWidth - 2x20 - 0x5 - 3x2 - 4x2 - 1x4
			// 4: screenWidth - 1x20 - 0x5 - 2x2 - 6x2 - 1x4
			// 5: screenWidth - 0x20 - 0x5 - 1x2 - 8x2 - 1x4
			//
			// 0: screenWidth - 3x20 - 4x5 - 8x2 - 0x2 - 0x4
			// 1: screenWidth - 3x20 - 3x5 - 7x2 - 0x2 - 1x4
			// 2: screenWidth - 3x20 - 2x5 - 6x2 - 2x2 - 1x4
			// 3: screenWidth - 3x20 - 1x5 - 5x2 - 4x2 - 1x4
			// 4: screenWidth - 3x20 - 0x5 - 4x2 - 6x2 - 1x4
			// 5: screenWidth - 2x20 - 0x5 - 3x2 - 8x2 - 1x4
			// 6: screenWidth - 1x20 - 0x5 - 2x2 - 10x2 - 1x4
			// 7: screenWidth - 0x20 - 0x5 - 1x2 - 12x2 - 1x4

			if (i < sections - 1 - FOLD_AMOUNT) {
				folds = FOLD_AMOUNT;
				lesserFolds = sections - 1 - FOLD_AMOUNT - i;
			} else {
				folds = sections - 1 - i;
				lesserFolds = 0;
			}
			borders = sections - i;
			// Put calculated amounts into formula.
			containerWidth = screenWidth - folds * FOLD_WIDTH - lesserFolds
					* LESSER_FOLD_WIDTH - borders * RIGHT_BORDER_WIDTH;
			System.out.println(String.format("%d: %dpx (%d - %d - %d - %d)", i,
					containerWidth, screenWidth, folds * FOLD_WIDTH, lesserFolds
							* LESSER_FOLD_WIDTH, borders * RIGHT_BORDER_WIDTH));

			layContainer.setLayoutParams(new LayoutParams(containerWidth,
					LayoutParams.MATCH_PARENT));
			layContainer.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						@SuppressWarnings("deprecation")
						@Override
						public void onGlobalLayout() {
							layContainer.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
							layContainer.setPivotY(layContainer.getBottom());
						}
					});

			layContainer.setTranslationX(layContainer.getTranslationX()
					- RIGHT_BORDER_WIDTH);
			// Initial rotation for first container is different than for the
			// others.
			if (i == 0)
				layContainer.setScaleY(1);
			else if (i <= FOLD_AMOUNT)
				layContainer.setScaleY(SCALE_DEFAULT - (i - 1) * SCALE_STEP);
			else
				layContainer.setScaleY(SCALE_DEFAULT - (FOLD_AMOUNT - 1) * SCALE_STEP
						- (i - FOLD_AMOUNT) * SCALE_LESSER_STEP);
			// Add the container to the list for accessing it later on.
			containers.add(layContainer);
		}

		// Adding the initial translation for before the leporello effect.
		// Needed for later calculations.
		System.out.println("INIT X TRANSLATIONS:");
		for (int i = 0, initTranslationX = 0, lesserFolds = sections - 1 - FOLD_AMOUNT; i < sections; i++) {
			System.out.println(String.format("%d: %d", i, initTranslationX));
			containers.get(i).setInitialTranslationX(initTranslationX);
			if (FOLD_AMOUNT > lesserFolds) {
				if (i < lesserFolds) {
					initTranslationX = initTranslationX + FOLD_DISTANCE_WIDTH;
				} else if (i >= FOLD_AMOUNT) {
					initTranslationX = initTranslationX - FOLD_DISTANCE_WIDTH;
				}
			} else {
				if (i < FOLD_AMOUNT) {
					initTranslationX = initTranslationX + FOLD_DISTANCE_WIDTH;
				} else if (i >= lesserFolds) {
					initTranslationX = initTranslationX - FOLD_DISTANCE_WIDTH;
				}
			}

			View middleBar = new View(this);
			middleBar.setBackgroundColor(Color.BLACK);
			android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(
					1, LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			layCards.addView(middleBar, params);
		}

		// Getting size of containers, as it is used a lot in onTouchEvent.
		containersSize = containers.size();

		setContentView(layCards);
	}

	private void initBorders() {
		borders = new ArrayList<View>();

		for (int i = 0; i < sections; i++) {
			final View viwBorder = new View(this);
			viwBorder.setLayoutParams(new LayoutParams(RIGHT_BORDER_WIDTH,
					LayoutParams.MATCH_PARENT));
			viwBorder.setBackgroundColor(Color.LTGRAY);
			containers.get(i).setBorderRight(viwBorder);
			borders.add(viwBorder);

			final View viwSecondBorder = new View(this);
			viwSecondBorder.setLayoutParams(new LayoutParams(RIGHT_BORDER_WIDTH,
					LayoutParams.MATCH_PARENT));
			viwSecondBorder.setBackgroundColor(Color.LTGRAY);
		}
	}

	private void addContainersToView() {
		// Views must be added to the view hierarchy from last to first.
		for (int i = sections - 1; i >= 0; i--) {
			layCards.addView(containers.get(i));
			layCards.addView(borders.get(i));
		}

		// Temporary code.
		containers.get(0).setBackgroundColor(Color.GRAY);
		containers.get(1).setBackgroundColor(Color.BLUE);
		containers.get(2).setBackgroundColor(Color.YELLOW);
		containers.get(3).setBackgroundColor(Color.RED);
		containers.get(4).setBackgroundColor(Color.GREEN);
		containers.get(5).setBackgroundColor(Color.CYAN);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Check GestureDetector first.
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		}

		// Check usual finger actions.
		final int action = event.getAction();

		if (action == MotionEvent.ACTION_DOWN) {
			lastX = event.getRawX();
			firstX = event.getRawX();
			layCurr = containers.get(currSection);
			for (CardView card : containers)
				card.setLastScaleY(card.getScaleY());
			if (currSection < containersSize - 1) {
				firstScaleIncrement = (1 - SCALE_DEFAULT) / (float) layCurr.getRight();
				normalScaleIncrement = SCALE_STEP / (float) layCurr.getRight();
				lesserScaleIncrement = SCALE_LESSER_STEP / (float) layCurr.getRight();
			}
			if (currSection - 1 >= 0) {
				layPrev = containers.get(currSection - 1);
				layPrevTranslationXOld = layPrev.getTranslationX();
			}
		}
		if (action == MotionEvent.ACTION_MOVE) {
			currX = event.getRawX();
			totalDiffX = currX - firstX;

			// Calculating reduced travel distance of the cards affected by the
			// leporello effect.
			foldDiffX = (currX - lastX) / (screenWidth / FOLD_DISTANCE_WIDTH);
			// The current translation of the first card. Must be checked to see
			// if further animation to the left is possible.

			if (currSection != containersSize - 1) {
				if (totalDiffX < 0) {
					// move finger to left
					layCurr.setTranslationX(totalDiffX);
					for (int i = 0; i < containersSize - (currSection + 1); i++) {
						CardView card = containers.get(currSection + 1 + i);
						if (i == 0) {
							card.setScaleY(card.getLastScaleY() + firstScaleIncrement
									* Math.abs(layCurr.getTranslationX()));
						} else if (i < FOLD_AMOUNT)
							card.setScaleY(card.getLastScaleY() + normalScaleIncrement
									* Math.abs(layCurr.getTranslationX()));
						else
							card.setScaleY(card.getLastScaleY() + lesserScaleIncrement
									* Math.abs(layCurr.getTranslationX()));
					}
				} else {
					// move finger to right
					if (currSection - 1 >= 0) {
						layPrev = containers.get(currSection - 1);
						layPrev.setTranslationX(layPrevTranslationXOld + totalDiffX);

						for (int i = 0; i < containersSize - currSection; i++) {
							CardView card = containers.get(currSection + i);
							if (i == 0)
								card.setScaleY(card.getLastScaleY() - firstScaleIncrement
										* totalDiffX);
							else if (i < FOLD_AMOUNT)
								card.setScaleY(card.getLastScaleY()
										- normalScaleIncrement * totalDiffX);
							else
								card.setScaleY(card.getLastScaleY()
										- lesserScaleIncrement * totalDiffX);
						}
					}
				}

				// Leporello animation for affected cards.
				if (currSection != 0 && currSection <= sections - 1 - FOLD_AMOUNT) {
					if (layCurr.getTranslationX() >= 0) {
						for (int i = currSection; i < currSection + FOLD_AMOUNT; i++) {
							containers.get(i).setTranslationX(
									containers.get(i).getTranslationX() + foldDiffX);
						}
					} else if (currSection < sections - 1 - FOLD_AMOUNT) {
						for (int i = currSection + 1; i <= currSection + FOLD_AMOUNT; i++) {
							containers.get(i).setTranslationX(
									containers.get(i).getTranslationX() + foldDiffX);
						}
					}
				} else if (currSection == 0 && layCurr.getTranslationX() < 0) {
					for (int i = 1; i <= 3; i++)
						containers.get(i).setTranslationX(
								containers.get(i).getTranslationX() + foldDiffX);
				}
			}

			lastX = currX;
		}

		if (action == MotionEvent.ACTION_UP) {
			List<Animator> animators = new ArrayList<Animator>();
			if (totalDiffX < 0) {
				float rightEdge = layCurr.getWidth() + layCurr.getTranslationX();
				if (rightEdge < screenWidth / 2) {
					animateTransission(animators, false, true);
					currSection++;
				} else {
					currSection++;
					animateTransission(animators, true, false);
					currSection--;
				}
			} else if (currSection != 0) {
				float rightEdge = containers.get(currSection - 1).getWidth()
						+ containers.get(currSection - 1).getTranslationX();
				if (rightEdge < screenWidth / 2) {
					currSection--;
					animateTransission(animators, false, false);
					currSection++;
				} else {
					animateTransission(animators, true, true);
					currSection--;
				}
			}
		}
		return super.onTouchEvent(event);
	}

	public void animateTransission(List<Animator> animators, boolean leftToRight,
			boolean gesture) {

		if (leftToRight) {
			animators.add(ObjectAnimator.ofFloat(containers.get(currSection - 1),
					"translationX", 0));
			for (int i = 0; i < containersSize - currSection; i++) {
				if (i == 0)
					animators.add(ObjectAnimator.ofFloat(containers.get(currSection + i),
							"scaleY", SCALE_DEFAULT));
				else if (i < FOLD_AMOUNT)
					animators.add(ObjectAnimator.ofFloat(containers.get(currSection + i),
							"scaleY", SCALE_DEFAULT - (i) * SCALE_STEP));
				else
					animators.add(ObjectAnimator.ofFloat(containers.get(currSection + i),
							"scaleY", SCALE_DEFAULT - (FOLD_AMOUNT - 1) * SCALE_STEP
									- (i - (FOLD_AMOUNT - 1)) * SCALE_LESSER_STEP));
			}

			// Adding additional animations for all views affected by
			// the leporello effect.
			if (currSection <= sections - 1 - FOLD_AMOUNT) {
				if (gesture)
					for (int i = currSection; i < currSection + FOLD_AMOUNT; i++) {
						final CardView currContainer = containers.get(i);
						if (currContainer.getLastTranslationX() < currContainer
								.getInitialTranslationX()) {
							currContainer.setLastTranslationX(currContainer
									.getLastTranslationX() + 15);
							animators.add(ObjectAnimator.ofFloat(currContainer,
									"translationX", currContainer.getLastTranslationX()));
						}
					}
				else
					for (int i = currSection; i < currSection + FOLD_AMOUNT; i++) {
						final CardView card = containers.get(i);
						animators.add(ObjectAnimator.ofFloat(card, "translationX",
								card.getLastTranslationX()));
				}
			}

		} else {
			animators.add(ObjectAnimator.ofFloat(containers.get(currSection),
					"translationX", -(containers.get(currSection).getWidth())));
			for (int i = 0; i < containersSize - (currSection + 1); i++) {
				if (i == 0)
					animators.add(ObjectAnimator.ofFloat(
							containers.get(currSection + 1 + i), "scaleY", 1));
				else if (i <= FOLD_AMOUNT)
					animators.add(ObjectAnimator.ofFloat(
							containers.get(currSection + 1 + i), "scaleY", SCALE_DEFAULT
									- (i - 1) * SCALE_STEP));
				else
					animators.add(ObjectAnimator.ofFloat(
							containers.get(currSection + 1 + i), "scaleY", SCALE_DEFAULT
									- (FOLD_AMOUNT - 1) * SCALE_STEP - (i - FOLD_AMOUNT)
									* SCALE_LESSER_STEP));
			}

			// Adding additional animations for all views affected by
			// the leporello effect.
			if (currSection < sections - 1 - FOLD_AMOUNT) {
				if (gesture)
					for (int i = currSection + 1; i <= currSection + FOLD_AMOUNT; i++) {
						final CardView currContainer = containers.get(i);
						if (currContainer.getLastTranslationX() > 0) {
							currContainer.setLastTranslationX(currContainer
									.getLastTranslationX() - 15);
							animators.add(ObjectAnimator.ofFloat(currContainer,
									"translationX", currContainer.getLastTranslationX()));
						}
					}
				else
					for (int i = currSection + 1; i <= currSection + FOLD_AMOUNT; i++) {
						final CardView card = containers.get(i);
						animators.add(ObjectAnimator.ofFloat(card, "translationX",
								card.getLastTranslationX()));
					}
			}
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		set.setInterpolator(new DecelerateInterpolator());
		set.setDuration(350).start();

	}

	private class GestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getRawX() - e2.getRawX() > flingMinDistance
					&& Math.abs(velocityX) > flingThresholdVelocity) {
				// Fling to right (finger moved to left). Not allowed in last
				// section.
				if (currSection != containersSize - 1) {
					List<Animator> animators = new ArrayList<Animator>();
					animateTransission(animators, false, true);
					currSection++;

					return true;
				}
			} else if (e2.getRawX() - e1.getRawX() > flingMinDistance
					&& Math.abs(velocityX) > flingThresholdVelocity) {
				// Fling to left (finger moved to right). Not allowed in first
				// section.
				if (currSection != 0) {
					List<Animator> animators = new ArrayList<Animator>();
					animateTransission(animators, true, true);
					currSection--;

					return true;
				}
			}
			return false;
		}
	}
}
