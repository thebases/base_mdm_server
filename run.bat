copy /Y C:\Git\thebase\base_mdm_server\server\target\launcher.war C:\Git\thebase\base_mdm_server\server\webapps\server_war.war
set CATALINA_HOME=C:\tools\tomcat9
set CATALINA_BASE=C:\Git\thebase\base_mdm_server\server
%CATALINA_HOME%\bin\catalina.bat run