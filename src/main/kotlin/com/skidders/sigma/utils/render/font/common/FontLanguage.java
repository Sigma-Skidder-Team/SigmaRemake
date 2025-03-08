package com.skidders.sigma.utils.render.font.common;

public enum FontLanguage {
	ENGLISH(new int[] {31, 127, 0, 0}),
	ENGLISH_RUSSIAN(new int[] {31, 127, 1024, 1106});
	
	public final int[] charCodes;
	
	FontLanguage(int[] charCodes) {
		this.charCodes = charCodes;
	}
}
