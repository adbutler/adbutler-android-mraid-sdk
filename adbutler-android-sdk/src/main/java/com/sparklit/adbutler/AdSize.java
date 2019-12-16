package com.sparklit.adbutler;


/**
 * The size (width and height) of an ad request.
 */
public class AdSize {
    private int mWidth;
    private int mHeight;

    /**
     * Set the size of the ad.
     *
     * @param width
     * @param height
     */
    public AdSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    /**
     * Get the width of the ad.
     *
     * @return
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Get the height of the ad.
     *
     * @return
     */
    public int getHeight() {
        return mHeight;
    }
}
