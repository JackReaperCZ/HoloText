Param(
  [string]$ServerDir = "C:\McServers\Paper-1.21.4",
  [string]$Version = "1.0.1",
  [string]$ServerJarName = "paper-1.21.4-232.jar",
  [switch]$RunServer
)

$ErrorActionPreference = 'Stop'

function Write-Info($msg) { Write-Host "[HoloText] $msg" -ForegroundColor Cyan }
function Write-Err($msg) { Write-Host "[HoloText] $msg" -ForegroundColor Red }

$ProjectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectDir

# Paths
$SrcDir = Join-Path $ProjectDir 'src\main\java'
$ResDir = Join-Path $ProjectDir 'src\main\resources'
$TargetDir = Join-Path $ProjectDir 'target'
$ClassesDir = Join-Path $TargetDir 'classes'
$OutJar = Join-Path $TargetDir ("HoloText-" + $Version + ".jar")
$PluginJar = Join-Path $ServerDir ("plugins\HoloText-" + $Version + ".jar")
$ServerJar = Join-Path $ServerDir $ServerJarName
$ApiJar = Join-Path $ServerDir 'libraries\io\papermc\paper\paper-api\1.21.4-R0.1-SNAPSHOT\paper-api-1.21.4-R0.1-SNAPSHOT.jar'
$AnnotationsJar = Join-Path $ServerDir 'libraries\org\jetbrains\annotations\24.0.1\annotations-24.0.1.jar'
$KyoriApiJar = Join-Path $ServerDir 'libraries\net\kyori\adventure-api\4.20.0\adventure-api-4.20.0.jar'
$KyoriKeyJar = Join-Path $ServerDir 'libraries\net\kyori\adventure-key\4.20.0\adventure-key-4.20.0.jar'
$KyoriLegacyJar = Join-Path $ServerDir 'libraries\net\kyori\adventure-text-serializer-legacy\4.20.0\adventure-text-serializer-legacy-4.20.0.jar'
$KyoriGsonJar = Join-Path $ServerDir 'libraries\net\kyori\adventure-text-serializer-gson\4.20.0\adventure-text-serializer-gson-4.20.0.jar'
$KyoriJsonJar = Join-Path $ServerDir 'libraries\net\kyori\adventure-text-serializer-json\4.20.0\adventure-text-serializer-json-4.20.0.jar'
$BungeeChatJar = Join-Path $ServerDir 'libraries\net\md-5\bungeecord-chat\1.20-R0.2-deprecated+build.19\bungeecord-chat-1.20-R0.2-deprecated+build.19.jar'

Write-Info "Project: $ProjectDir"
Write-Info "Server:  $ServerDir"

if (!(Test-Path $ServerJar)) { Write-Err "Server jar not found: $ServerJar"; exit 1 }
if (!(Test-Path $ApiJar)) { Write-Err "Paper API jar not found: $ApiJar"; exit 1 }
if (!(Test-Path $AnnotationsJar)) { Write-Err "JetBrains annotations jar not found: $AnnotationsJar"; exit 1 }
if (!(Test-Path $KyoriApiJar)) { Write-Err "Kyori Adventure API jar not found: $KyoriApiJar"; exit 1 }
if (!(Test-Path $KyoriKeyJar)) { Write-Err "Kyori Adventure Key jar not found: $KyoriKeyJar"; exit 1 }
if (!(Test-Path $KyoriLegacyJar)) { Write-Err "Kyori Adventure Legacy serializer jar not found: $KyoriLegacyJar"; exit 1 }
if (!(Test-Path $KyoriGsonJar)) { Write-Err "Kyori Adventure Gson serializer jar not found: $KyoriGsonJar"; exit 1 }
if (!(Test-Path $KyoriJsonJar)) { Write-Err "Kyori Adventure Json serializer jar not found: $KyoriJsonJar"; exit 1 }
if (!(Test-Path $BungeeChatJar)) { Write-Err "BungeeCord chat jar not found: $BungeeChatJar"; exit 1 }

# Clean lib folder if present (remove traces of external jars)
$LibDir = Join-Path $ProjectDir 'lib'
if (Test-Path $LibDir) {
  Write-Info "Removing lib directory: $LibDir"
  Remove-Item -Recurse -Force $LibDir
}

# Prepare target directories
New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
New-Item -ItemType Directory -Force -Path $ClassesDir | Out-Null

# Gather sources
$sources = Get-ChildItem -Recurse -File $SrcDir -Filter *.java | ForEach-Object { $_.FullName }
if ($sources.Count -eq 0) { Write-Err "No Java source files found in $SrcDir"; exit 1 }
Set-Content -Path (Join-Path $TargetDir 'sources.txt') -Value ($sources -join [Environment]::NewLine)

# Compile against Paper API + annotations (no external libraries)
$cp = "$ApiJar;$AnnotationsJar;$KyoriApiJar;$KyoriKeyJar;$KyoriLegacyJar;$KyoriGsonJar;$KyoriJsonJar;$BungeeChatJar"
Write-Info "Compiling with classpath: $cp"
# PowerShell treats @"..." as here-string; use cmd to pass @argfile cleanly
$argsFile = Join-Path $TargetDir 'sources.txt'
cmd /c "javac --release 21 -d `"$ClassesDir`" -cp `"$cp`" @`"$argsFile`""

# Add plugin.yml
Copy-Item -Force (Join-Path $ResDir 'plugin.yml') (Join-Path $ClassesDir 'plugin.yml')
# Add config.yml if present
if (Test-Path (Join-Path $ResDir 'config.yml')) {
  Copy-Item -Force (Join-Path $ResDir 'config.yml') (Join-Path $ClassesDir 'config.yml')
}

# Package: use zip then rename to .jar (jar tool may not be on PATH)
$zipPath = Join-Path $TargetDir ("HoloText-" + $Version + ".zip")
if (Test-Path $zipPath) { Remove-Item -Force $zipPath }
Write-Info "Packaging to $OutJar"
Compress-Archive -Force -Path (Get-ChildItem -Recurse -File $ClassesDir | ForEach-Object { $_.FullName }) -DestinationPath $zipPath
if (Test-Path $OutJar) { Remove-Item -Force $OutJar }
Move-Item -Force $zipPath $OutJar

# Deploy to server plugins folder
Write-Info "Deploying to $PluginJar"
Copy-Item -Force $OutJar $PluginJar

if ($RunServer) {
  Write-Info "Starting server..."
  # Start server non-blocking; let Paper manage remapping and load plugins
  $cmd = "java -jar `"$ServerJar`""
  Start-Process -WorkingDirectory $ServerDir -FilePath "powershell.exe" -ArgumentList "-NoProfile","-Command",$cmd
  Write-Info "Server start command issued. Check console window for logs."
}

Write-Info "Done."