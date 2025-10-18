@echo off
echo "===== Stop TOMCAT server ====="
ssh base_43  "service tomcat stop"
echo "===== Remove MDM frontend folder ====="
ssh base_43  "rm -rf /opt/tomcat/webapps/ROOT"
echo "===== Deploy MDM service ====="
scp .\base-tomcat\ROOT.xml base_43:/opt/tomcat/conf/Catalina/localhost/ROOT.xml
scp .\server\target\launcher.war base_43:/opt/tomcat/webapps/ROOT.war
echo "===== Start TOMCAT server ====="
ssh base_43 "service tomcat start && tail -f /opt/tomcat/logs/catalina.out "