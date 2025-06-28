package com.si.gymmanager.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween

object NavigationTransitionUtil {

    fun enterSlideTransition(): AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition {
        return {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(100)
            )
        }
    }

    fun exitSlideTransition(): AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition {
        return {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(100)
            )
        }
    }
}