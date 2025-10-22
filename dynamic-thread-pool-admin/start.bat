@echo off
echo ========================================
echo  动态线程池监控平台 - 启动脚本
echo ========================================
echo.

echo [1/3] 清理并编译项目...
call mvn clean compile -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [错误] 编译失败，请检查代码！
    pause
    exit /b 1
)

echo.
echo [2/3] 启动应用...
echo.

start "动态线程池监控平台" mvn spring-boot:run

echo [3/3] 等待应用启动...
timeout /t 10 /nobreak

echo.
echo ========================================
echo  启动完成！
echo ========================================
echo  访问地址: http://localhost:8092
echo  Swagger: http://localhost:8092/swagger-ui.html
echo ========================================
echo.
echo 按任意键打开浏览器...
pause

start http://localhost:8092

echo.
echo 应用正在后台运行，关闭此窗口不会停止应用
pause

