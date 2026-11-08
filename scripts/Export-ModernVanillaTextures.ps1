param([Parameter(Mandatory=$true)][string]$ManifestPath)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$root=Split-Path $PSScriptRoot
$art=Join-Path $root 'docs/art/modern-vanilla-v4'
$assets=Get-Content -LiteralPath $ManifestPath -Raw | ConvertFrom-Json -AsHashtable
$sheet=[Drawing.Bitmap]::new(640,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$native=[Drawing.Bitmap]::new(80,32,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$before=[Drawing.Bitmap]::new(640,256,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
$index=0
foreach($asset in $assets) {
    $name=$asset.Name
    $original=[Drawing.Bitmap]::new((Join-Path $art "previous-textures/$name.png"))
    $source=[Drawing.Bitmap]::new($asset.Source)
    $archive=Join-Path $art ($name+'-source.png')
    if((Resolve-Path -LiteralPath $asset.Source).Path -ne [IO.Path]::GetFullPath($archive)) {
        Copy-Item -LiteralPath $asset.Source -Destination $archive
    }
    $minX=$source.Width;$minY=$source.Height;$maxX=-1;$maxY=-1
    for($y=0;$y -lt $source.Height;$y+=2) {
        for($x=0;$x -lt $source.Width;$x+=2) {
            if($source.GetPixel($x,$y).A -gt 200) {
                $minX=[Math]::Min($minX,$x);$minY=[Math]::Min($minY,$y)
                $maxX=[Math]::Max($maxX,$x);$maxY=[Math]::Max($maxY,$y)
            }
        }
    }
    if($maxX -lt 0) {throw "Empty image: $name"}
    $ox0=16;$oy0=16;$ox1=-1;$oy1=-1
    for($y=0;$y -lt 16;$y++) {
        for($x=0;$x -lt 16;$x++) {
            if($original.GetPixel($x,$y).A -gt 0) {
                $ox0=[Math]::Min($ox0,$x);$oy0=[Math]::Min($oy0,$y)
                $ox1=[Math]::Max($ox1,$x);$oy1=[Math]::Max($oy1,$y)
            }
        }
    }
    $samples=[Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for($y=$oy0;$y -le $oy1;$y++) {
        for($x=$ox0;$x -le $ox1;$x++) {
            $sx=$minX+[int][Math]::Floor(($x-$ox0+0.5)*($maxX-$minX+1)/($ox1-$ox0+1))
            $sy=$minY+[int][Math]::Floor(($y-$oy0+0.5)*($maxY-$minY+1)/($oy1-$oy0+1))
            $samples.SetPixel($x,$y,$source.GetPixel($sx,$sy))
        }
    }
    $palette=@($asset.Palette | ForEach-Object {[Drawing.ColorTranslator]::FromHtml($_)})
    $output=[Drawing.Bitmap]::new(16,16,[Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for($y=0;$y -lt 16;$y++) {
        for($x=0;$x -lt 16;$x++) {
            $alpha=$original.GetPixel($x,$y).A
            if($alpha -eq 0) {continue}
            $color=$samples.GetPixel($x,$y)
            if($color.A -lt 128) {
                $nearest=999
                for($ny=$oy0;$ny -le $oy1;$ny++) {
                    for($nx=$ox0;$nx -le $ox1;$nx++) {
                        $candidate=$samples.GetPixel($nx,$ny)
                        $distance=($nx-$x)*($nx-$x)+($ny-$y)*($ny-$y)
                        if($candidate.A -ge 128 -and $distance -lt $nearest) {$color=$candidate;$nearest=$distance}
                    }
                }
            }
            $best=$null;$bestDistance=[double]::PositiveInfinity
            foreach($candidate in $palette) {
                $distance=($color.R-$candidate.R)*($color.R-$candidate.R)+($color.G-$candidate.G)*($color.G-$candidate.G)+($color.B-$candidate.B)*($color.B-$candidate.B)
                if($distance -lt $bestDistance) {$best=$candidate;$bestDistance=$distance}
            }
            $output.SetPixel($x,$y,[Drawing.Color]::FromArgb($alpha,$best.R,$best.G,$best.B))
        }
    }
    $output.Save((Join-Path $root "src/main/resources/assets/rabbittools/textures/item/$name.png"),[Drawing.Imaging.ImageFormat]::Png)
    if($index -lt 3) {$column=$index;$row=0} else {$column=$index-3;$row=1}
    for($y=0;$y -lt 16;$y++) {for($x=0;$x -lt 16;$x++) {$native.SetPixel($column*16+$x,$row*16+$y,$output.GetPixel($x,$y))}}
    for($y=0;$y -lt 128;$y++) {
        for($x=0;$x -lt 128;$x++) {
            $px=[int][Math]::Floor($x/8);$py=[int][Math]::Floor($y/8)
            $sheet.SetPixel($column*128+$x,$row*128+$y,$output.GetPixel($px,$py))
            $before.SetPixel($column*128+$x,$row*128+$y,$original.GetPixel($px,$py))
        }
    }
    $original.Dispose();$source.Dispose();$samples.Dispose();$output.Dispose();$index++
}
$sheet.Save((Join-Path $art 'textures-preview.png'),[Drawing.Imaging.ImageFormat]::Png)
$native.Save((Join-Path $art 'textures-native.png'),[Drawing.Imaging.ImageFormat]::Png)
$before.Save((Join-Path $art 'textures-before.png'),[Drawing.Imaging.ImageFormat]::Png)
$sheet.Dispose();$native.Dispose();$before.Dispose()
Write-Output 'Exported modern vanilla material palettes while preserving all original 16x16 alpha masks.'
