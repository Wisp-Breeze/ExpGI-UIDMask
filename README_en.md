# This is an unfinished experimental tool

[Engage](README_en.md) |
[中文](README.md)

## 1.Project Description
### （1）Development Environment
*  JDK 26
*  Maven

### （2）How to operate
    Currently not supported for operation, still under development
---

## BitBlt截图器
- The code originates from BetterGI [[Original Document]](https://github.com/babalae/better-genshin-impact/tree/main/Fischless.GameCapture)
 
- Original language is **C#** ,This stage is being gradually translated into **JAVA**

- The author is a Java newbie, feel free to point out any mistakes.
## Partial Code Display


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
    public void Start(long hWnd, Map<String, Object> settings) {
        Object value;
        if (settings == null || !settings.containsKey("autoFixWin11BitBlt")) return;
        value = settings.get("autoFixWin11BitBlt");
        if (value != null && (Boolean) value) {
            BitBltRegistryHelper.SetDirectXUserGlobalSettings();
        }

        synchronized (this) {
            try {
                _hWnd = hWnd;
                if (_hWnd == 0) {return;}
                if (_session != null) {_session.close();}

                _session = null;
                this.setCapturing(true);
            } catch (Exception e) {e.getStackTrace();}
            finally {_rwLock.unlock();}

            CheckSession();
        }
    }
```
