package gregtech.api.util;

public class ColorUtils {

    private static final ColorResource.Factory color = new ColorResource.Factory("gregtech");

    public static final ColorResource
    // spotless:off
        progressBarTop                 = color.argb("progressBarTop", "0xFFFF0000"),
        progressBarBottom              = color.argb("progressBarBottom", "0xFF8B0000"),

        euBarTop                       = color.argb("euBarTop", "0xFFF5E32C"),
        euBarBottom                    = color.argb("euBarBottom", "0xFF9C7E00");
    // spotless:on
}
