#!/bin/bash

echo "========================================"
echo " 动态线程池监控平台 - 启动脚本"
echo "========================================"
echo ""

echo "[1/3] 清理并编译项目..."
mvn clean compile -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 编译失败，请检查代码！"
    exit 1
fi

echo ""
echo "[2/3] 启动应用..."
echo ""

# 后台启动
nohup mvn spring-boot:run > /dev/null 2>&1 &

echo "[3/3] 等待应用启动..."
sleep 10

echo ""
echo "========================================"
echo " 启动完成！"
echo "========================================"
echo " 访问地址: http://localhost:8092"
echo " Swagger: http://localhost:8092/swagger-ui.html"
echo "========================================"
echo ""

# 尝试打开浏览器
if command -v xdg-open > /dev/null; then
    xdg-open http://localhost:8092
elif command -v open > /dev/null; then
    open http://localhost:8092
else
    echo "请手动在浏览器中打开: http://localhost:8092"
fi

echo ""
echo "应用正在后台运行"
echo "查看日志: tail -f nohup.out"
echo "停止应用: pkill -f spring-boot:run"

