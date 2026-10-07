export type Platform =
  | 'CODEFORCES'
  | 'LEETCODE'
  | 'CODECHEF'
  | 'ATCODER'
  | 'GEEKSFORGEEKS'
  | 'GITHUB';

export type ContestStatus = 'UPCOMING' | 'LIVE' | 'ENDED';

export interface NormalizedContest {
  id: string;
  providerContestId: string;
  platform: Platform;
  name: string;
  officialUrl: string;
  registrationUrl: string | null;
  startTimeUtc: string; // ISO 8601
  endTimeUtc: string; // ISO 8601
  durationSeconds: number;
  contestType: string | null;
  ratingType: string | null;
  status: ContestStatus;
  lastFetchedAt: string;
}

export interface UserStatsPayload {
  platform: Platform;
  handle: string;
  rating?: number;
  maxRating?: number;
  rank?: string;
  solvedCount?: number;
  easySolved?: number;
  mediumSolved?: number;
  hardSolved?: number;
  globalRank?: number;
  totalContributions?: number;
  currentStreak?: number;
  avatarUrl?: string;
  fetchedAt: string;
}

export interface Env {
  CONTEST_CACHE: KVNamespace;
  ENVIRONMENT: string;
  APP_NAME: string;
  API_VERSION: string;
  GITHUB_TOKEN?: string;
}
