import { NormalizedContest, ContestStatus } from '../types';

const LEETCODE_GRAPHQL_URL = 'https://leetcode.com/graphql';

const CONTESTS_QUERY = `
  query {
    allContests {
      title
      titleSlug
      startTime
      duration
      originStartTime
      isVirtual
    }
  }
`;

export async function fetchLeetCodeContests(): Promise<NormalizedContest[]> {
  try {
    const response = await fetch(LEETCODE_GRAPHQL_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'User-Agent': 'Mozilla/5.0 (Cloudflare Worker; CodeCalendar Gateway)',
      },
      body: JSON.stringify({ query: CONTESTS_QUERY }),
    });

    if (!response.ok) {
      console.warn(`[LeetCode] HTTP error: ${response.status}`);
      return [];
    }

    const json = (await response.json()) as {
      data?: {
        allContests?: Array<{
          title: string;
          titleSlug: string;
          startTime: number;
          duration: number;
        }>;
      };
    };

    const contests = json.data?.allContests || [];
    const nowSec = Math.floor(Date.now() / 1000);
    const nowIso = new Date().toISOString();

    return contests
      .filter((c) => c.startTime + c.duration > nowSec - 86400) // within last 24h or future
      .map((c) => {
        const startMs = c.startTime * 1000;
        const endMs = (c.startTime + c.duration) * 1000;
        const startIso = new Date(startMs).toISOString();
        const endIso = new Date(endMs).toISOString();

        let status: ContestStatus = 'UPCOMING';
        if (nowSec >= c.startTime && nowSec < c.startTime + c.duration) {
          status = 'LIVE';
        } else if (nowSec >= c.startTime + c.duration) {
          status = 'ENDED';
        }

        return {
          id: `leetcode_${c.titleSlug}`,
          providerContestId: c.titleSlug,
          platform: 'LEETCODE',
          name: c.title,
          officialUrl: `https://leetcode.com/contest/${c.titleSlug}`,
          registrationUrl: `https://leetcode.com/contest/${c.titleSlug}`,
          startTimeUtc: startIso,
          endTimeUtc: endIso,
          durationSeconds: c.duration,
          contestType: 'Weekly/Biweekly',
          ratingType: 'LeetCode Rating',
          status,
          lastFetchedAt: nowIso,
        };
      });
  } catch (err) {
    console.error('[LeetCode] Fetch failed:', err);
    return [];
  }
}
