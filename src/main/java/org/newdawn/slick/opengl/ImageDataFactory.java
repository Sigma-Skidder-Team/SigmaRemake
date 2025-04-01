package org.newdawn.slick.opengl;

import com.skidders.sigma.util.client.interfaces.ILogger;

/**
 * A static utility to create the appropriate image data for a particular reference.
 *
 * @author kevin
 */
public class ImageDataFactory implements ILogger {
    /**
     * True if we're going to use the native PNG loader - cached so it doesn't have
     * the security check repeatedly
     */
    private static boolean usePngLoader = true;
    /**
     * True if the PNG loader property has been checked
     */
    private static boolean pngLoaderPropertyChecked = false;

    /**
     * The name of the PNG loader configuration property
     */
    private static final String PNG_LOADER = "org.newdawn.org.newdawn.slick.slick.pngloader";

    /**
     * Check PNG loader property. If set, the native PNG loader will
     * not be used.
     */
    private static void checkProperty() {
        if (!pngLoaderPropertyChecked) {
            pngLoaderPropertyChecked = true;

            try {
                String val = System.getProperty(PNG_LOADER);
                if ("false".equalsIgnoreCase(val)) {
                    usePngLoader = false;
                }

                logger.info("Use Java PNG Loader = {}", usePngLoader);
            } catch (SecurityException e) {
                // Ignore, likely due to security restrictions
            }
        }
    }

    /**
     * Create an image data that is appropriate for the reference supplied
     *
     * @param ref The reference to the image to retrieve
     * @return The image data that can be used to retrieve the data for that resource
     */
    public static LoadableImageData getImageDataFor(String ref) {
        LoadableImageData imageData;
        checkProperty();

        ref = ref.toLowerCase();

        if (ref.endsWith(".tga")) {
            return new TGAImageData();
        }
        if (ref.endsWith(".png")) {
            CompositeImageData data = new CompositeImageData();
            if (usePngLoader) {
                data.add(new PNGImageData());
            }
            data.add(new ImageIOImageData());

            return data;
        }

        return new ImageIOImageData();
    }
}