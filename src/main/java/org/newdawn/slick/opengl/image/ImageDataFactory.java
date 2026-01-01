package org.newdawn.slick.opengl.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.newdawn.slick.opengl.image.png.PNGImageData;
import org.newdawn.slick.opengl.image.tga.TGAImageData;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * A static utility to create the appropriate image data for a particular reference.
 *
 * @author kevin
 */
public class ImageDataFactory {
    private static final Logger LOGGER = LogManager.getLogger("Slick2D");

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
    private static final String PNG_LOADER = "org.newdawn.slick.pngloader";

    /**
     * Check PNG loader property. If set the native PNG loader will
     * not be used.
     */
    private static void checkProperty() {
        if (pngLoaderPropertyChecked) return;
        pngLoaderPropertyChecked = true;

        try {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                String val = System.getProperty(PNG_LOADER);
                usePngLoader = !"false".equalsIgnoreCase(val);
                LOGGER.info("Use Java PNG Loader = {}", usePngLoader);
                return null;
            });
        } catch (Throwable ignored) {
            // Security exception, likely in an applet context
        }
    }

    /**
     * Create an image data that is appropriate for the reference supplied
     *
     * @param ref The reference to the image to retrieve
     * @return The image data that can be used to retrieve the data for that resource
     */
    public static LoadableImageData getImageDataFor(String ref) {
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