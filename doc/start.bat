@echo off
setlocal enabledelayedexpansion




set YEAR=%date:~0,4%
set MOUTH=%date:~5,2%
set DAY=%date:~8,2%
set HOUR=%time:~0,2%
if /i %HOUR% LSS 10 (set HOUR=0%time:~1,1%)
set MINUTE=%time:~3,2%
set SECOUND=%time:~6,2%


echo start ...
set logname=%YEAR%_%MOUTH%_%DAY%_%HOUR%_%MINUTE%_%SECOUND%.log
rem md logs > nul 2>&1

rem start "" /i /b "javaw"  -Dfile.encoding=utf-8 -jar ./EasyCode.jar >./logs/EasyCode%logname% 2>&1
start "" /i /b "javaw"  -Dfile.encoding=utf-8 -jar ./EasyCode.jar



goto :exit


































:exit
echo.
exit /b 0







:getpid
for /F "tokens=1,2,3,4,5"  %%i in ('netstat -ano') do ( 
	::echo _%%i_  _%%j_  _%%k_  _%%l_  _%%m_
	if "%%j" == "0.0.0.0:%2" (
		if "%%l" == "LISTENING" (
			set %1=%%m
			exit /b 0
		)
	)
)
exit /b 0

:getPwd
set pwd_t=1
for /F "tokens=1 delims= " %%i in ('dir') do ( 
	set pwd_t=%%i

	if "!pwd_t:~1,2!" == ":\" (
		set %1=!pwd_t!
		exit /b 0
	)
)
exit /b 0