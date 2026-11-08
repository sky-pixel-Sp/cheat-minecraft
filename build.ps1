$ErrorActionPreference = 'Stop'
$GradleVersion = '9.7.1'
$ToolRoot = Join-Path $env:LOCALAPPDATA 'SkyOP-Builder'
$GradleHome = Join-Path $ToolRoot "gradle-$GradleVersion"
$GradleExe = Join-Path $GradleHome 'bin\gradle.bat'

Write-Host 'Checking Java runtime...' -ForegroundColor Cyan
$javaCommand = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCommand) {
    throw 'Java was not found in PATH. Install a JDK 25 (64-bit), then reopen this window.'
}
$versionLine = (& java -version 2>&1 | Select-Object -First 1).ToString()
if ($versionLine -notmatch '"?([0-9]+)') {
    throw "Could not read Java version: $versionLine"
}
$javaMajor = [int]$Matches[1]
if ($javaMajor -lt 25) {
    throw "Minecraft 26.3 mod development requires Java 25+. Detected Java $javaMajor. Install JDK 25 and set JAVA_HOME/PATH first."
}
Write-Host "Java $javaMajor detected." -ForegroundColor Green

if (-not (Test-Path $GradleExe)) {
    New-Item -ItemType Directory -Force -Path $ToolRoot | Out-Null
    $zipPath = Join-Path $ToolRoot "gradle-$GradleVersion-bin.zip"
    $url = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
    Write-Host "Downloading Gradle $GradleVersion..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $url -OutFile $zipPath
    Write-Host 'Extracting Gradle...' -ForegroundColor Cyan
    Expand-Archive -LiteralPath $zipPath -DestinationPath $ToolRoot -Force
    Remove-Item $zipPath -Force
}

Write-Host 'Building SkyOP Client for Minecraft 26.3...' -ForegroundColor Cyan
Push-Location $PSScriptRoot
try {
    & $GradleExe --no-daemon clean build
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

Write-Host ''
Write-Host 'BUILD SUCCESSFUL' -ForegroundColor Green
Write-Host "Mod JAR: $PSScriptRoot\build\libs\skyop-client-1.0.0.jar" -ForegroundColor Green
