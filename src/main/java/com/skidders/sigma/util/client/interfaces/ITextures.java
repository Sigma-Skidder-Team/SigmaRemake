package com.skidders.sigma.util.client.interfaces;

import com.skidders.sigma.util.client.render.Loader;
import org.newdawn.slick.opengl.Texture;

public interface ITextures {
    Texture switch_background = Loader.loadTexture("loading/back.png");
    Texture switch_sigmaLogo = Loader.loadTexture("loading/logo.png");

    Texture switch_noAddonsButton = Loader.loadTexture("switch/noaddons.png");
    Texture switch_classicButton = Loader.loadTexture("switch/classic.png");
    Texture switch_jelloButton = Loader.loadTexture("switch/jello.png");

    Texture switch_youtubeLogo = Loader.loadTexture("media/youtube.png");
    Texture switch_redditLogo = Loader.loadTexture("media/reddit.png");
    Texture switch_discordLogo = Loader.loadTexture("media/youtube.png");
}