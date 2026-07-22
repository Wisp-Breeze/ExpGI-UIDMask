# ExpGI-UIDMask

[English](README_en.md) |
[中文](README.md)

## 1.Project Description
### （1）Development Environment
*  JDK 26
*  Maven

### （2）Dependencies
*  **JNA** — Calls Windows native APIs (like GDI screenshot, window operations)
*  **JavaCV / OpenCV** — Handles the captured images
*  **Guava** — Some handy utilities

### （3）How to operate
    Currently not supported for operation, still under development
---

## BitBlt Capture
- The code originates from BetterGI [[Original Document]](https://github.com/babalae/better-genshin-impact/tree/main/Fischless.GameCapture)

- Original language is **C#** , the BitBlt mode has been translated to **JAVA** ✅

- The author is a Java newbie, feel free to point out any mistakes

## Partial Code Display

Here's the `Start` method, comparing the original C# and the translated Java:

### Original Text C#
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

### Translation JAVA
```java
    @Override
    public void start(long hWnd, Map<String, Object> settings) {
        // Optional: Win11 BitBlt fix
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