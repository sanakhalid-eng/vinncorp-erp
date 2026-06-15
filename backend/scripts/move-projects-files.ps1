# VinnCorp ERP — Stage 3 (modules/projects) + remaining shared/ moves
# Moves directories wholesale and updates package declarations.

$ErrorActionPreference = "Continue"
$javaRoot = "C:\Users\Noman Traders\Documents\VinnCorp ERP\Project Management Tool\backend\src\main\java\com\vinncorp\erp"
$testRoot = "C:\Users\Noman Traders\Documents\VinnCorp ERP\Project Management Tool\backend\src\test\java\com\vinncorp\erp"

# Define: oldSubdir -> (newSubdir, oldPackagePrefix, newPackagePrefix)
# Note: All old packages start with "com.vinncorp.erp." + (oldSubdir with \ replaced by .)
$moves = New-Object System.Collections.ArrayList

# shared/ moves
[void]$moves.Add(@{src="security";         dst="shared\security";      oldPkg="com.vinncorp.erp.security";       newPkg="com.vinncorp.erp.shared.security"})
[void]$moves.Add(@{src="exception";        dst="shared\exception";     oldPkg="com.vinncorp.erp.exception";      newPkg="com.vinncorp.erp.shared.exception"})
[void]$moves.Add(@{src="filter";           dst="shared\filter";        oldPkg="com.vinncorp.erp.filter";         newPkg="com.vinncorp.erp.shared.filter"})
[void]$moves.Add(@{src="cache";            dst="shared\cache";         oldPkg="com.vinncorp.erp.cache";          newPkg="com.vinncorp.erp.shared.cache"})
[void]$moves.Add(@{src="tenant";           dst="shared\tenant";        oldPkg="com.vinncorp.erp.tenant";         newPkg="com.vinncorp.erp.shared.tenant"})
[void]$moves.Add(@{src="scheduling";       dst="shared\scheduling";    oldPkg="com.vinncorp.erp.scheduling";     newPkg="com.vinncorp.erp.shared.scheduling"})
[void]$moves.Add(@{src="jobs";             dst="shared\scheduling";    oldPkg="com.vinncorp.erp.jobs";           newPkg="com.vinncorp.erp.shared.scheduling"})
[void]$moves.Add(@{src="websocket";        dst="shared\websocket";     oldPkg="com.vinncorp.erp.websocket";      newPkg="com.vinncorp.erp.shared.websocket"})
[void]$moves.Add(@{src="mapper\PaginationMapper.java"; dst="shared\mapper"; oldPkg="com.vinncorp.erp.mapper"; newPkg="com.vinncorp.erp.shared.mapper"})
[void]$moves.Add(@{src="mapper\UserMapper.java";       dst="core\user";     oldPkg="com.vinncorp.erp.mapper"; newPkg="com.vinncorp.erp.core.user"})

# shared/config/ — move whole dir
[void]$moves.Add(@{src="config";           dst="shared\config";        oldPkg="com.vinncorp.erp.config";         newPkg="com.vinncorp.erp.shared.config"})

# core leftovers
[void]$moves.Add(@{src="constants";        dst="core\user";            oldPkg="com.vinncorp.erp.constants";      newPkg="com.vinncorp.erp.core.user"})
[void]$moves.Add(@{src="audit";            dst="core\audit";           oldPkg="com.vinncorp.erp.audit";          newPkg="com.vinncorp.erp.core.audit"})
[void]$moves.Add(@{src="notification";     dst="core\notification";    oldPkg="com.vinncorp.erp.notification";   newPkg="com.vinncorp.erp.core.notification"})
[void]$moves.Add(@{src="notifications";    dst="core\notification";    oldPkg="com.vinncorp.erp.notifications";  newPkg="com.vinncorp.erp.core.notification"})
[void]$moves.Add(@{src="user";             dst="core\user";            oldPkg="com.vinncorp.erp.user";           newPkg="com.vinncorp.erp.core.user"})

