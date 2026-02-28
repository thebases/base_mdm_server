@echo off
echo "===== Stop TOMCAT server ====="
ssh %1  "service tomcat stop"
echo "===== Remove MDM frontend folder ====="
ssh %1  "rm -rf /opt/tomcat/webapps/ROOT"
echo "===== Deploy MDM service ====="
@REM scp .\base-tomcat\ROOT.xml base_43:/opt/tomcat/conf/Catalina/localhost/ROOT.xml
scp .\server\target\launcher.war %1:/opt/tomcat/webapps/ROOT.war
echo "===== Start TOMCAT server ====="
ssh %1 "service tomcat start && tail -f /opt/tomcat/logs/catalina.out "