# LLM Chat - 大语言模型聊天桌面应用 Wiki

---

## 一、项目概述

**LLM Chat** 是一个基于 Java SE 开发的大语言模型（LLM）聊天桌面应用程序。它通过 OpenAI 兼容的 API 接口与大语言模型进行实时对话，支持流式响应（SSE）、多会话管理、配置持久化等功能。

### 1.1 技术栈

| 技术       | 说明                         |
|------------|------------------------------|
| Java SE    | 核心运行环境，无第三方依赖   |
| Swing      | GUI 框架，构建桌面界面       |
| HttpURLConnection | HTTP 网络请求         |
| Java 序列化 | 数据持久化存储              |
| 多线程     | 后台网络请求 + EDT UI 更新   |

### 1.2 核心特性

- **流式对话**：基于 SSE（Server-Sent Events）实现逐字输出的流式响应体验
- **多会话管理**：支持创建、切换、删除多个独立聊天会话
- **配置持久化**：API URL、API Key、模型名称等配置自动保存
- **现代 UI 设计**：自定义圆角按钮、消息气泡、加载动画等现代化组件
- **跨平台支持**：纯 Java 标准库实现，可在 Windows / macOS / Linux 上运行
- **零依赖**：不依赖任何第三方库，全部使用 Java 标准库

---

## 二、目录结构说明

```
java-llm-interface/
├── src/                          # 源代码目录
│   └── com/llmchat/
│       ├── App.java              # 程序主入口
│       ├── config/               # 配置管理模块
│       │   ├── ConfigManager.java      # 应用配置持久化
│       │   └── ChatHistoryManager.java # 聊天历史持久化
│       ├── http/                 # 网络通信模块
│       │   └── HttpClient.java         # HTTP 客户端（流式请求）
│       ├── model/                # 数据模型层
│       │   ├── ChatMessage.java        # 消息实体
│       │   ├── ChatSession.java        # 会话实体
│       │   └── Config.java             # 配置实体
│       └── ui/                   # 用户界面层
│           ├── MainFrame.java          # 主窗口
│           ├── ChatPanel.java          # 聊天面板（核心交互）
│           ├── SettingsDialog.java     # 设置对话框
│           └── components/       # 自定义 UI 组件
│               ├── ChatListPanel.java      # 侧边栏聊天列表
│               ├── MessageBubble.java      # 消息气泡
│               ├── ModernButton.java       # 现代化按钮
│               ├── ModernTextField.java    # 现代化单行文本框
│               ├── ModernTextArea.java     # 现代化多行文本区域
│               └── LoadingIndicator.java   # 加载动画指示器
├── bin/                          # 编译输出目录（.class 文件）
├── .llmchat/                     # 本地配置与数据目录
│   ├── config.dat                # 应用配置文件（序列化）
│   └── 答辩问题库.md             # 项目答辩问题库
├── wiki/                         # 项目文档目录
├── compile.bat                   # Windows CMD 编译脚本
├── compile.ps1                   # PowerShell 编译脚本
├── run.bat                       # Windows CMD 运行脚本
├── run.ps1                       # PowerShell 运行脚本
└── .gitignore                    # Git 忽略规则
```

### 2.1 目录职责说明

| 目录/文件          | 职责                                                         |
|--------------------|--------------------------------------------------------------|
| `src/com/llmchat/` | 项目根包，包含程序入口 `App.java`                            |
| `config/`          | 配置管理模块，负责应用配置和聊天历史的序列化存储与加载       |
| `http/`            | 网络通信模块，封装 HTTP 请求发送和 SSE 流式响应解析          |
| `model/`           | 数据模型层，定义消息、会话、配置等核心数据结构               |
| `ui/`              | 用户界面层，包含主窗口、聊天面板、设置对话框                 |
| `ui/components/`   | 可复用的自定义 UI 组件，如按钮、文本框、消息气泡等           |
| `bin/`             | 编译产物目录，存放 `.class` 字节码文件                       |
| `.llmchat/`        | 运行时数据目录，存放序列化配置和历史文件                     |

---

## 三、核心模块介绍

### 3.1 配置管理模块（`config` 包）