# modules/projects/ — everything PM-related
[void]$moves.Add(@{src="controller";       dst="modules\projects";     oldPkg="com.vinncorp.erp.controller";     newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="service";          dst="modules\projects";     oldPkg="com.vinncorp.erp.service";        newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="repository";       dst="modules\projects";     oldPkg="com.vinncorp.erp.repository";     newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="dto";              dst="modules\projects";     oldPkg="com.vinncorp.erp.dto";            newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="entity";           dst="modules\projects";     oldPkg="com.vinncorp.erp.entity";         newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="event";            dst="modules\projects";     oldPkg="com.vinncorp.erp.event";          newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="events";           dst="modules\projects";     oldPkg="com.vinncorp.erp.events";         newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="analytics";        dst="modules\projects";     oldPkg="com.vinncorp.erp.analytics";      newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="integration";      dst="modules\projects";     oldPkg="com.vinncorp.erp.integration";    newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="project";          dst="modules\projects";     oldPkg="com.vinncorp.erp.project";        newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="specification";    dst="modules\projects";     oldPkg="com.vinncorp.erp.specification";  newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="task";             dst="modules\projects";     oldPkg="com.vinncorp.erp.task";           newPkg="com.vinncorp.erp.modules.projects"})
[void]$moves.Add(@{src="workflow";         dst="modules\projects";     oldPkg="com.vinncorp.erp.workflow";       newPkg="com.vinncorp.erp.modules.projects"})

# ========== Apply moves ==========
foreach ($m in $moves) {
    $src = $m.src
    $dst = $m.dst
    $oldPkg = $m.oldPkg
    $newPkg = $m.newPkg

    $srcFull = Join-Path $javaRoot $src
    $dstFull = Join-Path $javaRoot $dst

    if (-not (Test-Path -LiteralPath $srcFull)) {
        Write-Host "  [SKIP] $src (not found)"
        continue
    }

    # Ensure dest dir exists
    if (-not (Test-Path -LiteralPath $dstFull)) {
        New-Item -ItemType Directory -Path $dstFull -Force | Out-Null
    }

    # If src is a file, move file
    if ((Get-Item -LiteralPath $srcFull) -is [System.IO.FileInfo]) {
        $fileName = Split-Path -Path $srcFull -Leaf
        $destPath = Join-Path $dstFull $fileName
        Move-Item -LiteralPath $srcFull -Destination $destPath -Force
    } else {
        # src is a directory — move all its contents
        Get-ChildItem -LiteralPath $srcFull | ForEach-Object {
            $target = Join-Path $dstFull $_.Name
            if (Test-Path -LiteralPath $target) {
                # target exists, move INTO it
                Move-Item -LiteralPath $_.FullName -Destination $target -Force
            } else {
                Move-Item -LiteralPath $_.FullName -Destination $dstFull -Force
            }
        }
        # Remove now-empty source dir
        Remove-Item -LiteralPath $srcFull -Recurse -Force -ErrorAction SilentlyContinue
    }

    # Update package declarations + imports in moved files
    $filesToUpdate = if ((Get-Item -LiteralPath $dstFull) -is [System.IO.FileInfo]) {
        @($dstFull)
    } else {
        Get-ChildItem -LiteralPath $dstFull -Recurse -Filter *.java -ErrorAction SilentlyContinue
    }

    foreach ($f in $filesToUpdate) {
        $c = Get-Content -LiteralPath $f.FullName -Raw
        $orig = $c
        # Update package declaration
        $c = [regex]::Replace(
            $c,
            '^package\s+' + [regex]::Escape($oldPkg) + '[\w\.]*;',
            "package $newPkg;",
            [System.Text.RegularExpressions.RegexOptions]::Multiline
        )
        # Update imports
        $c = $c -replace ([regex]::Escape($oldPkg) + '\.'), ($newPkg + '.')
        if ($c -ne $orig) {
            Set-Content -LiteralPath $f.FullName -Value $c -NoNewline
        }
    }

    Write-Host "  [OK] $src -> $dst"
}

Write-Host ""
Write-Host "=== Done. Cleaning up empty dirs ==="

# Remove any remaining empty dirs at the top level
Get-ChildItem -LiteralPath $javaRoot -Directory | ForEach-Object {
    if ((Get-ChildItem -LiteralPath $_.FullName -Recurse -ErrorAction SilentlyContinue | Measure-Object).Count -eq 0) {
        Remove-Item -LiteralPath $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "  removed empty: $($_.Name)"
    }
}

Get-ChildItem -LiteralPath $javaRoot | Select-Object Name
