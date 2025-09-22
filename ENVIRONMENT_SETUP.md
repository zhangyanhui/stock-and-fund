# 环境变量配置说明

## 概述

为了提高安全性，腾讯云相关的敏感配置信息已从 `application.properties` 迁移到环境变量中。

## 配置步骤

### 1. 创建环境变量文件

复制 `.env.example` 文件为 `.env`：

```bash
cp .env.example .env
```

### 2. 配置环境变量

编辑 `.env` 文件，填入实际的配置值：

```bash
# 腾讯云环境ID
TENCENT_CLOUD_ENV_ID=your_actual_env_id

# 腾讯云访问令牌
TENCENT_CLOUD_TOKEN=your_actual_token

# 基金函数名称
TENCENT_CLOUD_FUND_FUNCTION_NAME=getAllUserFunds
```

### 3. 运行应用

#### 方式一：使用 IDE 运行

在 IDE 中配置环境变量：
- IntelliJ IDEA: Run Configuration → Environment Variables
- Eclipse: Run Configuration → Environment tab

#### 方式二：命令行运行

```bash
# 设置环境变量后运行
export TENCENT_CLOUD_ENV_ID=your_actual_env_id
export TENCENT_CLOUD_TOKEN=your_actual_token
export TENCENT_CLOUD_FUND_FUNCTION_NAME=getAllUserFunds

# 运行应用
mvn spring-boot:run
```

#### 方式三：使用 .env 文件（需要额外配置）

如果需要自动加载 `.env` 文件，可以添加 `spring-boot-dotenv` 依赖：

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-boot-dotenv</artifactId>
    <version>2.5.4</version>
</dependency>
```

## 安全注意事项

1. **不要提交 `.env` 文件**：该文件已添加到 `.gitignore` 中
2. **定期更新令牌**：腾讯云访问令牌应定期更新
3. **权限控制**：确保只有必要的人员能访问生产环境的环境变量

## 环境变量说明

| 变量名 | 说明 | 默认值 | 是否必需 |
|--------|------|--------|----------|
| `TENCENT_CLOUD_ENV_ID` | 腾讯云环境ID | `spring-3go98zd4f98e1fb9` | 是 |
| `TENCENT_CLOUD_TOKEN` | 腾讯云访问令牌 | 无 | 是 |
| `TENCENT_CLOUD_FUND_FUNCTION_NAME` | 基金函数名称 | `getAllUserFunds` | 否 |

## 故障排除

### 应用启动失败

如果应用启动时提示找不到配置，请检查：

1. 环境变量是否正确设置
2. 变量名是否拼写正确
3. 令牌是否有效且未过期

### 功能异常

如果基金相关功能异常，请检查：

1. 腾讯云令牌是否有效
2. 环境ID是否正确
3. 函数名称是否匹配