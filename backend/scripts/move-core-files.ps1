# VinnCorp ERP Module Restructuring Script
# Stage 2 + Stage 3: Move files from com.vinncorp.erp.{X} to com.vinncorp.erp.{core|shared|modules.projects}.{X}
# with package declaration updates.

$ErrorActionPreference = "Stop"
$javaRoot = "C:\Users\Noman Traders\Documents\VinnCorp ERP\Project Management Tool\backend\src\main\java\com\vinncorp\erp"
$testRoot = "C:\Users\Noman Traders\Documents\VinnCorp ERP\Project Management Tool\backend\src\test\java\com\vinncorp\erp"

# Map: sourceRelativePath -> destModuleSubPath
# e.g. "auth\AuthService.java" -> "core\auth\AuthService.java"   (package: com.vinncorp.erp.core.auth)
# e.g. "entity\User.java" -> "core\user\User.java"              (package: com.vinncorp.erp.core.user)
$moves = @{
    # ========== core/auth/ ==========
    "controller\AuthController.java"               = "core\auth"
    "controller\TwoFactorController.java"         = "core\auth"
    "service\AuthService.java"                     = "core\auth"
    "service\TwoFactorService.java"                = "core\auth"
    "service\PasswordResetService.java"            = "core\auth"
    "service\RefreshTokenService.java"             = "core\auth"
    "service\CustomUserDetailsService.java"        = "core\auth"
    "service\impl\AuthServiceImpl.java"            = "core\auth"
    "service\impl\PasswordResetServiceImpl.java"   = "core\auth"
    "entity\EmailVerificationToken.java"           = "core\auth"
    "entity\PasswordResetToken.java"               = "core\auth"
    "entity\RefreshToken.java"                     = "core\auth"
    "entity\UserTwoFactor.java"                    = "core\auth"
    "repository\EmailVerificationTokenRepository.java" = "core\auth"
    "repository\PasswordResetTokenRepository.java" = "core\auth"
    "repository\RefreshTokenRepository.java"       = "core\auth"
    "repository\TwoFactorRepository.java"          = "core\auth"
    "dto\request\LoginRequest.java"                = "core\auth"
    "dto\request\ChangePasswordRequest.java"       = "core\auth"
    "dto\response\LoginResponse.java"              = "core\auth"

    # ========== core/user/ ==========
    "entity\User.java"                             = "core\user"
    "entity\UserRole.java"                         = "core\user"
    "entity\Role.java"                             = "core\user"
    "entity\RoleAuditLog.java"                     = "core\user"
    "entity\Permission.java"                       = "core\user"
    "entity\RolePermission.java"                   = "core\user"
    "repository\UserRepository.java"               = "core\user"
    "repository\UserRoleRepository.java"           = "core\user"
    "repository\RoleRepository.java"               = "core\user"
    "repository\RolePermissionRepository.java"     = "core\user"
    "repository\PermissionRepository.java"         = "core\user"
    "service\UserService.java"                     = "core\user"
    "service\UserRoleService.java"                 = "core\user"
    "service\RoleService.java"                     = "core\user"
    "service\impl\UserServiceImpl.java"            = "core\user"
    "service\impl\UserRoleServiceImpl.java"        = "core\user"
    "service\impl\RoleServiceImpl.java"            = "core\user"
    "controller\UserController.java"               = "core\user"
    "controller\UserRoleController.java"           = "core\user"
    "controller\RoleController.java"               = "core\user"
    "controller\PermissionController.java"         = "core\user"
    "dto\request\UpdateUserRequest.java"           = "core\user"
    "dto\request\RegisterRequest.java"             = "core\user"
    "dto\response\RegisterResponse.java"           = "core\user"
    "dto\response\UserResponse.java"               = "core\user"
    "dto\response\UserSummary.java"                = "core\user"
    "constants\PermissionConstants.java"           = "core\user"

    # ========== core/workspace/ ==========
    "entity\Workspace.java"                        = "core\workspace"
    "entity\WorkspaceMember.java"                  = "core\workspace"
    "entity\WorkspaceRole.java"                    = "core\workspace"
    "entity\WorkspacePermissionMatrix.java"        = "core\workspace"
    "entity\WorkspaceInvitation.java"              = "core\workspace"
    "entity\WorkspaceNote.java"                    = "core\workspace"
    "entity\WorkspaceUsage.java"                   = "core\workspace"
    "entity\enums\InvitationStatus.java"           = "core\workspace"
    "repository\WorkspaceRepository.java"          = "core\workspace"
    "repository\WorkspaceMemberRepository.java"    = "core\workspace"
    "repository\WorkspaceRoleRepository.java"      = "core\workspace"
    "repository\WorkspacePermissionMatrixRepository.java" = "core\workspace"
    "repository\WorkspaceInvitationRepository.java"= "core\workspace"
    "repository\WorkspaceNoteRepository.java"      = "core\workspace"
    "repository\WorkspaceUsageRepository.java"     = "core\workspace"
    "service\WorkspaceService.java"                = "core\workspace"
    "service\WorkspaceInvitationService.java"      = "core\workspace"
    "service\WorkspaceNoteService.java"            = "core\workspace"
    "service\impl\WorkspaceNoteServiceImpl.java"   = "core\workspace"
    "controller\WorkspaceController.java"          = "core\workspace"
    "controller\WorkspaceInvitationController.java"= "core\workspace"
    "controller\WorkspaceNoteController.java"      = "core\workspace"
    "workspace\CurrentWorkspaceResolver.java"       = "core\workspace"
    "workspace\CurrentTenantResolver.java"         = "core\workspace"
    "workspace\DefaultWorkspaceResolver.java"       = "core\workspace"
    "workspace\TenantContext.java"                 = "core\workspace"
    "dto\request\CreateWorkspaceRequest.java"      = "core\workspace"
    "dto\request\CreateWorkspaceInvitationRequest.java" = "core\workspace"
    "dto\request\WorkspaceNoteRequest.java"        = "core\workspace"
    "dto\request\WorkspacePreferencesRequest.java" = "core\workspace"
    "dto\request\TransferOwnershipRequest.java"    = "core\workspace"
    "dto\response\WorkspaceResponse.java"          = "core\workspace"
    "dto\response\WorkspaceMemberResponse.java"    = "core\workspace"
    "dto\response\WorkspaceInvitationResponse.java"= "core\workspace"
    "dto\response\WorkspaceNoteResponse.java"      = "core\workspace"
    "dto\response\WorkspaceSettingsResponse.java"  = "core\workspace"
    "dto\response\WorkspaceSummary.java"           = "core\workspace"
    "dto\response\SystemSettingsResponse.java"     = "core\workspace"

    # ========== core/notification/ ==========
    "entity\Notification.java"                     = "core\notification"
    "entity\NotificationPreference.java"           = "core\notification"
    "entity\enums\NotificationType.java"           = "core\notification"
    "entity\enums\NotificationCategory.java"       = "core\notification"
    "repository\NotificationRepository.java"       = "core\notification"
    "repository\NotificationPreferenceRepository.java" = "core\notification"
    "service\NotificationService.java"             = "core\notification"
    "service\NotificationIntelligenceService.java" = "core\notification"
    "service\impl\NotificationServiceImpl.java"    = "core\notification"
    "service\impl\NotificationIntelligenceServiceImpl.java" = "core\notification"
    "controller\NotificationController.java"       = "core\notification"
    "controller\NotificationPreferenceController.java" = "core\notification"
    "dto\request\NotificationPreferenceRequest.java" = "core\notification"
    "dto\response\NotificationResponse.java"       = "core\notification"
    "dto\response\NotificationPreferenceResponse.java" = "core\notification"
    "dto\response\NotificationIntelligenceResponse.java" = "core\notification"
    "events\EventPublisher.java"                   = "core\notification"
    "events\DomainEvent.java"                      = "core\notification"
    "events\NotificationEventProcessor.java"       = "core\notification"
    "events\TaskEventProcessor.java"               = "core\notification"
    "events\WebhookEventProcessor.java"            = "core\notification"
    "listener\EmailNotificationListener.java"      = "core\notification"
    "notifications\NotificationRulesEngine.java"   = "core\notification"

    # ========== core/audit/ ==========
    "entity\ActivityLog.java"                      = "core\audit"
    "entity\ActivityIntelligenceSummary.java"      = "core\audit"
    "entity\enums\ActionType.java"                 = "core\audit"
    "entity\enums\EntityType.java"                 = "core\audit"
    "repository\ActivityLogRepository.java"        = "core\audit"
    "repository\ActivityIntelligenceSummaryRepository.java" = "core\audit"
    "service\ActivityLogService.java"              = "core\audit"
    "service\ActivityIntelligenceService.java"     = "core\audit"
    "service\impl\ActivityLogServiceImpl.java"     = "core\audit"
    "service\impl\ActivityIntelligenceServiceImpl.java" = "core\audit"
    "controller\ActivityLogController.java"        = "core\audit"
    "controller\ActivityIntelligenceController.java" = "core\audit"
    "dto\response\ActivityLogResponse.java"        = "core\audit"
    "dto\response\ActivityIntelligenceResponse.java" = "core\audit"

    # ========== core/integrations/ ==========
    "entity\Webhook.java"                          = "core\integrations"
    "entity\WebhookDelivery.java"                  = "core\integrations"
    "entity\WebhookDeliveryStatus.java"            = "core\integrations"
    "entity\SlackIntegration.java"                 = "core\integrations"
    "entity\SlackUserMapping.java"                 = "core\integrations"
    "entity\FeatureFlag.java"                      = "core\integrations"
    "repository\WebhookRepository.java"            = "core\integrations"
    "repository\WebhookDeliveryRepository.java"    = "core\integrations"
    "repository\SlackIntegrationRepository.java"   = "core\integrations"
    "repository\SlackUserMappingRepository.java"   = "core\integrations"
    "repository\FeatureFlagRepository.java"        = "core\integrations"
    "service\WebhookService.java"                  = "core\integrations"
    "service\WebhookRetryService.java"             = "core\integrations"
    "service\WebhookSignatureService.java"         = "core\integrations"
    "service\SlackService.java"                    = "core\integrations"
    "service\FeatureFlagService.java"              = "core\integrations"
    "controller\WebhookController.java"            = "core\integrations"
    "controller\SlackController.java"              = "core\integrations"
    "dto\request\WebhookRequest.java"              = "core\integrations"
    "dto\response\WebhookResponse.java"            = "core\integrations"
    "dto\response\WebhookDeliveryResponse.java"    = "core\integrations"
    "dto\response\SlackIntegrationResponse.java"   = "core\integrations"
}

