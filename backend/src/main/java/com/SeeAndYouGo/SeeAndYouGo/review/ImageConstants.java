package com.SeeAndYouGo.SeeAndYouGo.review;

public final class ImageConstants {

    // File paths
    public static final String IMAGE_STORAGE_DIR = "imageStorage";
    public static final String TEMP_IMAGE_DIR = "./tmpImage";
    public static final String IMAGE_API_PREFIX = "/api/images/";

    // Image resize dimensions
    public static final int TARGET_WIDTH = 800;
    public static final int TARGET_HEIGHT = 600;

    // Aspect ratio for cropping (4:3 ratio)
    public static final int ASPECT_RATIO_WIDTH = 4;
    public static final int ASPECT_RATIO_HEIGHT = 3;

    private ImageConstants() {
    }
}
