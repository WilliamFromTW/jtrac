# JTrac 系统管理员完整指南 (Administrator Guide)

[English](ADMIN_GUIDE_en.md) | [繁體中文](ADMIN_GUIDE_zh-TW.md) | [简体中文](ADMIN_GUIDE_zh-CN.md) | [日本語](ADMIN_GUIDE_ja.md) | [Tiếng Việt](ADMIN_GUIDE_vi.md) | [Deutsch](ADMIN_GUIDE_de.md) | [Español](ADMIN_GUIDE_es.md) | [Français](ADMIN_GUIDE_fr.md)

---

## 目录
1. [首次登录与默认凭证](#一首次登录与默认凭证)
2. [系统初始化必填关键设置 (极重要)](#二系统初始化必填关键设置-极重要)
3. [系统架构与邮件流程图 (Mermaid)](#三系统架构与邮件流程图-mermaid)
4. [常用系统管理功能指引](#四常用系统管理功能指引)
5. [全系统备份、还原与防锁死机制 (System Backup & Restore)](#五全系统备份还原与防锁死机制-system-backup--restore)
6. [安全维护、数据库升级与日常运维建议](#六安全维护数据库升级与日常运维建议)
7. [Docker 容器化运维与数据备份指引 (Docker Operations & Volume Management)](#七docker-容器化运维与数据备份指引-docker-operations--volume-management)

---

## 一、首次登录与默认凭证

- **系统访问地址**：`http://<服务器IP>:<端口>/`（例如本地默认：`http://localhost:8888/`）
- **默认管理员账号 (Username)**：`admin`
- **默认管理员密码 (Password)**：`admin`

> [!WARNING]
> 首次登录成功后，请务必前往页面右上角点击 **OPTIONS** ➜ **Edit User Profile** 修改 `admin` 密码，切勿将默认凭证暴露在生产环境！

---

## 二、系统初始化必填关键设置 (极重要)

点击右上角 **OPTIONS** ➜ **Manage Settings**：

### 1. `jtrac.url.base`（系统基准网址 - 必填/极重要）
- **系统默认值**：`http://localhost/jtrac/`
- **必填推荐设置**：必须填入终端用户实际能访问的完整网址，**且结尾必须带有斜杠 `/`**（例如：`http://192.168.1.100:8888/` 或 `https://issues.yourcompany.com/`）。
- **重要说明**：所有通知信（新账号通知、重置密码链接、工单更新）的超链接均依赖此基准网址。若保持 `localhost`，外部收件人点击时将无法打开页面。

---

### 2. `locale.default`（默认系统语言）
- **系统默认值**：`en`（英文）
- 推荐设置：`zh_CN`、`zh_TW`、`ja`、`en`。

---

### 3. SMTP 邮件服务器设置
- `mail.server.host`：SMTP 服务器地址。
- `mail.server.port`：端口号（25、TLS 587、SSL 465）。
- `mail.server.username` 与 `mail.server.password`：认证账号密码。
- `mail.server.starttls.enable`：启用 TLS 请设为 `true`。
- `mail.from`：发件人邮箱。

---

### 4. 高级设置与分页配置
- `users.list.pageSize`：用户列表默认每页显示条数（默认 `25`，可选 10, 25, 50, 100, 全部）。
- `spaces.list.pageSize`：空间列表默认每页显示条数（默认 `25`，可选 10, 25, 50, 100, 全部）。
- `attachment.maxsize`：附件大小上限（MB，默认 `10`）。
- `session.timeout`：会话超时时间（秒，默认 `1800`）。

---

## 三、系统架构与邮件流程图 (Mermaid)

```mermaid
flowchart TD
    Start([启动 JTrac 服务]) --> Login[首次登录<br/>admin / admin]
    Login --> ChangePwd[修改管理员密码<br/>OPTIONS ➜ Edit User Profile]
    ChangePwd --> ConfigSettings[配置系统核心参数<br/>OPTIONS ➜ Manage Settings]
    
    subgraph CriticalSettings [必填核心设置]
        ConfigSettings --> SetUrlBase["设置 jtrac.url.base<br/>(例如: http://192.168.1.100:8888/)<br/>★ 防止邮件链接变成 localhost"]
        ConfigSettings --> SetLocale["设置 locale.default<br/>(例如: zh_CN)<br/>★ 界面语言设为中文"]
        ConfigSettings --> SetSMTP["配置 SMTP 邮件服务器<br/>(host / port / from)<br/>★ 启用通知与密码重置邮件"]
        ConfigSettings --> SetPaging["设置分页大小<br/>(users/spaces.list.pageSize)"]
    end
    
    CriticalSettings --> CreateSpaces[创建项目空间<br/>OPTIONS ➜ Manage Spaces]
    CreateSpaces --> CreateUsers[创建用户与分配角色<br/>OPTIONS ➜ Manage Users]
    CreateUsers --> Finish([系统正式上线运营])
```

---

## 四、常用系统管理功能指引

| 功能项目 | 说明 |
|---|---|
| **Edit User Profile** | 修改当前管理员邮箱、显示名称与密码。 |
| **Manage Users** | 用户账号管理：创建用户、分页浏览、重设密码、账号锁定与分配全局管理员权限。 |
| **Manage Spaces** | 项目空间管理：创建空间、分页浏览、自定义字段、状态与角色分配。 |
| **Configure Links** | 配置导航栏外部链接。 |
| **Manage Settings** | 配置核心参数（基础网址、SMTP、分页参数等）。 |
| **Rebuild Indexes** | 重建全文检索索引。 |
| **Export HTML** | 批量离线 HTML 导出与 ZIP 下载。 |
| **Backup & Restore** | 全系统备份与还原：超级管理员专属功能，支持一键下载数据库 JSON、整合 SQL 转储文件 (`jtrac-dump.sql`) 与附件之单文件 ZIP 备份包，并提供安全还原（具备自动安全快照与防锁死保护）。 |

---

## 五、全系统备份、还原与防锁死机制 (System Backup & Restore)

系统提供原生全系统灾难恢复与数据迁移机制，仅供最高管理员（SuperUser）使用：

1. **一键导出全系统备份包**：
   - 前往 **OPTIONS** ➜ **Backup & Restore (系统备份与还原)**。
   - 点击「**下载备份 (.zip)**」，系统将所有数据库实体序列化为跨数据库标准 JSON (`manifest.json` 与 `data/system_data.json`)，并同步生成包含通用 ANSI DDL、MySQL/PostgreSQL/HSQLDB 方言建表注释、14 张表外键拓扑排序 INSERT 语句与 Sequence 重置校准提示的独立 SQL 转储文件 `jtrac-dump.sql`，与物理附件目录（`${jtrac.home}/attachments/`）打包压缩为单个 `.zip` 文件供浏览器下载。
2. **安全还原引擎**：
   - 上传合法的 JTrac 备份 `.zip` 文件，勾选确认覆盖复选框，点击「**执行还原**」。
   - **自动创建服务端紧急快照 (Safety Snapshot)**：在清空任何现有数据前，系统自动在服务端 `${jtrac.home}/backups/` 目录创建当前系统完整快照，确保任何异常均可回退。
   - **操作者凭据防锁死保护 (Anti-Lockout Credential Shield)**：系统识别当前执行还原的管理员。即使备份包中管理员密码遗失或过旧，系统仍**强制保留当前操作者的活动密码哈希与最高管理员权限 (`ROLE_ADMIN`)**（若备份中无该用户则主动注入），彻底消除管理员被反锁在系统外的隐患。
   - **后台异步重建检索索引**：还原完成后，系统自动在后台触发 Lucene 全文检索全量重建，管理员 Session 保持有效无中断。

---

## 六、安全维护、数据库升级与日常运维建议

1. **密码安全性升级 (BCrypt & 混合迁移)**：
   - 升级至 Spring Security 5.8 与 BCrypt 散列算法。旧版 MD5 哈希在用户成功登录时自动升级为 BCrypt，无需手动重置密码。
2. **数据库升级 (从 2.3.3-1.0.0 升级)**：
   - 外部数据库（MySQL、PostgreSQL 等）：执行脚本 [`etc/sql/upgrade-to-2.0.0.sql`](../../etc/sql/upgrade-to-2.0.0.sql)。
   - 内置 HSQLDB：启动时自动备份并迁移至 HSQLDB 2.x。
3. **数据目录 (`jtrac.home`) 判定机制与定期备份**：
   - **`jtrac.home` 判定优先级 (4 级 Fallback)**：
     1. `WEB-INF/classes/jtrac-init.properties` 内部配置之 `jtrac.home`
     2. JVM 系统启动参数 `-Djtrac.home=...`（**生产环境推荐首选**）
     3. Servlet Context Init 参数（`web.xml` 或 Tomcat Context XML 中的 `jtrac.home`）
     4. **默认兜底 (Default Fallback)**：`System.getProperty("user.home") + "/.jtrac"`
   - **常见疑问解惑：为什么以前 Tomcat 在 Linux 下会默认存放在 `/root/.jtrac`？**
     当在 Linux 下以 `root` 账户启动 Tomcat 且未设置前 1~3 级参数时，Java 的 `user.home` 即为 `/root`，系统自动创建隐藏目录 `/root/.jtrac` 作为数据存储目录。若以普通服务用户 `jtrac` 启动则为 `/home/jtrac/.jtrac`。
   - **容器自定义指定路径示例**：
     - Linux Tomcat (`bin/setenv.sh`)：添加 `export CATALINA_OPTS="$CATALINA_OPTS -Djtrac.home=/var/jtrac-data"`
     - Windows Tomcat (`bin/setenv.bat`)：添加 `set "CATALINA_OPTS=%CATALINA_OPTS% -Djtrac.home=D:/jtrac-data"`
     - Jetty：启动命令带上 `-Djtrac.home=data`（如本地 `start-jtrac.bat`）
   - **目录结构与备份建议**：
     - `jtrac.properties`：数据库连接、URL、账号密码与 Hibernate 方言。
     - `db/`：内置 HSQLDB 数据库文件（若使用外部数据库请按常规备份）。
     - `attachments/`：物理附件目录（`${jtrac.home}/attachments/`），必须定期纳入备份排程。
     - `indexes/`：Lucene 全文索引目录（损毁时可随时由管理员在后台重建）。
     - `backups/`：全系统还原前自动生成的紧急安全快照（Safety Snapshot）。
     - 推荐定期通过 **OPTIONS** ➜ **Backup & Restore** 下载涵盖完整数据库与附件的备份包。
4. **附件纯项目 ID 分区存储与全文检索运维**：
   - **纯项目 ID 目录结构 (选项 C)**：附件全面存放于 `${jtrac.home}/attachments/{spaceId}/{prefix}_{filename}`，项目更名或变更代码完全不受影响。
   - **双轨查档安全网 (Dual-Read Fallback)**：读取文件时自动 Fallback 至根目录与孤儿隔离目录（`attachments/0_ORPHAN/`），确保升级过渡期 0% 下载断链 404。
   - **全文检索与安全防护参数**：系统支持 `.xlsx`、`.docx`、`.pdf`、`.txt`、`.csv`、`.md`、`.log` 全文索引，默认单文件上限 10MB、抽取上限 50,000 字符（可在 `config` 表调节）。
   - **重建索引与词干/前缀检索维护 (Rebuild Indexes & Search Optimization)**：
     - 系统采用增强型 `JtracAnalyzer`（整合标准分词、小写转换与 Porter 英文词干分析），自动对齐英文单复数与时态（例如输入 `window` 可精确命中包含 `Windows` 的附件与工单；输入 `test` 命中 `tests`/`testing`）。
     - 具备智能前缀备援机制（长度 >= 2 个字符之纯单字在查无精确结果时自动扩展为 `prefix*`，例如输入 `win` 自动比对 `win*`）。中文/CJK 字符维持标准 Unigram 切词，音标与全形字符维持原始精确度。
     - **升级后必要操作**：系统升级后，请管理员前往 **OPTIONS ➜ Rebuild Indexes** 执行一次索引重建，将现存工单与历史附件以新词干规则重新纳入 Lucene 索引库。

---

## 七、Docker 容器化运维与数据备份指引 (Docker Operations & Volume Management)

当 JTrac 运行于 Docker 容器环境时，建议系统管理员遵循以下运维准则：

### 1. 容器数据目录与 Volume 映射
所有数据库、附件与全局设置均持久化于容器内的 `/jtrac-data`：
- **命名 Volume 模式 (推荐)**：使用 `-v jtrac_data:/jtrac-data`。
- **本机目录映射模式**：使用 `-v /opt/jtrac/data:/jtrac-data`。容器 Entrypoint 在开机时会自动以 root 身份将目录所有人修正为 `jetty:jetty` (UID 999)，随后降权运行，无需在宿主机手动 `chown`。

### 2. Volume 定期冷热备份
管理员可直接对 Docker Volume 进行快速打包备份：
```bash
# 将 jtrac_data Volume 备份为 tar.gz 归档
docker run --rm -v jtrac_data:/data -v $(pwd):/backup alpine tar czvf /backup/jtrac_data_backup.tar.gz -C /data .

# 还原 Volume
docker run --rm -v jtrac_data:/data -v $(pwd):/backup alpine sh -c "rm -rf /data/* && tar xzvf /backup/jtrac_data_backup.tar.gz -C /data"
```

### 3. 连接外部关系型数据库 (MySQL / PostgreSQL / Oracle)
若不使用内置 HSQLDB，可在启动容器时注入数据库连接环境变量：
```bash
docker run -d \
  -p 8888:8080 \
  -v jtrac_data:/jtrac-data \
  -e DATABASE_URL="jdbc:mysql://db-server:3306/jtrac?useUnicode=true&characterEncoding=UTF-8" \
  -e DATABASE_DRIVER="com.mysql.cj.jdbc.Driver" \
  -e DATABASE_USERNAME="jtrac" \
  -e DATABASE_PASSWORD="your_password" \
  -e HIBERNATE_DIALECT="org.hibernate.dialect.MySQL8Dialect" \
  --name jtrac \
  jtrac:latest
```
容器启动时会自动将连接信息写入 `/jtrac-data/jtrac.properties`。