该模块负责应用配置和聊天历史的持久化存储，采用 Java 序列化机制将对象保存为二进制 `.dat` 文件。

#### 3.1.1 ConfigManager — 应用配置管理器

**文件**：`src/com/llmchat/config/ConfigManager.java`

**职责**：管理应用级配置（API URL、API Key、模型名称）的加载、保存与更新。

**核心机制**：
- 配置文件路径：`{工作目录}/.llmchat/config.dat`
- 使用 `ObjectInputStream` / `ObjectOutputStream` 进行序列化读写
- 加载失败时自动创建默认配置（优雅降级）
- 提供 Base64 编码/解码方法用于 API Key 的简单混淆存储

**关键方法**：

| 方法 | 说明 |
|------|------|
| `loadConfig()` | 从磁盘加载配置，失败则创建默认配置 |
| `saveConfig()` | 将当前配置序列化到磁盘 |
| `getConfig()` | 获取当前配置对象 |
| `updateConfig(apiUrl, apiKey, modelName)` | 更新配置并自动保存 |
| `encryptApiKey(apiKey)` | Base64 编码 API Key |
| `decryptApiKey(encrypted)` | Base64 解码 API Key |

#### 3.1.2 ChatHistoryManager — 聊天历史管理器

**文件**：`src/com/llmchat/config/ChatHistoryManager.java`

**职责**：管理所有聊天会话的增删查改及持久化存储。

**存储结构**：
- 索引文件：`~/.llmchat/history/index.dat` — 保存会话 ID 的有序列表
- 会话文件：`~/.llmchat/history/{sessionId}.dat` — 每个会话独立存储

**数据结构**：
- `ConcurrentHashMap<String, ChatSession>` — 会话 ID 到会话对象的映射，支持高并发读写
- `Collections.synchronizedList(ArrayList)` — 维护会话的显示顺序（新会话在前）

**关键方法**：

| 方法 | 说明 |
|------|------|
| `loadHistory()` | 加载索引和所有会话，损坏时优雅降级 |
| `saveHistory()` | 保存索引和所有会话到磁盘 |
| `createNewSession()` | 创建新会话并持久化 |
| `getSession(sessionId)` | 根据 ID 获取指定会话 |
| `getAllSessions()` | 按时间顺序返回所有会话列表 |
| `deleteSession(sessionId)` | 删除会话及其磁盘文件 |
| `addMessageToSession(sessionId, message)` | 添加消息并自动更新会话标题 |
| `clearSessionMessages(sessionId)` | 清空会话中的全部消息 |

**容错策略**：
- 单个会话文件损坏 → 跳过该会话，不影响其他会话加载
- 索引文件损坏 → 清除所有会话，创建新的默认会话
- 无任何会话时 → 自动创建一个默认会话

---

### 3.2 HTTP 客户端模块（`http` 包）

#### 3.2.1 HttpClient — HTTP 流式客户端

**文件**：`src/com/llmchat/http/HttpClient.java`

**职责**：向 OpenAI 兼容 API 发送流式请求，并通过回调接口逐步返回响应内容。

**核心流程**：
1. 在独立后台线程中发起 HTTP POST 请求
2. 设置请求头：`Content-Type: application/json`、`Authorization: Bearer {key}`、`Accept: text/event-stream`
3. 手动构建 JSON 请求体（使用 `StringBuilder` 拼接）
4. 逐行读取 SSE 流式响应，解析 `data:` 前缀的 JSON 片段
5. 提取 `"content"` 字段值并通过 `callback.onChunk()` 回调
6. 收到 `[DONE]` 标记或流结束时调用 `callback.onComplete()`
7. 异常时调用 `callback.onError()` 通知上层

**回调接口 `StreamCallback`**：

```java
public interface StreamCallback {
    void onChunk(String chunk);    // 收到一段响应内容
    void onComplete();             // 响应完成
    void onError(String error);    // 发生错误
}
```

**超时配置**：
- 连接超时：30 秒（`connectTimeout`）
- 读取超时：60 秒（`readTimeout`）

