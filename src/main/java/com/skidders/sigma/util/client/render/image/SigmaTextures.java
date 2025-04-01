package com.skidders.sigma.util.client.render.image;

import com.skidders.sigma.util.system.file.ResourceLoader;
import org.newdawn.slick.opengl.Texture;

public class SigmaTextures {
    public static final Texture switch_background = ResourceLoader.loadTexture("loading/back.png");
    public static final Texture switch_sigmaLogo = ResourceLoader.loadTexture("loading/logo.png");

    public static final Texture switch_noAddonsButton = ResourceLoader.loadTexture("switch/noaddons.png");
    public static final Texture switch_classicButton = ResourceLoader.loadTexture("switch/classic.png");
    public static final Texture switch_jelloButton = ResourceLoader.loadTexture("switch/jello.png");

    public static final Texture switch_youtubeLogo = ResourceLoader.loadTexture("media/youtube.png");
    public static final Texture switch_redditLogo = ResourceLoader.loadTexture("media/reddit.png");
    public static final Texture switch_discordLogo = ResourceLoader.loadTexture("media/youtube.png");
}