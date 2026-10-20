$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$simArguments = @($args)
$classes = Join-Path $PSScriptRoot 'build/classes'
$active = Join-Path $PSScriptRoot 'build/active-build.txt'
if (Test-Path -LiteralPath $active) {
    $candidate = (Get-Content -LiteralPath $active -Raw).Trim()
    if (Test-Path -LiteralPath $candidate) { $classes = $candidate }
}
$needsBuild = !(Test-Path -LiteralPath (Join-Path $classes 'virtual_robot/biobuzz/StudioServer.class'))
$stamp = Join-Path $PSScriptRoot 'build/last-build.txt'
if (!(Test-Path -LiteralPath $stamp)) { $needsBuild = $true }
else {
    $built = [DateTime]::Parse((Get-Content -LiteralPath $stamp -Raw)).ToUniversalTime()
    foreach ($source in @('Controller/src','TeamCode/src','ImportedTeamCode')) {
        if (Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot $source) -Filter '*.java' -Recurse | Where-Object { $_.LastWriteTimeUtc -gt $built } | Select-Object -First 1) { $needsBuild = $true }
    }
}
if ($needsBuild) {
    Write-Host 'Building your simulator and Java OpModes...'
    & (Join-Path $PSScriptRoot 'Rebuild.ps1')
    $classes = (Get-Content -LiteralPath $active -Raw).Trim()
}
if ($simArguments.Count -eq 0) { $simArguments = @('--studio') }
& (Join-Path $PSScriptRoot 'runtime/bin/java.exe') "-Djavafx.cachedir=$PSScriptRoot/build/fx-cache" '--module-path' (Join-Path $PSScriptRoot 'vendor/javafx') '--add-modules' 'javafx.controls,javafx.fxml,javafx.swing,jdk.httpserver' '-cp' "$classes;$PSScriptRoot/Controller/src;$PSScriptRoot/TeamCode/src;$PSScriptRoot/lib/*" 'virtual_robot.controller.VirtualRobotApplication' @simArguments
exit $LASTEXITCODE
