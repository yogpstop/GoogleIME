@echo off
set GRP=com\yogpc\gi
set ARC=GoogleIME
set VRS=1.0.0
set MCV=1.8.1
set GI=target\%ARC%-%VRS%.jar
set MCDIR=%APPDATA%\.minecraft
set VERSIONS=%MCDIR%\versions
set VALS=%MCV%-%ARC%-%VRS%
set LDIR=%MCDIR%\libraries\%GRP%\%ARC%\%VRS%
cd %~dp0
mkdir %VERSIONS%\%MCV%-%ARC%-%VRS% %LDIR%
copy %VERSIONS%\%MCV%\%MCV%.jar %VERSIONS%\%VALS%\%VALS%.jar
copy %VALS%.json %VERSIONS%\%VALS%\%VALS%.json
copy %GI% %LDIR%\%ARC%-%VRS%.jar
pause
