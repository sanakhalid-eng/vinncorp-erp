// Standard API response wrapper (matches backend ApiResponse<T>)
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T;
}

// Auth responses
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  userId: number | string;
  message?: string;
}

export interface TwoFactorRequiredResponse {
  message: string;
  requires2FA: true;
  userId: number | string;
}

export interface TwoFactorSetupData {
  secretKey: string;
  qrCodeUrl: string;
  totpAuthUrl: string;
}

export interface TwoFactorStatus {
  enabled: boolean;
}

export interface TwoFactorVerifyResult {
  backupCodes: string[];
  message: string;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role?: string;
  roles?: string[];
  avatarUrl?: string;
  enabled?: boolean;
  emailVerified?: boolean;
  twoFactorEnabled?: boolean;
  projectCount?: number;
  createdAt?: string;
}

export interface Role {
  id: number;
  name: string;
  description?: string;
  system?: boolean;
  permissions?: string[];
}

export interface Permission {
  id: number;
  name: string;
  description?: string;
}

export interface Project {
  id: number;
  name: string;
  description?: string;
  key?: string;
  status?: string;
  statusName?: string;
  priority?: string;
  startDate?: string;
  endDate?: string;
  owner?: User;
  members?: ProjectMember[];
  memberCount?: number;
  createdAt?: string;
  updatedAt?: string;

  // Extended fields
  category?: string;
  objectives?: string;
  budget?: number;
  currency?: string;
  tags?: string;
  isActive?: boolean;
  isPublic?: boolean;
  projectManagerId?: number | string;
}

export interface ProjectMember {
  id: number;
  user: User;
  role?: string;
  joinedAt?: string;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  status?: string;
  statusId?: number | string;
  priority?: string;
  assignee?: User | null;
  project?: Project | { id: number };
  createdBy?: User | null;
  dueDate?: string;
  createdAt?: string;
  updatedAt?: string;
  storyPoints?: number;
  labels?: Label[];
  dependencies?: TaskDependency[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface Sprint {
  id: number;
  name: string;
  goal?: string;
  startDate?: string;
  endDate?: string;
  status?: string;
  projectId: number;
  createdAt?: string;
}

export interface Board {
  id: number;
  name: string;
  projectId: number;
  columns?: BoardColumn[];
}

export interface BoardColumn {
  id: number;
  name: string;
  position: number;
  statusId?: number | string;
  tasks?: Task[];
}

export interface Label {
  id: number;
  name: string;
  color?: string;
  projectId?: number;
}

export interface Comment {
  id: number;
  content: string;
  author: User;
  taskId: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface TaskDependency {
  id: number;
  taskId: number;
  dependsOnTaskId: number;
  dependsOnTask?: Task;
  dependencyType?: string;
}

export interface Attachment {
  id: number;
  fileName: string;
  fileUrl: string;
  fileSize?: number;
  taskId: number;
  uploadedBy?: User;
  createdAt?: string;
}

export interface TimeLog {
  id: number;
  taskId: number;
  userId: number;
  hours: number;
  description?: string;
  date?: string;
  createdAt?: string;
}

export interface Notification {
  id: number;
  type: string;
  title: string;
  message?: string;
  read: boolean;
  createdAt: string;
}

export interface Workspace {
  id: number;
  name: string;
  slug: string;
  plan?: string;
  ownerId?: number;
  memberCount?: number;
  createdAt?: string;
}

export interface ActivityLog {
  id: number;
  action: string;
  entityType: string;
  entityId: number;
  user: User;
  details?: string;
  createdAt: string;
}

export interface Webhook {
  id: number;
  name: string;
  url: string;
  events: string[];
  enabled: boolean;
  secret?: string;
  projectId?: number;
  createdAt?: string;
}

export interface DashboardStats {
  totalProjects?: number;
  activeTasks?: number;
  completedTasks?: number;
  overdueTasks?: number;
  teamMembers?: number;
}

export interface AccessTokenPayload {
  sub: string;
  userId: number;
  roles: string[];
  workspaceId?: number;
  exp: number;
  iat: number;
}

export type BulkUpdatePayload = {
  statusId?: number | string;
  assigneeId?: number | string;
  priority?: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};

/* =========================
   Phase 3 Types
========================= */

export interface SavedSearch {
  id: number;
  name: string;
  query: string;
  filters?: Record<string, unknown>;
  createdAt?: string;
}

export interface KnowledgeArticle {
  id: number;
  title: string;
  slug: string;
  content?: string;
  summary?: string;
  published?: boolean;
  tags?: string[];
  author?: User;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkspaceNote {
  id: number;
  title: string;
  content?: string;
  pinned?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ActivityIntelligence {
  activeUsers?: number;
  tasksCompletedToday?: number;
  overdueTasks?: number;
  productivityScore?: number;
}

export interface ExecutiveDashboard {
  totalRevenue?: number;
  activeProjects?: number;
  velocity?: number;
  deliveryHealth?: number;
}

export interface ExecutiveTrend {
  id: number;
  metric: string;
  value: number;
  recordedAt: string;
}

export interface DeliveryPredictability {
  projectId: number;
  predictabilityScore: number;
  onTrack: boolean;
}

export interface ProductivityStats {
  completedTasks?: number;
  focusHours?: number;
  efficiency?: number;
}

export interface NotificationIntelligence {
  unreadCount?: number;
  priorityAlerts?: number;
}

export interface QuickAction {
  label: string;
  action: string;
  category?: string;
}

export interface CalendarIntelligence {
  upcomingDeadlines?: number;
  meetingsToday?: number;
}

export interface CommandPaletteItem {
  actionKey?: string;
  actionLabel?: string;
  title?: string;
  subtitle?: string;
  targetUrl?: string;
  url?: string;
  category?: string;
  badge?: string;
}

export interface MonteCarloForecast {
  completionProbability?: number;
  projectedCompletionDate?: string;
}

export interface CapacityForecast {
  availableHours?: number;
  requiredHours?: number;
  utilization?: number;
}