copy /Y C:\Git\thebase\base_mdm_server\install\sql\bmdm_init.en.sql C:\Git\thebase\base_mdm_server\tomcat_server\base\bmdm_init.en.sql
@REM copy /Y C:\Git\thebase\base_mdm_server\install\log4j_template.xml C:\Git\thebase\base_mdm_server\tomcat_server\log4j-base-mdm.xml
copy /Y C:\Git\thebase\base_mdm_server\server\target\context.xml C:\Git\thebase\base_mdm_server\tomcat_server\conf\context.xml
copy /Y C:\Git\thebase\base_mdm_server\server\target\launcher.war C:\Git\thebase\base_mdm_server\tomcat_server\webapps\ROOT.war
set CATALINA_HOME=C:\tools\tomcat9
set CATALINA_BASE=C:\Git\thebase\base_mdm_server\tomcat_server
%CATALINA_HOME%\bin\catalina.bat run

