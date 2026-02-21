@echo off
cd /d "F:\Programmes\Github\Reps\Parallel26\out\production\Lab_6"
"C:\mpj\bin\mpjrun.bat" -np 2 multiplicator.BlockingMPI
pause