param([Parameter(Mandatory=$true)][string]$ManifestPath)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot
$artRoot = Join-Path $root 'docs/art/retexture-v2'
$textureRoot = Join-Path $root 'src/main/resources/assets/rabbittools/textures/item'
New-Item -ItemType Directory -Path $artRoot -Force | Out-Null
$backupRoot = Join-Path $artRoot 'previous-textures'
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
$manifest = Get-Content -LiteralPath $ManifestPath -Raw | ConvertFrom-Json -AsHashtable
$template = [Drawing.Bitmap]::new((Join-Path $textureRoot 'core_angel.png'))
$originalPalette = @{}
for ($y=0; $y -lt 16; $y++) {
    for ($x=0; $x -lt 16; $x++) {
        $color = $template.GetPixel($x,$y)
        if ($color.A -gt 0) { $originalPalette[$color.ToArgb()] = $color }
    }
}
$sourceColors = @($originalPalette.Values | Sort-Object { $_.GetBrightness() })
$names = @('building_component','precision_building_component','blank_core',
    'iron_wand_head','gold_wand_head','diamond_wand_head','netherite_wand_head','infinity_wand_head')
$sheet = [Drawing.Bitmap]::new(640,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$index = 0
foreach ($name in $names) {
    $sourcePath = $manifest[$name]
    if (-not $sourcePath -or -not (Test-Path -LiteralPath $sourcePath)) { throw "Missing image: $name" }
    $destination = Join-Path $textureRoot ($name+'.png')
    $backup = Join-Path $backupRoot ($name+'.png')
    if (-not (Test-Path -LiteralPath $backup)) { Copy-Item -LiteralPath $destination -Destination $backup }
    Copy-Item -LiteralPath $sourcePath -Destination (Join-Path $artRoot ($name+'-source.png'))
    $generated = [Drawing.Bitmap]::new($sourcePath)
    $output = [Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
    if ($name.EndsWith('_wand_head')) {
        # Use imagegen's material palette with the exact original angel-core pixel template.
        # Sorting the two palettes by lightness retains the source facets and highlights.
        $targetPalette = @{}
        for ($y=0; $y -lt 32; $y++) {
            for ($x=0; $x -lt 32; $x++) {
                $sx = [int][Math]::Floor(($x+0.5)*$generated.Width/32)
                $sy = [int][Math]::Floor(($y+0.5)*$generated.Height/32)
                $color = $generated.GetPixel($sx,$sy)
                if ($color.A -gt 200) { $targetPalette[$color.ToArgb()] = $color }
            }
        }
        $targetColors = @($targetPalette.Values | Sort-Object { $_.GetBrightness() })
        if ($targetColors.Count -lt 3) { throw "Insufficient generated colors: $name" }
        $mapping = @{}
        for ($i=0; $i -lt $sourceColors.Count; $i++) {
            $targetIndex = [int][Math]::Round($i*($targetColors.Count-1)/($sourceColors.Count-1))
            $mapping[$sourceColors[$i].ToArgb()] = $targetColors[$targetIndex]
        }
        for ($y=0; $y -lt 16; $y++) {
            for ($x=0; $x -lt 16; $x++) {
                $original = $template.GetPixel($x,$y)
                if ($original.A -gt 0) {
                    $replacement = $mapping[$original.ToArgb()]
                    $output.SetPixel($x,$y,[Drawing.Color]::FromArgb($original.A,$replacement.R,$replacement.G,$replacement.B))
                }
            }
        }
    } else {
        for ($y=0; $y -lt 16; $y++) {
            for ($x=0; $x -lt 16; $x++) {
                $sx = [int][Math]::Floor(($x+0.25)*$generated.Width/16)
                $sy = [int][Math]::Floor(($y+0.25)*$generated.Height/16)
                $output.SetPixel($x,$y,$generated.GetPixel($sx,$sy))
            }
        }
    }
    $output.Save($destination,[Drawing.Imaging.ImageFormat]::Png)
    if ($index -lt 3) { $column = $index; $row = 0 } else { $column = $index-3; $row = 1 }
    for ($y=0; $y -lt 128; $y++) {
        for ($x=0; $x -lt 128; $x++) {
            $sheet.SetPixel($column*128+$x,$row*128+$y,
                $output.GetPixel([int][Math]::Floor($x/8),[int][Math]::Floor($y/8)))
        }
    }
    $output.Dispose()
    $generated.Dispose()
    $index++
}
$template.Dispose()
$sheet.Save((Join-Path $artRoot 'textures-preview.png'),[Drawing.Imaging.ImageFormat]::Png)
$sheet.Dispose()
Write-Output 'Exported 8 textures; five wand heads use the exact original angel-core pixel template.'
