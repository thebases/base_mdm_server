copy /Y C:\Git\thebase\base_mdm_server\server\target\launcher.war C:\Git\thebase\base_mdm_server\tomcat_server\webapps\ROOT.war
set CATALINA_HOME=C:\tools\tomcat9
set CATALINA_BASE=C:\Git\thebase\base_mdm_server\tomcat_server
cd base-mdm
%CATALINA_HOME%\bin\catalina.bat run
cd ..