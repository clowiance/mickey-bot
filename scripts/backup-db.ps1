param(
    [string]$DatabasePath = "mickey.db",
    [string]$BackupDirectory = "backups"
)

$ErrorActionPreference = "Stop"
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot ".."))
$database = if ([System.IO.Path]::IsPathRooted($DatabasePath)) {
    [System.IO.Path]::GetFullPath($DatabasePath)
} else {
    [System.IO.Path]::GetFullPath((Join-Path $projectRoot $DatabasePath))
}
$backupRoot = if ([System.IO.Path]::IsPathRooted($BackupDirectory)) {
    [System.IO.Path]::GetFullPath($BackupDirectory)
} else {
    [System.IO.Path]::GetFullPath((Join-Path $projectRoot $BackupDirectory))
}

if (-not (Test-Path -LiteralPath $database -PathType Leaf)) {
    throw "SQLite database not found: $database"
}
if (-not (Get-Command sqlite3 -ErrorAction SilentlyContinue)) {
    throw "sqlite3 is required for an online-consistent backup"
}

New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$finalPath = Join-Path $backupRoot "mickey-$timestamp.db"
$partialPath = "$finalPath.partial"
$sqlitePath = $partialPath.Replace("'", "''")

& sqlite3 $database ".backup '$sqlitePath'"
if ($LASTEXITCODE -ne 0) {
    throw "sqlite3 backup failed with exit code $LASTEXITCODE"
}

$integrity = (& sqlite3 $partialPath "PRAGMA integrity_check;").Trim()
if ($LASTEXITCODE -ne 0 -or $integrity -ne "ok") {
    throw "Backup integrity check failed: $integrity"
}

Move-Item -LiteralPath $partialPath -Destination $finalPath
$hash = Get-FileHash -Algorithm SHA256 -LiteralPath $finalPath
Write-Output ([pscustomobject]@{
    Backup = $finalPath
    SizeBytes = (Get-Item -LiteralPath $finalPath).Length
    SHA256 = $hash.Hash
})