**JSON 处理**：
- `escapeJson()` — 转义 `\`、`"`、`\n`、`\r`、`\t` 等特殊字符，防止破坏 JSON 结构
- `unescapeJson()` — 将转义序列还原为原始字符
- `parseStreamContent()` — 从 JSON 片段中提取 `"content"` 字段值（手动解析，无第三方库依赖）

---

### 3.3 数据模型模块（`model` 包）

该模块定义了应用的三个核心数据实体，均实现 `Serializable` 接口以支持序列化持久化。

#### 3.3.1 ChatMessage — 消息模型

**文件**：`src/com/llmchat/model/ChatMessage.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | `Role` (枚举) | 消息角色：`USER` / `ASSISTANT` / `SYSTEM` |
| `content` | `String` | 消息文本内容 |
| `timestamp` | `long` | 消息创建时间戳（毫秒） |

**枚举 `Role`**：定义了三种消息角色，提供类型安全，避免字符串拼写错误。`getRoleString()` 方法返回小写角色名用于 API 请求。

#### 3.3.2 ChatSession — 会话模型

**文件**：`src/com/llmchat/model/ChatSession.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` | 会话唯一标识（UUID） |
| `title` | `String` | 会话标题（默认取第一条用户消息前 20 字） |
| `createdAt` | `long` | 创建时间戳 |
| `updatedAt` | `long` | 最后更新时间戳 |
| `messages` | `List<ChatMessage>` | 消息列表 |

**关键方法**：
- `addMessage()` — 添加消息并自动更新 `updatedAt`
- `getFirstMessagePreview()` — 提取第一条用户消息的前 20 字符作为会话标题预览
- `clearMessages()` — 清空消息列表

#### 3.3.3 Config — 配置模型

**文件**：`src/com/llmchat/model/Config.java`

| 字段 | 类型 | 说明 |
|------|------|------|
| `apiUrl` | `String` | API 接口地址（默认 OpenAI） |
| `apiKey` | `String` | API 密钥 |
| `modelName` | `String` | 模型名称（默认 `gpt-3.5-turbo`） |

**验证方法**：`isValid()` 检查三个字段是否均非空，用于发送消息前的配置校验。

---

### 3.4 用户界面模块（`ui` 包）

采用简化的 MVC 模式，UI 层负责界面展示和用户交互，通过监听器接口与其他模块通信。

#### 3.4.1 MainFrame — 主窗口

**文件**：`src/com/llmchat/ui/MainFrame.java`

**职责**：应用程序主界面，整合侧边栏和聊天面板。

**界面布局**：
```
┌────────────────────────────────────────────┐
│  标题栏 (LLM Chat)            [设置] 按钮  │  ← Header (BorderLayout.NORTH)
├──────────┬─────────────────────────────────┤
│          │                                 │
│ 侧边栏   │      聊天面板                   │  ← JSplitPane
│ ChatList │      ChatPanel                  │     (分割位置 250px)
│ Panel    │                                 │
│          │                                 │
└──────────┴─────────────────────────────────┘
```

**窗口属性**：
- 默认尺寸：1100 × 750 像素
- 最小尺寸：800 × 600 像素
- 居中显示

**事件处理**：
- 监听 `ChatListListener` 处理会话选择、删除、新建事件
- 监听 `ChatPanelListener` 在会话更新时刷新侧边栏列表
- 窗口关闭时弹出确认对话框，保存历史数据后退出

#### 3.4.2 ChatPanel — 聊天面板

**文件**：`src/com/llmchat/ui/ChatPanel.java`

**职责**：核心聊天交互界面，处理消息发送、流式接收、UI 更新。

**界面结构**：
- **消息显示区**：`JScrollPane` 内嵌 `BoxLayout(Y_AXIS)` 面板，纵向排列消息气泡
- **输入区域**：底部 `ModernTextArea` + 发送按钮
- **欢迎页**：会话无消息时显示"欢迎使用 LLM Chat"提示

**消息发送流程**：
1. 校验输入非空、未在加载中、配置有效
2. 创建 `ChatMessage(USER)` 并添加到历史记录和 UI
3. 通知监听器刷新侧边栏
4. 清空输入框，设置发送按钮为加载状态
5. 创建空的助手消息气泡占位
6. 调用 `HttpClient.sendStreamRequest()` 发起请求
7. `onChunk` → 追加文本到气泡（EDT 线程）
8. `onComplete` → 保存完整响应，恢复 UI 状态
9. `onError` → 移除空气泡，弹出错误对话框

**线程安全**：所有 HTTP 回调通过 `SwingUtilities.invokeLater()` 确保 UI 更新在 EDT 线程执行。

#### 3.4.3 SettingsDialog — 设置对话框

**文件**：`src/com/llmchat/ui/SettingsDialog.java`

**职责**：模态对话框，供用户配置 API 连接参数。

**配置项**：
- **API URL**：接口地址（默认 `https://api.openai.com/v1/chat/completions`）
- **API Key**：密钥输入（使用 `JPasswordField` 隐藏显示）
- **模型选择**：下拉框预设 `gpt-3.5-turbo`、`gpt-4`、`gpt-4-turbo`、`gpt-4o`、`gpt-4o-mini`，支持选择"自定义..."手动输入

