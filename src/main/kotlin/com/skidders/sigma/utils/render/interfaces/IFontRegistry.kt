package com.skidders.sigma.utils.render.interfaces

import com.skidders.sigma.utils.render.font.common.FontLanguage
import com.skidders.sigma.utils.render.font.styled.StyledFont

interface IFontRegistry {
    companion object {
        val Light12: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 12, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light14: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 14, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light18: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 18, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light20: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 20, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light24: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 24, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light25: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 25, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light28: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 28, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light36: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 36, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light40: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 40, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Light50: StyledFont = StyledFont("HelveticaNeue-Light.ttf", 50, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Regular20: StyledFont = StyledFont("MyFont-Regular.ttf", 20, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Regular40: StyledFont = StyledFont("MyFont-Regular.ttf", 40, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Medium20: StyledFont = StyledFont("HelveticaNeue-Medium.ttf", 20, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Medium25: StyledFont = StyledFont("HelveticaNeue-Medium.ttf", 25, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Medium40: StyledFont = StyledFont("HelveticaNeue-Medium.ttf", 40, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val Medium50: StyledFont = StyledFont("HelveticaNeue-Medium.ttf", 50, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val SRegular17: StyledFont = StyledFont("SFUIDisplay-Regular.ttf", 17, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
        val SBold17: StyledFont = StyledFont("SFUIDisplay-Bold.ttf", 17, 0.0f, 0.0f, 0.5f, FontLanguage.ENGLISH)
    }
}