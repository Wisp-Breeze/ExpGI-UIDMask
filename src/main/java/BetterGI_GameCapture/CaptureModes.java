package BetterGI_GameCapture;

/**
 * 截图模式枚举，对应 C# Fischless.GameCapture.CaptureModes
 */
public enum CaptureModes {

    BitBlt(0, "BitBlt"),

    WindowsGraphicsCapture(1, "WindowsGraphicsCapture"),

    DwmGetDxSharedSurface(2, "DwmGetDxSharedSurface"),

    WindowsGraphicsCaptureHdr(3, "WindowsGraphicsCapture（HDR）");

    private final int value;
    private final String description;

    CaptureModes(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 从模式名称字符串解析为枚举值
     */
    public static CaptureModes fromName(String name) {
        return valueOf(name);
    }
}
