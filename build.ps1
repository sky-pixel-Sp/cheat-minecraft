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
    Expand-Archive -LiteralPath $zipPath -DestinationPath $ToolRoot -Force
    Remove-Item $zipPath -Force
}

Write-Host 'Building SkyOP Client 4.0.1 for Minecraft 26.3...' -ForegroundColor Cyan
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
$modJar = Get-ChildItem -Path (Join-Path $PSScriptRoot 'build\libs') -Filter 'skyop-client-*.jar' |
    Where-Object { $_.Name -notlike '*-sources.jar' } |
    Sort-Object Name |
    Select-Object -First 1
if (-not $modJar) {
    throw 'Gradle completed but the SkyOP mod JAR was not found in build\libs.'
}
Write-Host "Mod JAR: $($modJar.FullName)" -ForegroundColor Green
