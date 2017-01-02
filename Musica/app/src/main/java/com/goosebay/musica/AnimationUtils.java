package com.goosebay.musica;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

import static com.goosebay.musica.R.id.addButton;

/**
 * Created by maribel on 2016-12-30.
 */

public final class AnimationUtils {
    private AnimationUtils(){}

    public static void rotate(View view, float degrees) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("rotation", degrees));
        animator.setDuration(400);
        animator.start();
    }

    public static ObjectAnimator startPulseAnimation(View view) {
        // Animate the playing icon with a pulse animation
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(addButton,
                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));

        animator.setDuration(400);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);

        animator.setTarget(view);

        return animator;
    }
}
