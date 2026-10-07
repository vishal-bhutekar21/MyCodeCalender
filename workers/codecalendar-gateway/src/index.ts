import { Env, NormalizedContest, Platform } from './types';
import { fetchLeetCodeContests } from './aggregators/leetcode';
import { fetchCodeforcesContests } from './aggregators/codeforces';
import { fetchCodeChefContests } from './aggregators/codechef';
import { fetchAtCoderContests } from './aggregators/atcoder';
import { fetchUserStatsProxy } from './services/statsProxy';

const CACHE_KEY_CONTESTS = 'contests:all:v1';

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, User-Agent',
  'Content-Type': 'application/json; charset=utf-8',
};

/**
 * Aggregates all upstream platforms in parallel with full error isolation.
 */
async function aggregateAllContests(): Promise<NormalizedContest[]> {
  const [leetcodeRes, cfRes, codechefRes, atcoderRes] = await Promise.allSettled([
    fetchLeetCodeContests(),
    fetchCodeforcesContests(),
    fetchCodeChefContests(),
    fetchAtCoderContests(),
  ]);

  const all: NormalizedContest[] = [];

  if (leetcodeRes.status === 'fulfilled') all.push(...leetcodeRes.value);
  if (cfRes.status === 'fulfilled') all.push(...cfRes.value);
  if (codechefRes.status === 'fulfilled') all.push(...codechefRes.value);
  if (atcoderRes.status === 'fulfilled') all.push(...atcoderRes.value);

  // Sort ascending by start time
  all.sort((a, b) => new Date(a.startTimeUtc).getTime() - new Date(b.startTimeUtc).getTime());

  return all;
}

export default {
  /**
   * Cron Trigger handler (fires every 10 minutes)
   */
  async scheduled(event: ScheduledEvent, env: Env, ctx: ExecutionContext): Promise<void> {
    console.log(`[Cron] Warming cache for CodeCalendar contests at ${new Date(event.scheduledTime).toISOString()}`);
    const contests = await aggregateAllContests();

    if (contests.length > 0 && env.CONTEST_CACHE) {
      // Store in KV with 1-hour TTL
      await env.CONTEST_CACHE.put(CACHE_KEY_CONTESTS, JSON.stringify(contests), {
        expirationTtl: 3600,
      });
      console.log(`[Cron] Stored ${contests.length} normalized contests in KV.`);
    }
  },

  /**
   * HTTP Request handler
   */
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    // Handle preflight OPTIONS
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: CORS_HEADERS });
    }

    if (request.method !== 'GET') {
      return new Response(JSON.stringify({ error: 'Method not allowed' }), {
        status: 405,
        headers: CORS_HEADERS,
      });
    }

    // Health check
    if (url.pathname === '/' || url.pathname === '/health' || url.pathname === '/v1/health') {
      return new Response(
        JSON.stringify({
          status: 'healthy',
          service: env.APP_NAME || 'CodeCalendar Gateway',
          version: env.API_VERSION || 'v1',
          timestamp: new Date().toISOString(),
          environment: env.ENVIRONMENT || 'production',
        }),
        {
          status: 200,
          headers: CORS_HEADERS,
        }
      );
    }

    // GET /v1/contests
    if (url.pathname === '/v1/contests' || url.pathname === '/contests') {
      let cachedData: NormalizedContest[] | null = null;

      if (env.CONTEST_CACHE) {
        try {
          cachedData = await env.CONTEST_CACHE.get(CACHE_KEY_CONTESTS, 'json');
        } catch (err) {
          console.warn('[KV] Read error:', err);
        }
      }

      if (cachedData && cachedData.length > 0) {
        return new Response(
          JSON.stringify({
            status: 'success',
            count: cachedData.length,
            cached: true,
            data: cachedData,
          }),
          {
            status: 200,
            headers: {
              ...CORS_HEADERS,
              'Cache-Control': 'public, max-age=300, stale-while-revalidate=3600',
              'X-Cache-Status': 'HIT',
            },
          }
        );
      }

      // Cache miss: aggregate immediately and populate KV
      const freshContests = await aggregateAllContests();

      if (env.CONTEST_CACHE && freshContests.length > 0) {
        ctx.waitUntil(
          env.CONTEST_CACHE.put(CACHE_KEY_CONTESTS, JSON.stringify(freshContests), {
            expirationTtl: 3600,
          })
        );
      }

      return new Response(
        JSON.stringify({
          status: 'success',
          count: freshContests.length,
          cached: false,
          data: freshContests,
        }),
        {
          status: 200,
          headers: {
            ...CORS_HEADERS,
            'Cache-Control': 'public, max-age=300, stale-while-revalidate=3600',
            'X-Cache-Status': 'MISS',
          },
        }
      );
    }

    // GET /v1/user/stats/:platform/:handle
    const statsMatch = url.pathname.match(/^\/v1\/user\/stats\/([^/]+)\/([^/]+)$/);
    if (statsMatch) {
      const platformParam = statsMatch[1].toUpperCase() as Platform;
      const handleParam = decodeURIComponent(statsMatch[2]);

      const validPlatforms: Platform[] = ['GITHUB', 'CODEFORCES', 'LEETCODE'];
      if (!validPlatforms.includes(platformParam)) {
        return new Response(
          JSON.stringify({ error: `Unsupported platform: ${platformParam}. Supported: ${validPlatforms.join(', ')}` }),
          { status: 400, headers: CORS_HEADERS }
        );
      }

      const stats = await fetchUserStatsProxy(platformParam, handleParam, env);
      if (!stats) {
        return new Response(
          JSON.stringify({ error: `User stats not found for ${platformParam} handle: ${handleParam}` }),
          { status: 404, headers: CORS_HEADERS }
        );
      }

      return new Response(
        JSON.stringify({
          status: 'success',
          data: stats,
        }),
        {
          status: 200,
          headers: {
            ...CORS_HEADERS,
            'Cache-Control': 'public, max-age=1800, stale-while-revalidate=3600',
          },
        }
      );
    }

    // 404
    return new Response(JSON.stringify({ error: 'Not found', path: url.pathname }), {
      status: 404,
      headers: CORS_HEADERS,
    });
  },
};
