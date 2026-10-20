param([switch]$Test, [string]$ImportedSource, [switch]$NoActivate, [switch]$PassThru)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$javaRoot = Join-Path $PSScriptRoot 'runtime'
if (!(Test-Path -LiteralPath (Join-Path $javaRoot 'bin/javac.exe'))) {
    if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin/javac.exe'))) { $javaRoot = $env:JAVA_HOME }
    else { throw 'JDK missing. Keep the bundled runtime folder, or set JAVA_HOME to JDK 21+.' }
}
$targetDir = Join-Path $PSScriptRoot ('build/classes-' + [DateTime]::UtcNow.ToString('yyyyMMddHHmmssfff'))
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$libraries = ((Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'lib') -Filter '*.jar').FullName -join ';')
$sourceDirs = @((Join-Path $PSScriptRoot 'Controller/src'), (Join-Path $PSScriptRoot 'TeamCode/src'))
if (!$ImportedSource) { $ImportedSource = Join-Path $PSScriptRoot 'ImportedTeamCode' }
$sourceDirs += $ImportedSource
$sourceFiles = foreach ($dir in $sourceDirs) { if(Test-Path -LiteralPath $dir){Get-ChildItem -LiteralPath $dir -Recurse -Filter '*.java'} }
$compileArgs = @('--release','21','--module-path',(Join-Path $PSScriptRoot 'vendor/javafx'),'--add-modules','javafx.controls,javafx.fxml,javafx.swing,jdk.httpserver','-encoding','UTF-8','-cp',$libraries,'-d',$targetDir) + @($sourceFiles.FullName)
$quoted = $compileArgs | ForEach-Object { '"' + $_.Replace('\','/') + '"' }
$argumentFile = Join-Path $PSScriptRoot 'build/compile.args'
[IO.File]::WriteAllLines($argumentFile, [string[]]$quoted, (New-Object Text.UTF8Encoding($false)))
& (Join-Path $javaRoot 'bin/javac.exe') ('@' + $argumentFile)
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. Existing runnable build is preserved; see the Java errors above.' }
if ($Test) {
    & (Join-Path $javaRoot 'bin/java.exe') '-ea' '-cp' $targetDir 'virtual_robot.biobuzz.PhysicsTests'
    if ($LASTEXITCODE -ne 0) { throw 'Physics tests failed.' }
    & (Join-Path $javaRoot 'bin/java.exe') '-ea' '-cp' $targetDir 'virtual_robot.biobuzz.VisionTests'
    if ($LASTEXITCODE -ne 0) { throw 'Camera tests failed.' }
    & (Join-Path $javaRoot 'bin/java.exe') '--module-path' (Join-Path $PSScriptRoot 'vendor/javafx') '--add-modules' 'javafx.graphics' '-ea' '-cp' $targetDir 'virtual_robot.biobuzz.RealismTests'
    if ($LASTEXITCODE -ne 0) { throw 'V2 behavior tests failed.' }
    & (Join-Path $javaRoot 'bin/java.exe') '-ea' '-cp' ($targetDir + ';' + $libraries) 'virtual_robot.biobuzz.StudioTests'
    if ($LASTEXITCODE -ne 0) { throw 'Studio behavior tests failed.' }
    & (Join-Path $javaRoot 'bin/java.exe') '--module-path' (Join-Path $PSScriptRoot 'vendor/javafx') '--add-modules' 'javafx.controls,javafx.fxml,javafx.swing,jdk.httpserver' '-ea' '-cp' ($targetDir + ';' + $libraries) 'virtual_robot.biobuzz.TeamOpModeTests'
    if ($LASTEXITCODE -ne 0) { throw 'Team OpMode discovery tests failed.' }
}
if (!$NoActivate) {
    Set-Content -LiteralPath (Join-Path $PSScriptRoot 'build/active-build.txt') -Value $targetDir
    Set-Content -LiteralPath (Join-Path $PSScriptRoot 'build/last-build.txt') -Value ([DateTime]::UtcNow.ToString('O'))
}
Write-Host 'Build complete. Close and reopen Start Simulator.cmd to use it.' -ForegroundColor Green
if ($PassThru) { Write-Output $targetDir }
