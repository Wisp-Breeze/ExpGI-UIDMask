# 这是一个未完成的 实验性工具

[English](README_en.md) |
[中文](README.md)

## 1.项目说明
### （1）开发环境
*  JDK 26
*  Maven

### （2）用到的东西
*  **JNA** — 用来调用 Windows 系统的一些原生接口（比如截图、窗口操作）
*  **JavaCV / OpenCV** — 用来处理截图得到的图片
*  **Guava** — 一些方便的小工具

### （3）如何运行
    它暂时不支持运行，仍在开发中
---

## BitBlt截图器
- 代码来源自BetterGI[[原文件]](https://github.com/babalae/better-genshin-impact/tree/main/Fischless.GameCapture)

- 原语言为 **C#** ，BitBlt 模式已翻译为 **JAVA** ✅

- 作者是Java小白，有错误的地方，欢迎指出

## 部分代码展示

以 `Start` 方法为例，看看 C# 原版和翻译后的 Java 长什么样：

### 原文 C#
```csharp
    public void Start(nint hWnd, Dictionary<string, object>? settings = null)
    {
        if (settings == null || !settings.TryGetValue("autoFixWin11BitBlt", out var value)) return;
        if (value is true)
        {
            BitBltRegistryHelper.SetDirectXUserGlobalSettings();
        }

        _lockSlim.EnterWriteLock();
        try
        {
            _hWnd = hWnd;
            if (_hWnd == IntPtr.Zero)
            {
                return;
            }

            _session?.Dispose();
            _session = null;
            IsCapturing = true;
        }
        finally
        {
            _lockSlim.ExitWriteLock();
        }

        CheckSession();
    }
```

### 译文 JAVA
```java
    @Override
    public void start(long hWnd, Map<String, Object> settings) {
        // 可选：Win11 BitBlt 修复
        if (settings != null && Boolean.TRUE.equals(settings.get("autoFixWin11BitBlt"))) {
            BitBltRegistryHelper.setDirectXUserGlobalSettings();
        }

        lockSlim.writeLock().lock();
        try {
            hWndValue = hWnd;
            if (hWndValue == 0) {
                return;
            }

            if (session != null) {
                session.close();
                session = null;
            }
            capturing = true;
        } finally {
            lockSlim.writeLock().unlock();
        }

        checkSession();
    }
```
