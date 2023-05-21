package com.pwr.pjmassistant.model;

import org.opencv.core.Mat;

public class HandData
{
    private float bottom = 0;
    private float top = 0;
    private float left = 0;
    private float right = 0;

    private int height = 0;
    private int width = 0;

    private final boolean handDetected;

    private Mat image;

    public HandData(boolean handDetected, Mat image)
    {
        this.handDetected = handDetected;
        this.image = image;
    }

    public HandData(boolean handDetected, Mat image, int height, int width, float top, float bottom, float left, float right)
    {
        this.handDetected = handDetected;
        this.image = image;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.height = height;
        this.width = width;
    }

    public Float[] getBoundingBox()
    {
        return new Float[]{top, bottom, left, right};
    }

    public boolean isHandDetected() { return handDetected; }

    public Mat getImageData() { return image; }

    public int getHeight() { return height; }

    public int getWidth() { return width; }
}