---

### 3.5 自定义 UI 组件（`ui/components` 包）

#### 3.5.1 MessageBubble — 消息气泡

**文件**：`src/com/llmchat/ui/components/MessageBubble.java`

**功能**：以圆角气泡样式展示聊天消息，区分用户和助手角色。

| 属性 | 用户消息 | 助手消息 |
|------|----------|----------|
| 背景色 | 蓝色 `(0,122,255)` | 浅灰 `(240,240,240)` |
| 文字色 | 白色 | 深色 `(30,30,30)` |
| 对齐方式 | 右对齐 | 左对齐 |
| 最大宽度 | 500px | 500px |

**实现要点**：
- 重写 `paintComponent()` 使用 `Graphics2D` + `RoundRectangle2D` 绘制圆角背景
- 使用 `JTextPane` + `StyledDocument` 渲染富文本
- `getPreferredSize()` 自适应计算气泡尺寸（根据文本内容自动换行）
- `appendContent()` 支持流式追加文本并触发级联布局更新
- 内置 Emoji 字体检测，依次尝试 `Segoe UI Emoji`、`Apple Color Emoji`、`Noto Color Emoji` 等

#### 3.5.2 ChatListPanel — 聊天列表侧边栏

**文件**：`src/com/llmchat/ui/components/ChatListPanel.java`

**功能**：侧边栏展示所有会话列表，支持选择、新建、删除操作。

**回调接口 `ChatListListener`**：
- `onSessionSelected(sessionId)` — 点击会话项
- `onSessionDeleted(sessionId)` — 点击删除按钮（带确认对话框）
- `onNewChatClicked()` — 点击"+ 新对话"按钮

**列表项**：每个列表项（`ChatItemPanel`）显示会话标题、消息数量，支持悬停高亮和选中高亮效果。

#### 3.5.3 ModernButton — 现代化按钮

**文件**：`src/com/llmchat/ui/components/ModernButton.java`

**功能**：圆角蓝色按钮，支持悬停/按下颜色变化和加载动画。

- 正常状态：蓝色 `(0,122,255)`
- 悬停状态：深蓝 `(0,102,204)`
- 按下状态：更深蓝 `(0,82,184)`
- 加载状态：禁用按钮，显示三个动画圆点代替文字

#### 3.5.4 ModernTextField — 现代化单行文本框

**文件**：`src/com/llmchat/ui/components/ModernTextField.java`

**功能**：带焦点高亮边框的单行文本输入框。获得焦点时边框变为蓝色加粗，失去焦点恢复灰色。

#### 3.5.5 ModernTextArea — 现代化多行文本区域

**文件**：`src/com/llmchat/ui/components/ModernTextArea.java`

**功能**：包装在 `JScrollPane` 中的多行文本区域，支持自动换行、焦点高亮边框。提供 `getText()`、`setText()`、`append()`、`clear()` 等便捷方法。

#### 3.5.6 LoadingIndicator — 加载动画指示器

**文件**：`src/com/llmchat/ui/components/LoadingIndicator.java`

**功能**：三个蓝色圆点的循环加载动画。使用 `javax.swing.Timer` 每 200ms 切换当前活跃点，通过不同透明度（1.0 / 0.6 / 0.3）产生渐变动画效果。

