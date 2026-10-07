import { NormalizedContest, ContestStatus } from '../types';

const ATCODER_RESOURCES_URL = 'https://kenkoooo.com/atcoder/resources/contests.json';

interface AtCoderRawContest {
  id: string;
  title: string;
  start_epoch_second: number;
  duration_second: number;
  rate_change: string;
}

export async function fetchAtCoderContests(): Promise<NormalizedContest[]> {
  try {
    const response = await fetch(ATCODER_RESOURCES_URL, {
      headers: {
        'User-Agent': 'CodeCalendar-Gateway/1.0',
        Accept: 'application/json',
      },
    });

    if (!response.ok) {
      console.warn(`[AtCoder] HTTP error: ${response.status}`);
      return [];
    }

    const items = (await response.json()) as AtCoderRawContest[];
    const nowSec = Math.floor(Date.now() / 1000);
    const nowIso = new Date().toISOString();

    return items
      .filter((c) => {
        const start = c.start_epoch_second;
        // Keep active or upcoming within the next 60 days, or ended in the last 24h
        return start + c.duration_second > nowSec - 86400 && start < nowSec + 60 * 86400;
      })
      .slice(0, 30)
      .map((c) => {
        const startMs = c.start_epoch_second * 1000;
        const endMs = (c.start_epoch_second + c.duration_second) * 1000;
        const endSec = c.start_epoch_second + c.duration_second;

        let status: ContestStatus = 'UPCOMING';
        if (nowSec >= c.start_epoch_second && nowSec < endSec) {
          status = 'LIVE';
        } else if (nowSec >= endSec) {
          status = 'ENDED';
        }

        return {
          id: `atcoder_${c.id}`,
          providerContestId: c.id,
          platform: 'ATCODER',
          name: c.title,
          officialUrl: `https://atcoder.jp/contests/${c.id}`,
          registrationUrl: `https://atcoder.jp/contests/${c.id}`,
          startTimeUtc: new Date(startMs).toISOString(),
          endTimeUtc: new Date(endMs).toISOString(),
          durationSeconds: c.duration_second,
          contestType: c.rate_change,
          ratingType: 'AtCoder Rating',
          status,
          lastFetchedAt: nowIso,
        };
      });
  } catch (err) {
    console.error('[AtCoder] Fetch failed:', err);
    return [];
  }
}
