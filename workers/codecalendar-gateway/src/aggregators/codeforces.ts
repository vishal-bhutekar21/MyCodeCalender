import { NormalizedContest, ContestStatus } from '../types';

const CODEFORCES_API_URL = 'https://codeforces.com/api/contest.list?gym=false';

export async function fetchCodeforcesContests(): Promise<NormalizedContest[]> {
  try {
    const response = await fetch(CODEFORCES_API_URL, {
      headers: {
        'User-Agent': 'CodeCalendar-Gateway/1.0',
        Accept: 'application/json',
      },
    });

    if (!response.ok) {
      console.warn(`[Codeforces] HTTP error: ${response.status}`);
      return [];
    }

    const data = (await response.json()) as {
      status: string;
      result?: Array<{
        id: number;
        name: string;
        type: string;
        phase: string;
        durationSeconds: number;
        startTimeSeconds?: number;
      }>;
    };

    if (data.status !== 'OK' || !data.result) {
      return [];
    }

    const nowSec = Math.floor(Date.now() / 1000);
    const nowIso = new Date().toISOString();

    return data.result
      .filter((c) => {
        const start = c.startTimeSeconds || 0;
        // Keep upcoming, live, or recently finished (last 24 hours)
        return start + c.durationSeconds > nowSec - 86400;
      })
      .slice(0, 40) // Limit to top 40 relevant contests
      .map((c) => {
        const startSec = c.startTimeSeconds || nowSec;
        const endSec = startSec + c.durationSeconds;
        const startIso = new Date(startSec * 1000).toISOString();
        const endIso = new Date(endSec * 1000).toISOString();

        let status: ContestStatus = 'UPCOMING';
        if (c.phase === 'CODING' || (nowSec >= startSec && nowSec < endSec)) {
          status = 'LIVE';
        } else if (c.phase === 'FINISHED' || nowSec >= endSec) {
          status = 'ENDED';
        }

        return {
          id: `codeforces_${c.id}`,
          providerContestId: c.id.toString(),
          platform: 'CODEFORCES',
          name: c.name,
          officialUrl: `https://codeforces.com/contest/${c.id}`,
          registrationUrl: `https://codeforces.com/contestRegistration/${c.id}`,
          startTimeUtc: startIso,
          endTimeUtc: endIso,
          durationSeconds: c.durationSeconds,
          contestType: c.type,
          ratingType: 'CF Rating',
          status,
          lastFetchedAt: nowIso,
        };
      });
  } catch (err) {
    console.error('[Codeforces] Fetch failed:', err);
    return [];
  }
}
