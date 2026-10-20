param([string]$Source)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (!$Source) {
    Add-Type -AssemblyName System.Windows.Forms
    $picker = New-Object System.Windows.Forms.FolderBrowserDialog
    $picker.Description = 'Select an extracted OnBot Java download, TeamCode, or Java source folder. It will only be read.'
    if ($picker.ShowDialog() -ne [System.Windows.Forms.DialogResult]::OK) { exit }
    $Source = $picker.SelectedPath
}
$original = (Resolve-Path -LiteralPath $Source).Path
$destination = Join-Path $PSScriptRoot 'ImportedTeamCode'
if ($original.Equals($destination,[StringComparison]::OrdinalIgnoreCase) -or $original.StartsWith($destination+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Choose your separate source folder, Java file, or source ZIP.' }
$stage = Join-Path $PSScriptRoot ('build/import-offline-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $stage -Force | Out-Null
$script:sourceCount = 0
function Add-JavaSource([string]$name,[string]$content) {
    $name = [IO.Path]::GetFileName($name.Replace('/',[IO.Path]::DirectorySeparatorChar))
    if ($name -notmatch '^[A-Za-z_$][\w$]*\.java$') { return }
    $content = $content.TrimStart([char]0xFEFF)
    $package = [regex]::Match($content,'(?m)^\s*package\s+(org\.firstinspires\.ftc\.teamcode(?:\.[A-Za-z_$][\w$]*)*)\s*;')
    if (!$package.Success) { return }
    $relative = Join-Path $package.Groups[1].Value.Replace('.','/') $name
    if (Test-Path -LiteralPath (Join-Path $PSScriptRoot ('Controller/src/' + $relative))) { return }
    $target = Join-Path $stage $relative
    if (Test-Path -LiteralPath $target) { throw ('Duplicate Java source: ' + $relative) }
    New-Item -ItemType Directory -Path (Split-Path $target -Parent) -Force | Out-Null
    [IO.File]::WriteAllText($target,$content,(New-Object Text.UTF8Encoding($false)))
    $script:sourceCount++
}
if (Test-Path -LiteralPath $original -PathType Container) {
    $androidTeamCode = Join-Path $original 'TeamCode/src/main/java'
    $searchRoot = if(Test-Path -LiteralPath $androidTeamCode){$androidTeamCode}else{$original}
    foreach($file in Get-ChildItem -LiteralPath $searchRoot -File -Recurse -Filter '*.java') { Add-JavaSource $file.Name ([IO.File]::ReadAllText($file.FullName)) }
} elseif ([IO.Path]::GetExtension($original) -eq '.zip') {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [IO.Compression.ZipFile]::OpenRead($original)
    try {
        foreach($entry in $archive.Entries) {
            if($entry.FullName -notmatch '\.java$'){continue}
            if($entry.Length -gt 2MB){throw 'Java source exceeds 2 MB.'}
            $reader = New-Object IO.StreamReader($entry.Open())
            try { Add-JavaSource $entry.Name $reader.ReadToEnd() } finally { $reader.Dispose() }
        }
    } finally { $archive.Dispose() }
} elseif ([IO.Path]::GetExtension($original) -eq '.java') {
    Add-JavaSource ([IO.Path]::GetFileName($original)) ([IO.File]::ReadAllText($original))
} else { throw 'Choose Java source, a source folder, or a source ZIP. APK, CLASS and JAR files are not source imports.' }
if ($script:sourceCount -eq 0) { throw 'No Java source in package org.firstinspires.ftc.teamcode or its subpackages. Export source from OnBot Java, not a compiled app.' }
# Compile staging first: a failed import never replaces the working source or build.
$built = & (Join-Path $PSScriptRoot 'Rebuild.ps1') -ImportedSource $stage -NoActivate -PassThru
$built = @($built)[-1]
if (Test-Path -LiteralPath $destination) {
    $backup = Join-Path $PSScriptRoot ('import-history/' + [Guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $backup -Force | Out-Null
    $resolved = (Resolve-Path -LiteralPath $destination).Path
    if(!$resolved.StartsWith($PSScriptRoot+[IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)){throw 'Import target outside simulator.'}
    Move-Item -LiteralPath $resolved -Destination (Join-Path $backup 'ImportedTeamCode')
}
Move-Item -LiteralPath $stage -Destination $destination
Set-Content -LiteralPath (Join-Path $PSScriptRoot 'build/active-build.txt') -Value $built
Set-Content -LiteralPath (Join-Path $PSScriptRoot 'build/last-build.txt') -Value ([DateTime]::UtcNow.ToString('O'))
Write-Host ('Imported ' + $script:sourceCount + ' Java files locally. Original source unchanged. Restart the simulator to load them.')
