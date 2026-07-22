package BetterGI_GameCapture;

import BetterGI_GameCapture.BitBlt.BitBltCapture;

/**
 * 游戏截图工厂，对应 C# Fischless.GameCapture.GameCaptureFactory
 */
public class GameCaptureFactory {

    /**
     * 获取所有截图模式名称
     */
    public static String[] modeNames() {
        CaptureModes[] values = CaptureModes.values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    /**
     * 根据模式创建对应的截图实现
     *
     * @param mode 截图模式
     * @return IGameCapture 实例
     * @throws UnsupportedOperationException 如果模式暂不支持（如 DWM / GraphicsCapture）
     */
    public static IGameCapture create(CaptureModes mode) {
        return switch (mode) {
            case BitBlt -> new BitBltCapture();
            // DwmGetDxSharedSurface 和 WindowsGraphicsCapture 需要 SharpDX / WinRT，
            // Java 中暂无对应方案，暂不支持
            case DwmGetDxSharedSurface, WindowsGraphicsCapture, WindowsGraphicsCaptureHdr ->
                    throw new UnsupportedOperationException(
                            "Capture mode '" + mode.getDescription()
                                    + "' is not supported in Java implementation. "
                                    + "Only BitBlt is currently available.");
        };
    }
}
