# 使用多阶段构建，减少最终镜像体积

# ----------------------------------------
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 4. 从构建阶段复制 JAR 文件（显式重命名）
COPY /target/*.jar app.jar

# 5. 安全优化：使用非 root 用户运行
RUN useradd -m appuser && chown -R appuser /app
USER appuser

EXPOSE 8080

# 6. 使用 exec 形式启动
ENTRYPOINT ["java", "-jar", "app.jar"]