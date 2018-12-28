@echo off
rem Argument counting code from http://www.testdeveloper.com/2010/09/26/how-to-count-arguments-to-a-dos-batch-file-without-using-your-fingers-and-toes
set _exitStatus=0
set _argcActual=0
set _argcExpected=2
for %%i in (%*) do set /A _argcActual+=1
if %_argcActual% NEQ %_argcExpected% ( 
  call :_ShowUsage %0%, "Need to include login name and password for UMLS Terminology Services."  
  set _exitStatus=1  
  goto:_EOF
)

FOR /f tokens^=2-5^ delims^=.-_^" %%j IN ('java -fullversion 2^>^&1') DO SET "jver=%%j%%k"
IF %jver% GTR 18 (
java -Dumls-user=%1 -Dumls-password=%2 --add-modules=java.xml.ws  -jar cpt4.jar 4
) ELSE (
java -Dumls-user=%1 -Dumls-password=%2 -jar cpt4.jar 4
)
set _exitStatus=%ERRORLEVEL%
goto:_EOF
:_ShowUsage  
echo [USAGE]: %~1 login password 
if NOT "%~2" == "" (   
  echo %~2      
)  
goto:eof
:_EOF
echo The exit status is %_exitStatus%
cmd /c exit %_exitStatus%
