$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$fixture = Join-Path $repo ('build/export-test-' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $fixture -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $repo 'Export-Share.ps1') -Destination $fixture
Set-Content -LiteralPath (Join-Path $fixture 'public.txt') -Value 'public simulator'
$script:checks = 0
function Check([bool]$ok, [string]$message) { if (!$ok) { throw $message }; $script:checks++ }
function Reject([string[]]$paths, [string]$reason) {
    Set-Content -LiteralPath (Join-Path $fixture '.share-files.txt') -Value $paths
    $output = Join-Path $fixture ([Guid]::NewGuid().ToString('N') + '.zip')
    $failed = $false
    try { & (Join-Path $fixture 'Export-Share.ps1') -Destination $output } catch { $failed = $true }
    Check $failed ('Expected rejection: ' + $reason)
    Check (!(Test-Path -LiteralPath $output)) ('Rejected export created output: ' + $reason)
}
foreach ($bad in @('../secret.java', '/secret.java', 'C:/secret.java', 'web/../../secret.java', 'web\secret.java', 'public.txt:secret', './public.txt', 'web//secret.java', 'web./secret.java')) {
    Reject @('public.txt', $bad) $bad
}
Reject @('public.txt', 'PUBLIC.TXT') 'case-insensitive duplicate'
$outside = Join-Path $fixture 'outside'
New-Item -ItemType Directory -Path $outside | Out-Null
Set-Content -LiteralPath (Join-Path $outside 'secret.java') -Value 'PRIVATE_MARKER'
try {
    New-Item -ItemType Junction -Path (Join-Path $fixture 'linked') -Target $outside | Out-Null
    Reject @('public.txt', 'linked/secret.java') 'directory junction'
} catch [System.ComponentModel.Win32Exception] {
    Write-Warning 'Junction test unavailable: this environment disallows link creation.'
}
$private = @('ImportedTeamCode/Secret.java', 'build/Secret.java', 'out/Secret.java', 'artifacts/Secret.java', 'profiles/Secret.json', 'import-history/Secret.java', '.git/config', 'import-manifest.json', 'source.zip', 'Secret.class', 'session.log')
foreach ($name in $private) {
    if ($name -eq '.git/config') { continue }
    $local = Join-Path $fixture $name
    New-Item -ItemType Directory -Path (Split-Path $local -Parent) -Force | Out-Null
    Set-Content -LiteralPath $local -Value 'PRIVATE_MARKER'
}
Set-Content -LiteralPath (Join-Path $fixture '.share-files.txt') -Value (@('public.txt', '.share-files.txt') + $private)
$clean = Join-Path $fixture 'clean.zip'
& (Join-Path $fixture 'Export-Share.ps1') -Destination $clean
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::OpenRead($clean)
try {
    $names = @($archive.Entries.FullName)
    Check ($names.Count -eq 3) 'Only public file, sanitized manifest and empty import placeholder should be exported'
    Check ($names -contains 'BIOBUZZ-sim-community/public.txt') 'Public content missing'
    Check ($archive.GetEntry('BIOBUZZ-sim-community/ImportedTeamCode/.gitkeep').Length -eq 0) 'Import placeholder must be empty'
    $reader = [IO.StreamReader]::new($archive.GetEntry('BIOBUZZ-sim-community/.share-files.txt').Open())
    try { $manifestText = $reader.ReadToEnd() } finally { $reader.Dispose() }
    Check (!$manifestText.Contains('Secret')) 'Manifest exposes excluded private filenames'
} finally { $archive.Dispose() }

# Git mode must refuse both unstaged edits and staged additions.
& git -C $fixture init --quiet
if ($LASTEXITCODE -ne 0) { throw 'Fixture Git init failed' }
& git -C $fixture add -- public.txt Export-Share.ps1 .share-files.txt
& git -C $fixture -c user.name=ExportTest -c user.email=export-test@example.invalid commit --quiet -m 'Public fixture'
if ($LASTEXITCODE -ne 0) { throw 'Fixture Git commit failed' }
$gitClean = Join-Path $fixture 'git-clean.zip'
& (Join-Path $fixture 'Export-Share.ps1') -Destination $gitClean
Check (Test-Path -LiteralPath $gitClean) 'Clean Git export failed'
Set-Content -LiteralPath (Join-Path $fixture 'public.txt') -Value 'PRIVATE_MARKER'
function RejectGit([string]$reason) {
    $output = Join-Path $fixture ([Guid]::NewGuid().ToString('N') + '.zip')
    $failed = $false
    try { & (Join-Path $fixture 'Export-Share.ps1') -Destination $output } catch { $failed = $_.Exception.Message -like '*clean committed working tree*' }
    Check $failed ('Expected Git privacy rejection: ' + $reason)
    Check (!(Test-Path -LiteralPath $output)) ('Rejected Git export created output: ' + $reason)
}
RejectGit 'dirty tracked file'
& git -C $fixture add -- public.txt
RejectGit 'staged tracked file'
Write-Host ('PASS: ' + $script:checks + ' privacy/export assertions')
