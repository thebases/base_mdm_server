@echo off
ssh base_43  "service tomcat stop"
scp C:\Git\thebase\base_mdm_server\server\target\launcher.war base_43:/opt/tomcat/webapps/ROOT.war
ssh base_43 "service tomcat start && tail -f /opt/tomcat/logs/catalina.out "