# Files to DELETE (duplicates, deprecated, etc.)
$deletes = @(
    "notifications\NotificationEventProcessor.java"  # duplicate of events/NotificationEventProcessor
)

# ========== Apply moves ==========
$moved = 0
$skipped = 0
foreach ($key in $moves.Keys) {
    $srcRel = $key
    $destMod = $moves[$key]
    $fileName = Split-Path -Path $srcRel -Leaf
    $srcFull = Join-Path $javaRoot $srcRel
    $destFull = Join-Path $javaRoot (Join-Path $destMod $fileName)

    if (-not (Test-Path -LiteralPath $srcFull)) {
        Write-Host "  [SKIP] source not found: $srcRel"
        $skipped++
        continue
    }

    # Compute new package from destMod (e.g. "core\auth" -> "com.vinncorp.erp.core.auth")
    $newPackage = "com.vinncorp.erp." + ($destMod -replace "\\", ".")

    # Read file, update package, write to dest
    $content = Get-Content -LiteralPath $srcFull -Raw

    # Update package declaration
    $content = [regex]::Replace(
        $content,
        '^package\s+com\.vinncorp\.erp\.[\w\.]+;',
        "package $newPackage;",
        [System.Text.RegularExpressions.RegexOptions]::Multiline
    )

    # Ensure dest dir exists
    $destDir = Split-Path -Path $destFull -Parent
    if (-not (Test-Path -LiteralPath $destDir)) {
        New-Item -ItemType Directory -Path $destDir -Force | Out-Null
    }

    Set-Content -LiteralPath $destFull -Value $content -NoNewline
    Remove-Item -LiteralPath $srcFull -Force
    $moved++
}

# ========== Apply deletes ==========
$deleted = 0
foreach ($rel in $deletes) {
    $full = Join-Path $javaRoot $rel
    if (Test-Path -LiteralPath $full) {
        Remove-Item -LiteralPath $full -Force
        Write-Host "  [DELETE] $rel"
        $deleted++
    }
}

Write-Host ""
Write-Host "=== Summary ==="
Write-Host "Moved:    $moved files"
Write-Host "Skipped:  $skipped files (source not found)"
Write-Host "Deleted:  $deleted files (duplicates/deprecated)"
