# ⚡ CodeCalendar Super Admin CMS & CRM

> **Live Production Subdomain**: [https://codecalendar.vishalbhutekar.me](https://codecalendar.vishalbhutekar.me)  
> **Production Alias**: [https://admin.vishalbhutekar.me](https://admin.vishalbhutekar.me)  
> **Direct Cloudflare Pages URL**: [https://codecalendar-admin.pages.dev](https://codecalendar-admin.pages.dev)

A modern, high-performance Super Administrator CMS & CRM dashboard engineered with **React 18**, **TypeScript**, **Tailwind CSS**, and **Vite**, hosted on **Cloudflare Pages** and connected directly to the custom domain `vishalbhutekar.me`.

---

## 🌟 Live Deployment & Access

| Environment | URL | Status | Network |
|---|---|:---:|---|
| **Production Primary** | [https://codecalendar.vishalbhutekar.me](https://codecalendar.vishalbhutekar.me) | 🟢 Live | Cloudflare Global Edge |
| **Production Alias** | [https://admin.vishalbhutekar.me](https://admin.vishalbhutekar.me) | 🟢 Live | Cloudflare Global Edge |
| **Cloudflare Pages** | [https://codecalendar-admin.pages.dev](https://codecalendar-admin.pages.dev) | 🟢 Live | Cloudflare CDN |
| **Local Development** | `http://localhost:5173/` | 🟢 Active | Localhost / LAN |

---

## 🚀 Key Modules & Capabilities

1. **Dashboard Overview (`/`)**:
   - Real-time ecosystem statistics (Active Users, Curated Resources, Active In-App Broadcasts, Custom Contests).
   - Quick navigation action cards and live platform status indicator.

2. **Featured Materials & Resources CMS (`/featured-materials`)**:
   - Manage articles, cheat sheets, curated GitHub repos, and video masterclasses delivered to mobile clients.
   - Filter by category (`DSA Sheets`, `AI / ML`, `Roadmaps`, `Interviews`).

3. **In-App Broadcasts & Alerts (`/broadcasts`)**:
   - Publish real-time global notifications, server notices, or major contest countdowns directly to the Android app.
   - Severity tags: `INFO`, `IMPORTANT`, `URGENT`.

4. **Custom Contests & Hackathons (`/custom-contests`)**:
   - Add college, company, or community hackathons and contests.
   - Automatically synchronizes with Android client radar feeds.

5. **User Accounts Directory (`/users`)**:
   - Inspect registered user accounts, connected handles (LeetCode, Codeforces, GitHub), and active streaks.

6. **Account Deletion Compliance (`/deletions`)**:
   - Google Play Data Safety compliant deletion request processing and audit trails.

---

## 🔐 Authentication & Access Control

- **Google OAuth 2.0**: Integrated with Firebase Authentication.
- **Whitelist Enforcement**: Access restricted strictly to Super Admin emails:
  - `vishalbhutekar33772@gmail.com`
  - `vishal.bhutekar1@gmail.com`
  - `admin@mycodecalendar.app`
- **Instant Admin Access (Dev/Local)**: Dedicated 1-click admin authentication for seamless local testing and preview access.

---

## 🛠️ Local Development & Build

```bash
# Install dependencies
npm install

# Start local development server (port 5173)
npm run dev

# Compile production bundle
npm run build

# Deploy directly to Cloudflare Pages
npx wrangler pages deploy dist --project-name codecalendar-admin --branch main
```
