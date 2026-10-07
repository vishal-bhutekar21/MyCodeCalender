import { NormalizedContest, ContestStatus } from '../types';

const CODECHEF_API_URL =
  'https://www.codechef.com/api/list/contests/all?sort_by=START&sorting_order=asc&offset=0&mode=all';

interface CodeChefContestItem {
  contest_code: string;
  contest_name: string;
  contest_start_date_iso: string;
  contest_end_date_iso: string;
  contest_duration: number; // in minutes
}

export async function fetchCodeChefContests(): Promise<NormalizedContest[]> {
  try {
    const response = await fetch(CODECHEF_API_URL, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        Accept: 'application/json',
      },
    });

    if (!response.ok) {
      console.warn(`[CodeChef] HTTP error: ${response.status}`);
      return [];
    }

    const data = (await response.json()) as {
      status?: string;
      future_contests?: CodeChefContestItem[];
      present_contests?: CodeChefContestItem[];
    };

    const now = Date.now();
    const nowIso = new Date().toISOString();
    const list: NormalizedContest[] = [];

    const processItems = (items: CodeChefContestItem[] = [], defaultStatus: ContestStatus) => {
      for (const item of items) {
        try {
          const startTimeMs = new Date(item.contest_start_date_iso).getTime();
          const endTimeMs = new Date(item.contest_end_date_iso).getTime();
          const durationSec = Math.max(0, Math.floor((endTimeMs - startTimeMs) / 1000));

          let status = defaultStatus;
          if (now >= startTimeMs && now < endTimeMs) {
            status = 'LIVE';
          } else if (now < startTimeMs) {
            status = 'UPCOMING';
          } else {
            status = 'ENDED';
          }

          list.push({
            id: `codechef_${item.contest_code}`,
            providerContestId: item.contest_code,
            platform: 'CODECHEF',
            name: item.contest_name,
            officialUrl: `https://www.codechef.com/${item.contest_code}`,
            registrationUrl: `https://www.codechef.com/${item.contest_code}`,
            startTimeUtc: new Date(startTimeMs).toISOString(),
            endTimeUtc: new Date(endTimeMs).toISOString(),
            durationSeconds: durationSec,
            contestType: 'Starters / Cook-Off',
            ratingType: 'Division Rating',
            status,
            lastFetchedAt: nowIso,
          });
        } catch {
          // ignore item parsing error
        }
      }
    };

    processItems(data.present_contests, 'LIVE');
    processItems(data.future_contests, 'UPCOMING');

    return list;
  } catch (err) {
    console.error('[CodeChef] Fetch failed:', err);
    return [];
  }
}
