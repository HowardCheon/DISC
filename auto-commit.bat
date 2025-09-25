@echo off
echo Auto-committing changes...

:: Add all changes
git add .

:: Check if there are changes to commit
git diff --staged --quiet
if %errorlevel% == 0 (
    echo No changes to commit.
    exit /b 0
)

:: Create commit with timestamp
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"

git commit -m "chore: Auto-commit changes at %YYYY%-%MM%-%DD% %HH%:%Min%:%Sec%

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>"

:: Push to GitHub
git push

echo Auto-commit completed!