---

## 四、主要类功能说明

### 4.1 类关系总览

```
App (程序入口)
 └── MainFrame (主窗口)
      ├── ConfigManager (配置管理)
      │    └── Config (配置模型)
      ├── ChatHistoryManager (历史管理)
      │    └── ChatSession → ChatMessage (会话/消息模型)
      ├── ChatListPanel (侧边栏列表)
      │    └── ChatItemPanel (列表项)
      ├── ChatPanel (聊天面板)
      │    ├── HttpClient (HTTP 客户端)
      │    ├── MessageBubble (消息气泡)
      │    ├── ModernTextArea (输入框)
      │    └── ModernButton (发送按钮)
      └── SettingsDialog (设置对话框)
           ├── ModernTextField (文本框)
           └── ModernButton (保存/取消按钮)
```

### 4.2 关键接口一览

| 接口 | 定义位置 | 方法 | 用途 |
|------|----------|------|------|
| `HttpClient.StreamCallback` | `HttpClient.java` | `onChunk` / `onComplete` / `onError` | 流式响应回调 |
| `ChatPanel.ChatPanelListener` | `ChatPanel.java` | `onSessionUpdated` | 会话内容变更通知 |
| `ChatListPanel.ChatListListener` | `ChatListPanel.java` | `onSessionSelected` / `onSessionDeleted` / `onNewChatClicked` | 列表交互事件 |

### 4.3 类详细说明

#### App.java — 程序入口

- 设置系统原生外观（`UIManager.setLookAndFeel`）
- 使用 `SwingUtilities.invokeLater()` 在 EDT 线程中创建并显示主窗口

#### MainFrame.java — 主窗口

- 持有 `ConfigManager` 和 `ChatHistoryManager` 实例
- 通过 `JSplitPane` 组合侧边栏和聊天面板
- 管理窗口关闭事件（确认退出 + 保存数据）
- `refreshChatList()` 同步侧边栏列表与后端数据

#### ChatPanel.java — 聊天面板

- 持有 `HttpClient` 实例用于发送请求
- 维护 `messageHistory` 列表和 `currentSessionId` 状态
- 支持 Enter 键发送消息（Shift+Enter 换行）
- 使用 `StringBuilder` 累积流式响应内容

#### HttpClient.java — HTTP 客户端

- 每次请求在 `new Thread()` 中异步执行
- 手动构建 JSON 请求体和解析 SSE 响应（零依赖）
- 支持完整的错误流读取和异常通知

---

## 五、编译和运行指南

### 5.1 环境要求

- **JDK**：Java 8 或更高版本
- **操作系统**：Windows / macOS / Linux
- **无需构建工具**：项目不依赖 Maven / Gradle

### 5.2 使用 CMD 脚本（Windows）

**编译**：
```cmd
compile.bat
```
该脚本执行以下操作：
1. 创建 `bin/` 输出目录
2. 扫描 `src/` 下所有 `.java` 文件
3. 使用 `javac -encoding UTF-8 -d bin` 编译

**运行**：
```cmd
run.bat
```
执行 `java -cp bin com.llmchat.App` 启动应用。

### 5.3 使用 PowerShell 脚本

**编译**：
```powershell
.\compile.ps1
```
与 CMD 版本功能相同，使用 PowerShell 语法实现，支持 UTF-8 编码输出。

**运行**：
```powershell
.\run.ps1
```

### 5.4 手动编译运行

如果不使用脚本，可以直接执行以下命令：

```bash
# 编译
mkdir bin
javac -encoding UTF-8 -d bin src/com/llmchat/**/*.java src/com/llmchat/*.java

# 运行
java -cp bin com.llmchat.App
```

### 5.5 使用 IDE 运行

在 IntelliJ IDEA 或 Eclipse 中：
1. 将 `src/` 目录标记为 Sources Root
2. 直接运行 `App.java` 的 `main` 方法

---

## 六、项目配置和设置说明

### 6.1 数据存储位置

