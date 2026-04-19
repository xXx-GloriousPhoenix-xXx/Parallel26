@echo off
chcp 65001

set MPJ=C:\mpj\bin\mpjrun.bat
set CP=F:\Programmes\Github\Reps\Parallel26\out\production\Lab_7
set SIM_CLASS=multiplicator.Simple
set COL_CLASS=multiplicator.Collective
set NP=4
set SIZE=800

cmd /c "%MPJ% -np "%NP%" -cp "%CP%" %SIM_CLASS% -- "%SIZE%""
cmd /c "%MPJ% -np "%NP%" -cp "%CP%" %COL_CLASS% -- "%SIZE%""

pause