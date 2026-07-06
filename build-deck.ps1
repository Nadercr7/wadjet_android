# Build script: reads logo base64 and injects into HTML template
$logoData = Get-Content "$PSScriptRoot\logo-base64.txt" -Raw
$template = Get-Content "$PSScriptRoot\wadjet-android-deck-template.html" -Raw
$final = $template -replace '%%LOGO_BASE64%%', $logoData
[System.IO.File]::WriteAllText("$PSScriptRoot\wadjet-android-deck.html", $final, [System.Text.Encoding]::UTF8)
Write-Host "Built wadjet-android-deck.html ($(([System.IO.FileInfo]"$PSScriptRoot\wadjet-android-deck.html").Length / 1KB) KB)"
