param([string]$Destination)
$ErrorActionPreference = 'Stop'
$root = [IO.Path]::GetFullPath($PSScriptRoot)
if (!$Destination) { $Destination = Join-Path $root 'BIOBUZZ-sim-community.zip' }
$Destination = [IO.Path]::GetFullPath($Destination)
if (Test-Path -LiteralPath $Destination) { throw 'Choose a new ZIP filename; existing files are never overwritten.' }
if (Test-Path -LiteralPath (Join-Path $root '.git')) {
    $dirty = @(git -C $root status --porcelain --untracked-files=no)
    if ($LASTEXITCODE -ne 0) { throw 'Could not verify the Git working tree.' }
    if ($dirty.Count) { throw 'Sharing requires a clean committed working tree. Review and commit simulator changes first; never commit local robot code or calibration.' }
    $files = @(git -C $root -c core.quotepath=false ls-files)
    if ($LASTEXITCODE -ne 0) { throw 'Could not read the approved Git file list.' }
} else {
    $manifestFile = Get-Item -LiteralPath (Join-Path $root '.share-files.txt') -Force
    if (($manifestFile.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) { throw 'The distribution manifest cannot be a link.' }
    $files = @(Get-Content -LiteralPath (Join-Path $root '.share-files.txt'))
}
if (!$files.Count) { throw 'Approved distribution file list is empty.' }

# Validate every path before creating output; exclude local data even if tracked.
$approved = [Collections.Generic.List[string]]::new()
$seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($file in $files) {
    if ([string]::IsNullOrWhiteSpace($file) -or $file.Contains('\') -or $file.Contains(':') -or
        [IO.Path]::IsPathRooted($file) -or $file -match '(^|/)(\.{1,2}|)(/|$)' -or
        $file -match '[\x00-\x1f]' -or $file -match '[. ](/|$)') { throw "Invalid distribution path: $file" }
    if ($file -match '(^|/)(ImportedTeamCode|build|out|artifacts|import-history|profiles|\.git)(/|$)' -or
        $file -match '(^|/)import-manifest\.json$' -or $file -match '\.(class|zip|log)$') { continue }
    if (!$seen.Add($file)) { throw "Duplicate distribution path: $file" }
    $source = [IO.Path]::GetFullPath((Join-Path $root $file))
    if (!$source.StartsWith($root + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw "Distribution path escapes simulator: $file" }
    $part = $root
    foreach ($segment in $file.Split('/')) {
        $part = Join-Path $part $segment
        $item = Get-Item -LiteralPath $part -Force
        if (($item.Attributes -band [IO.FileAttributes]::ReparsePoint) -ne 0) { throw "Linked distribution paths are not allowed: $file" }
    }
    if (!(Test-Path -LiteralPath $source -PathType Leaf)) { throw "Distribution file is missing: $file" }
    if ($source.Equals($Destination, [StringComparison]::OrdinalIgnoreCase)) { throw 'Output cannot replace a distribution file.' }
    $approved.Add($file)
}
if (!$approved.Count) { throw 'No public distribution files were approved.' }
Add-Type -AssemblyName System.IO.Compression, System.IO.Compression.FileSystem
$zip = [IO.Compression.ZipFile]::Open($Destination, [IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in $approved) {
        if ($file -eq '.share-files.txt') { continue }
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, (Join-Path $root $file), ('BIOBUZZ-sim-community/' + $file), [IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
    # Excluded filenames must not leak through the exported manifest either.
    $manifest = $zip.CreateEntry('BIOBUZZ-sim-community/.share-files.txt')
    $writer = [IO.StreamWriter]::new($manifest.Open(), [Text.UTF8Encoding]::new($false))
    try { @(@($approved) + @('.share-files.txt', 'ImportedTeamCode/.gitkeep')) | Sort-Object -Unique | ForEach-Object { $writer.WriteLine($_) } } finally { $writer.Dispose() }
    $zip.CreateEntry('BIOBUZZ-sim-community/ImportedTeamCode/.gitkeep') | Out-Null
} finally { $zip.Dispose() }
Write-Host ('Clean sharing ZIP created: ' + $Destination)
