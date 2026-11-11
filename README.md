# 智慧思政课表（Smart Course Lite）

一个轻量的校园课表 / 签到 / 卫生检查客户端，纯 Java + Android View 体系实现，不依赖任何第三方库。

> 非官方客户端，与学校无关。仅作个人学习与自用工具。

## 功能

- **课表**：按天查看课程，七日横向选择器 + 左右滑动切换日期，日历选日期，周次显示与前后周切换
- **周次总览**：以课程表网格展示整周课程，支持上下周切换、课程详情、午休间隙
- **签到**：手动签到码签到、统计与历史记录、推荐位置（高级模式可自定义坐标）
- **卫生检查**：检查记录、照片浏览（全屏分页）、本学期过滤、顶栏折叠
- **登录态**：凭证加密存储（AndroidKeyStore + AES/GCM）、静默续期（隐藏 WebView 走 CAS 单点登录）
- **关于**：开发者信息、问题反馈邮箱、免责声明、MIT 许可、捐赠入口

## 界面与动效

- 大标题 + 副题的 iOS 风格首页，日期区四层结构
- iOS 时间轴式课程列表：开始/结束时间、节次、教室、教师，进行中高亮，已结束降对比
- 底栏半透明胶囊 + 不透明高亮块 + 回弹按压反馈
- 入场动画：白卡带着第一条课程从左滑入并逐条长高，课程 1.5s 单条、500ms 错开；周视图课程按天自上而下依次飞入

## 构建

本项目不是标准 Gradle 工程，而是「纯 javac 编译 + 重组 APK」的构建方式（因此不依赖 Android Studio）：

```bash
# 1. 编译（需要 android.jar，JDK 8 语法级别）
javac --release 8 -encoding UTF-8 \
  -classpath /path/to/android.jar \
  -d classes $(find src -name '*.java')

# 2. dex
java -cp d8.jar com.android.tools.r8.D8 --release --min-api 21 \
  --lib /path/to/android.jar --output dexout $(find classes -name '*.class')

# 3. 把 classes.dex 打进一个基座 APK（替换资源/清单/图标），再对齐、签名
apksigner sign --ks your.jks --ks-key-alias yourkey --out app.apk app-aligned.apk
```

基座 APK、签名文件与构建脚本（清单补丁、图标注入、资源对齐）不随仓库分发，请自备。

## 免责声明

- 本项目为个人自用的非官方客户端，与学校及其他任何机构无关；
- 登录凭证只保存在本机（加密存储），不会上传到任何第三方服务器；
- 使用本软件产生的任何后果由使用者自行承担；
- 请遵守学校相关管理规定，不要用于任何违规用途。

## 许可证

[MIT](LICENSE)

## 捐赠

如果这个项目对你有帮助，可以无偿捐赠支持开发者。应用内「关于此项目 → 点击捐赠」可查看收款码。

问题反馈：weirenwuwanren@qq.com
