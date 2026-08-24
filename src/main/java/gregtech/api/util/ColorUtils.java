package gregtech.api.util;

public class ColorUtils {

    private static final ColorResource.Factory color = new ColorResource.Factory("gregtech");

    public static final ColorResource
    // spotless:off
        progressBarTop                 = color.argb("progressBarTop", "0xFF69BF3D"),
        progressBarBottom              = color.argb("progressBarBottom", "0xFF4B8230"),

        euBarTop                       = color.argb("euBarTop", "0xFFE6C920"),
        euBarBottom                    = color.argb("euBarBottom", "0xFF8A6E00");
    // spotless:on
}
