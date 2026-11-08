param([int]$Port = 25586)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot
$properties = Get-Content (Join-Path $projectRoot 'run/server/server.properties')
$password = ($properties | Where-Object { $_ -like 'rcon.password=*' }).Substring(14)
$client = [Net.Sockets.TcpClient]::new('127.0.0.1', $Port)
$stream = $client.GetStream()
$stream.ReadTimeout = 10000
function Read-Exact([int]$length) {
    $buffer = [byte[]]::new($length)
    $offset = 0
    while ($offset -lt $length) {
        $read = $stream.Read($buffer, $offset, $length - $offset)
        if ($read -eq 0) { throw 'RCON disconnected' }
        $offset += $read
    }
    return ,$buffer
}
function Send-Rcon([int]$id, [int]$type, [string]$body) {
    $bytes = [Text.Encoding]::UTF8.GetBytes($body)
    $memory = [IO.MemoryStream]::new()
    $writer = [IO.BinaryWriter]::new($memory)
    $writer.Write([int]($bytes.Length + 10))
    $writer.Write($id)
    $writer.Write($type)
    $writer.Write($bytes)
    $writer.Write([byte]0)
    $writer.Write([byte]0)
    $packet = $memory.ToArray()
    $writer.Dispose()
    $stream.Write($packet, 0, $packet.Length)
    $length = [BitConverter]::ToInt32((Read-Exact 4), 0)
    $response = Read-Exact $length
    if ([BitConverter]::ToInt32($response, 0) -eq -1) { throw 'RCON authentication failed' }
    return [Text.Encoding]::UTF8.GetString($response, 8, $response.Length - 10)
}
try {
    $null = Send-Rcon 1 3 $password
    $results = [Collections.Generic.List[string]]::new()
    $results.Add((Send-Rcon 2 2 'list'))
    $items = @('iron_mining_hammer', 'copper_mining_hammer', 'gold_mining_hammer',
        'diamond_mining_hammer', 'wood_wand', 'stone_wand', 'iron_wand', 'gold_wand',
        'diamond_wand', 'netherite_wand', 'infinity_wand', 'core_angel',
        'core_destruction', 'core_exchange', 'void_sack', 'building_component',
        'precision_building_component', 'blank_core', 'iron_wand_head', 'gold_wand_head',
        'diamond_wand_head', 'netherite_wand_head', 'infinity_wand_head')
    $id = 3
    $null = Send-Rcon ($id++) 2 'forceload add 0 0'
    $null = Send-Rcon ($id++) 2 'kill @e[type=minecraft:item,tag=rabbittools_smoke]'
    foreach ($item in $items) {
        $command = 'summon minecraft:item 0 100 0 {Tags:["rabbittools_smoke"],Item:{id:"rabbittools:' + $item + '",count:1}}'
        $null = Send-Rcon ($id++) 2 $command
        $response = Send-Rcon ($id++) 2 'data get entity @e[type=minecraft:item,tag=rabbittools_smoke,limit=1] Item.id'
        if ($response -notlike "*rabbittools:$item*") {
            throw "Registration smoke check failed: $item / $response"
        }
        $results.Add("PASS rabbittools:$item")
        $null = Send-Rcon ($id++) 2 'kill @e[type=minecraft:item,tag=rabbittools_smoke]'
    }
    $null = Send-Rcon ($id++) 2 'forceload remove 0 0'
    $results.Add((Send-Rcon ($id++) 2 'datapack list enabled'))
    $results.Add((Send-Rcon ($id++) 2 'save-all flush'))
    $results | Set-Content (Join-Path $projectRoot 'build/registration-smoke.txt') -Encoding utf8
    $results | Write-Output
} finally {
    try { Send-Rcon 999 2 'stop' | Write-Output } finally { $client.Dispose() }
}