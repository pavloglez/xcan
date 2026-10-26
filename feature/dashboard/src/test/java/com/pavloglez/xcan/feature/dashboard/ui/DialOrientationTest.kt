package com.pavloglez.xcan.feature.dashboard.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DialOrientationTest {

    @Test
    fun mapOrientationToDialAngle_mapsPortraitCorrectly() {
        assertEquals(0f, mapOrientationToDialAngle(0), 0.01f)
        assertEquals(0f, mapOrientationToDialAngle(15), 0.01f)
        assertEquals(0f, mapOrientationToDialAngle(44), 0.01f)
        assertEquals(0f, mapOrientationToDialAngle(315), 0.01f)
        assertEquals(0f, mapOrientationToDialAngle(350), 0.01f)
    }

    @Test
    fun mapOrientationToDialAngle_mapsLandscapeLeftEdgeUpCorrectly() {
        assertEquals(-90f, mapOrientationToDialAngle(45), 0.01f)
        assertEquals(-90f, mapOrientationToDialAngle(90), 0.01f)
        assertEquals(-90f, mapOrientationToDialAngle(134), 0.01f)
    }

    @Test
    fun mapOrientationToDialAngle_mapsUpsideDownCorrectly() {
        assertEquals(180f, mapOrientationToDialAngle(135), 0.01f)
        assertEquals(180f, mapOrientationToDialAngle(180), 0.01f)
        assertEquals(180f, mapOrientationToDialAngle(224), 0.01f)
    }

    @Test
    fun mapOrientationToDialAngle_mapsLandscapeRightEdgeUpCorrectly() {
        assertEquals(90f, mapOrientationToDialAngle(225), 0.01f)
        assertEquals(90f, mapOrientationToDialAngle(270), 0.01f)
        assertEquals(90f, mapOrientationToDialAngle(314), 0.01f)
    }

    @Test
    fun calculateShortestTargetAngle_animatesAdjacentQuadrantsWithout360Spinning() {
        // Counter-clockwise progression: 0 -> -90 -> -180 -> -270 -> -360
        val target1 = calculateShortestTargetAngle(0f, -90f)
        assertEquals(-90f, target1, 0.01f)

        val target2 = calculateShortestTargetAngle(-90f, 180f)
        assertEquals(-180f, target2, 0.01f)

        val target3 = calculateShortestTargetAngle(-180f, 90f)
        assertEquals(-270f, target3, 0.01f)

        val target4 = calculateShortestTargetAngle(-270f, 0f)
        assertEquals(-360f, target4, 0.01f)

        // Clockwise progression: 0 -> 90 -> 180 -> 270 -> 360
        val cwTarget1 = calculateShortestTargetAngle(0f, 90f)
        assertEquals(90f, cwTarget1, 0.01f)

        val cwTarget2 = calculateShortestTargetAngle(90f, 180f)
        assertEquals(180f, cwTarget2, 0.01f)

        val cwTarget3 = calculateShortestTargetAngle(180f, -90f)
        assertEquals(270f, cwTarget3, 0.01f)

        val cwTarget4 = calculateShortestTargetAngle(270f, 0f)
        assertEquals(360f, cwTarget4, 0.01f)
    }
}
