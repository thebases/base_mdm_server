@echo off
echo "===== Create Base MDM data folder on remote server ====="
ssh base_43  "mkdir -p /opt/base-mdm/server/files"
ssh base_43  "mkdir -p /opt/base-mdm/server/plugins"
ssh base_43  "mkdir -p /opt/base-mdm/logs"
ssh base_43  "chown tomcat: /opt/base-mdm/ -R"
scp .\base-mdm\server\*.* base_43:/opt/base-mdm/server/