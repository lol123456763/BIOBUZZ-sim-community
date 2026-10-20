param([string]$Destination)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (!$Destination) { $Destination = Join-Path $PSScriptRoot 'BIOBUZZ-sim-community.zip' }
$Destination = [IO.Path]::GetFullPath($Destination)
if(Test-Path -LiteralPath $Destination){throw 'Choose a new ZIP filename; existing files are never overwritten.'}
if(Test-Path -LiteralPath (Join-Path $PSScriptRoot '.git')) {
    $files = @(git -C $PSScriptRoot ls-files)
    if($LASTEXITCODE -ne 0){throw 'Could not read the approved Git file list.'}
} else {
    $files = @(Get-Content -LiteralPath (Join-Path $PSScriptRoot '.share-files.txt'))
}
if(!$files.Count){throw 'Approved distribution file list is empty.'}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::Open($Destination,[IO.Compression.ZipArchiveMode]::Create)
try {
    foreach($file in $files) {
        if($file -eq 'ImportedTeamCode/.gitkeep'){continue}
        if($file -match '^(ImportedTeamCode|build|import-history|profiles|\.git)/' -or $file -match '(^|/)(import-manifest\.json)$' -or $file -match '\.(class|zip|log)$'){continue}
        $source = Join-Path $PSScriptRoot $file
        if(!(Test-Path -LiteralPath $source -PathType Leaf)){throw ('Tracked distribution file is missing: '+$file)}
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,$source,('BIOBUZZ-sim-community/'+$file),[IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
    $zip.CreateEntry('BIOBUZZ-sim-community/ImportedTeamCode/.gitkeep') | Out-Null
} finally { $zip.Dispose() }
Write-Host ('Clean sharing ZIP created: '+$Destination)