| 数据类型 | 存储路径 | 格式 |
|----------|----------|------|
| 应用配置 | `{工作目录}/.llmchat/config.dat` | Java 序列化二进制 |
| 聊天索引 | `~/.llmchat/history/index.dat` | Java 序列化二进制 |
| 会话数据 | `~/.llmchat/history/{uuid}.dat` | Java 序列化二进制 |

> **注意**：`ConfigManager` 使用 `System.getProperty("user.dir")` 获取工作目录，而 `ChatHistoryManager` 使用 `System.getProperty("user.home")` 获取用户主目录。

### 6.2 应用配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| API URL | LLM API 接口地址 | `https://api.openai.com/v1/chat/completions` |
| API Key | API 访问密钥 | 空（需用户配置） |
| 模型名称 | 使用的模型 | `gpt-3.5-turbo` |

### 6.3 配置方法

1. 启动应用后，点击右上角 **"设置"** 按钮
2. 填写 API URL、API Key
3. 从下拉框选择模型，或选择"自定义..."手动输入
4. 点击 **"保存"** 按钮

### 6.4 API Key 安全说明

当前实现使用 Base64 编码存储 API Key。这**不是真正的加密**，仅提供基本的混淆保护。任何能访问 `.dat` 文件的人都可以轻松解码。如需更高安全性，建议使用 AES 加密或操作系统凭据管理。

### 6.5 兼容的 API 服务

项目默认兼容 OpenAI Chat Completions API 格式，任何支持以下特性的 API 均可使用：
- POST 请求，JSON 格式请求体
- 包含 `model`、`messages`、`stream` 字段
- SSE 流式响应，`data:` 前缀格式
- 响应 JSON 中包含 `choices[0].delta.content` 字段

---

## 七、使用示例和注意事项

### 7.1 基本使用流程

1. **启动应用** → 显示欢迎界面
2. **配置 API** → 点击"设置"，填入 API URL、Key 和模型
3. **发送消息** → 在底部输入框输入文本，按 Enter 或点击"发送"
4. **查看回复** → AI 回复以流式方式逐字显示在灰色气泡中
5. **管理会话** → 点击侧边栏"+ 新对话"创建新会话，点击列表项切换会话

### 7.2 快捷键

| 快捷键 | 功能 |
|--------|------|
| `Enter` | 发送消息 |
| `Shift + Enter` | 输入换行 |

### 7.3 消息气泡说明

- **蓝色气泡（右对齐）**：用户发送的消息
- **灰色气泡（左对齐）**：AI 助手的回复
- 气泡最大宽度 500px，超出自动换行
- 气泡高度根据内容自适应

### 7.4 注意事项

1. **首次使用必须配置 API Key**：未配置时发送消息会提示"请先配置API设置"
2. **网络超时**：连接超时 30 秒，读取超时 60 秒。网络不稳定时可能触发超时错误
3. **数据持久化时机**：
   - 发送/接收消息后立即保存
   - 创建/删除会话后立即保存
   - 退出应用时保存
4. **并发防护**：AI 响应期间发送按钮被禁用，防止重复发送
5. **会话标题**：自动取第一条用户消息的前 20 个字符作为标题，超过则截断并加 `...`
6. **错误处理**：请求失败时会弹出错误对话框，并自动从历史中移除该条用户消息

### 7.5 已知局限

- **JSON 手动解析**：未使用 JSON 库，特殊字符处理可能不完善
- **API Key 存储**：Base64 编码非真正加密，安全性有限
- **Emoji 支持**：依赖系统字体，某些平台可能显示不完整
- **Markdown 渲染**：AI 回复中的 Markdown 格式以纯文本显示
- **长文本性能**：`JTextPane` 处理大量文本时可能性能下降
- **Emoji 截断**：`getFirstMessagePreview()` 按 `char` 计数截取，可能在 Emoji 代理对中间截断导致乱码

### 7.6 项目改进建议

- 引入 Gson/Jackson 替代手动 JSON 解析
- 使用 AES 加密 API Key
- 添加 Markdown 渲染支持
- 支持会话导出（Markdown / PDF）
- 添加暗黑模式主题切换
- 使用 AI 自动生成会话标题
- 添加快捷键支持（Ctrl+N 新建会话等）
- 使用 `SwingWorker` 替代手动线程管理
