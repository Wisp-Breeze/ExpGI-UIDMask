# 这是一个未完成的 实验性工具

## 1.项目说明
### （1）开发环境
*  JDK 26
*  Maven

### （2）如何运行
    它暂时不支持运行，仍在开发中

<<<<<<< HEAD
---

## BitBlt截图器
### 代码来源自BetterGI[[原文件]](https://github.com/babalae/better-genshin-impact/tree/main/Fischless.GameCapture)，原为 **C#** 语言，本阶段正在逐步翻译为 **JAVA**

## 部分代码展示

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