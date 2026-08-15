export type MaterialCategory =
  | 'DSA & CP Sheets'
  | 'AI & ML'
  | 'YouTube Playlists'
  | 'System Design'
  | 'Web & Mobile'
  | 'Articles & Roadmaps';

export type PriorityLevel = 1 | 2 | 3;

export interface FeaturedMaterial {
  id: string;
  title: string;
  description: string;
  category: MaterialCategory;
  creator: string;
  imageUrl?: string;
  redirectUrl: string;
  priority: number; // 1 = Top Pick (⭐), 2 = Featured (⚡), 3+ = Standard
  isActive: boolean;
  tags: string[];
  readTimeOrDuration?: string;
  createdAt?: any;
  updatedAt?: any;
}

export type BroadcastBadge = 'NOTICE' | 'ALERT' | 'HOT' | 'UPDATE';

export interface Broadcast {
  id: string;
  title: string;
  subtitle: string;
  badge: BroadcastBadge;
  actionUrl: string;
  bannerImageUrl?: string;
  isActive: boolean;
  priority?: number;
  createdAt?: any;
  updatedAt?: any;
}

export interface CustomContest {
  id: string;
  name: string;
  organizer: string;
  bannerUrl?: string;
  startTime: number; // Epoch millis
  endTime: number;   // Epoch millis
  registrationUrl: string;
  tags: string[];
  platformName?: string;
  isActive: boolean;
  createdAt?: any;
}

export interface UserAccount {
  uid: string;
  email?: string;
  displayName?: string;
  photoUrl?: string;
  authProvider?: string;
  connectedPlatforms?: string[];
  connectedAccountsMap?: Record<string, string>;
  currentStreak?: number;
  streakCount?: number;
  highestStreak?: number;
  activeDates?: string[];
  lastStreakSyncAt?: any;
  lastLoginAt?: any;
  createdAt?: any;
  appVersion?: string;
}

export interface DeletionRequest {
  id: string;
  uid: string;
  email: string;
  displayName: string;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'COMPLETED' | 'REJECTED';
  requestedAt?: any;
  resolvedAt?: any;
}

export interface DashboardMetrics {
  totalUsers: number;
  activeBroadcasts: number;
  publishedMaterials: number;
  pendingDeletions: number;
  totalCustomContests: number;
}
