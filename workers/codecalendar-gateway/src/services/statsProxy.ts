import { Env, Platform, UserStatsPayload } from '../types';

export async function fetchUserStatsProxy(
  platform: Platform,
  handle: string,
  env: Env
): Promise<UserStatsPayload | null> {
  const cacheKey = `user:stats:${platform.toLowerCase()}:${handle.toLowerCase()}`;

  // 1. Check KV Cache (30 min TTL)
  try {
    const cached = await env.CONTEST_CACHE.get(cacheKey, 'json');
    if (cached) {
      return cached as UserStatsPayload;
    }
  } catch (err) {
    console.warn(`[KV] Cache lookup failed for ${cacheKey}:`, err);
  }

  // 2. Fetch fresh data based on platform
  let payload: UserStatsPayload | null = null;
  const nowIso = new Date().toISOString();

  if (platform === 'GITHUB') {
    payload = await fetchGitHubStats(handle, env.GITHUB_TOKEN);
  } else if (platform === 'CODEFORCES') {
    payload = await fetchCodeforcesStats(handle);
  } else if (platform === 'LEETCODE') {
    payload = await fetchLeetCodeStats(handle);
  }

  if (payload) {
    payload.fetchedAt = nowIso;
    // Cache for 30 minutes (1800 seconds)
    try {
      await env.CONTEST_CACHE.put(cacheKey, JSON.stringify(payload), {
        expirationTtl: 1800,
      });
    } catch (err) {
      console.warn(`[KV] Failed to store ${cacheKey}:`, err);
    }
  }

  return payload;
}

async function fetchGitHubStats(username: string, token?: string): Promise<UserStatsPayload | null> {
  try {
    const headers: Record<string, string> = {
      'User-Agent': 'CodeCalendar-Edge-Proxy/1.0',
      Accept: 'application/vnd.github.v3+json',
    };
    if (token) {
      headers.Authorization = `token ${token}`;
    }

    const res = await fetch(`https://api.github.com/users/${encodeURIComponent(username)}`, { headers });
    if (!res.ok) return null;

    const data = (await res.json()) as {
      public_repos?: number;
      followers?: number;
      avatar_url?: string;
    };

    return {
      platform: 'GITHUB',
      handle: username,
      solvedCount: data.public_repos || 0,
      avatarUrl: data.avatar_url,
      fetchedAt: new Date().toISOString(),
    };
  } catch {
    return null;
  }
}

async function fetchCodeforcesStats(handle: string): Promise<UserStatsPayload | null> {
  try {
    const res = await fetch(`https://codeforces.com/api/user.info?handles=${encodeURIComponent(handle)}`, {
      headers: {
        'User-Agent': 'CodeCalendar-Edge-Proxy/1.0',
        Accept: 'application/json',
      },
    });
    if (!res.ok) return null;

    const json = (await res.json()) as {
      status: string;
      result?: Array<{
        rating?: number;
        maxRating?: number;
        rank?: string;
        titlePhoto?: string;
      }>;
    };

    if (json.status !== 'OK' || !json.result || json.result.length === 0) return null;
    const user = json.result[0];

    return {
      platform: 'CODEFORCES',
      handle,
      rating: user.rating || 0,
      maxRating: user.maxRating || 0,
      rank: user.rank || 'unrated',
      avatarUrl: user.titlePhoto,
      fetchedAt: new Date().toISOString(),
    };
  } catch {
    return null;
  }
}

async function fetchLeetCodeStats(handle: string): Promise<UserStatsPayload | null> {
  try {
    const query = `
      query userProfile($username: String!) {
        matchedUser(username: $username) {
          profile {
            ranking
            userAvatar
          }
          submitStats: submitStatsGlobal {
            acSubmissionNum {
              difficulty
              count
            }
          }
        }
      }
    `;

    const res = await fetch('https://leetcode.com/graphql', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'CodeCalendar-Edge-Proxy/1.0',
      },
      body: JSON.stringify({
        query,
        variables: { username: handle },
      }),
    });

    if (!res.ok) return null;
    const json = (await res.json()) as {
      data?: {
        matchedUser?: {
          profile?: {
            ranking?: number;
            userAvatar?: string;
          };
          submitStats?: {
            acSubmissionNum?: Array<{ difficulty: string; count: number }>;
          };
        };
      };
    };

    const user = json.data?.matchedUser;
    if (!user) return null;

    const subs = user.submitStats?.acSubmissionNum || [];
    const all = subs.find((s) => s.difficulty === 'All')?.count || 0;
    const easy = subs.find((s) => s.difficulty === 'Easy')?.count || 0;
    const med = subs.find((s) => s.difficulty === 'Medium')?.count || 0;
    const hard = subs.find((s) => s.difficulty === 'Hard')?.count || 0;

    return {
      platform: 'LEETCODE',
      handle,
      solvedCount: all,
      easySolved: easy,
      mediumSolved: med,
      hardSolved: hard,
      globalRank: user.profile?.ranking,
      avatarUrl: user.profile?.userAvatar,
      fetchedAt: new Date().toISOString(),
    };
  } catch {
    return null;
  }
}
