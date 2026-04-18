@echo off
chcp 65001

set MPJ=C:\mpj\bin\mpjrun.bat
set CP=F:\Programmes\Github\Reps\Parallel26\out\production\Lab_7
set SIM_CLASS=multiplicator.Simple
set COL_CLASS=multiplicator.Collective

for %%N in (4 8) do (
    for %%S in (800 1600 2400) do (
        cmd /c "%MPJ% -np %%N -cp "%CP%" %SIM_CLASS% -- %%S"
        cmd /c "%MPJ% -np %%N -cp "%CP%" %COL_CLASS% -- %%S"
    )
)

pause