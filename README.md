# Luckey小六壬测算 Android App

一个原生 Kotlin Android 应用，根据用户提供的图片公式计算小六壬结果。

## 功能
- 自动使用手机当前日期和时间
- 自选公历日期、时间
- 自动转换农历月日
- 自动将时间转换为十二时辰序号
- 按 `(农历月 + 农历日 + 时辰序号) - 2` 计算
- 显示余数、结果名称和解释
- 六种结果按钮：大安、留连、速喜、赤口、小吉、空亡
- GitHub Actions 自动编译 Debug APK

<img width="1145" height="1374" alt="中文手相标注图" src="https://github.com/user-attachments/assets/9d5c5583-9416-4271-8629-835afef9e5a1" />


## 计算映射
| 余数 | 结果 |
|---:|---|
| 1 | 大安 |
| 2 | 留连 |
| 3 | 速喜 |
| 4 | 赤口 |
| 5 | 小吉 |
| 0 | 空亡 |

时辰序号：子=1、丑=2、寅=3、卯=4、辰=5、巳=6、午=7、未=8、申=9、酉=10、戌=11、亥=12。

## 编译
1. 将本项目上传到 GitHub 仓库。
2. 在仓库的 **Actions** 页面运行 `Build Android APK`，或向 `main` / `master` 分支推送代码。
3. 在对应 workflow run 页面下载 `xiaoliuren-debug-apk` artifact。
4. 解压后安装 `app-debug.apk`。

也可以用 Android Studio 打开项目后运行，或在安装了 JDK 17 和 Gradle 的环境执行：
```bash
gradle assembleDebug
```

## 说明
这是按图片中提供的规则实现的民俗文化工具，仅供个人参考与娱乐。不同流派的日期换算、子时划分规则可能不同。


新增功能：支持输入一个或多个字的笔画数（例如 `8，12，6`），自动求和后除以 6 取余，并显示六宫结果及民俗宜忌建议。应用显示名称为 `luckeycount`，启动图标使用上传的白狗头像。宜忌内容仅作民俗文化娱乐参考。
