import React from "react";
import Head from "next/head";

export const metadata = {
  title: "Privacy Policy — Code Calendar",
  description:
    "Official Privacy Policy for Code Calendar Android Application (com.vishal.codecalendar). Transparent disclosure of data handling, permissions, and security.",
};

export default function PrivacyPolicyPage() {
  const lastUpdated = "August 14, 2026";
  const appName = "Code Calendar";
  const packageName = "com.vishal.codecalendar";
  const developerName = "Vishal Bhutekar";
  const contactEmail = "vishal.bhutekar1@gmail.com";
  const portfolioUrl = "https://vishalbhutekar.netlify.app/";
  const playDevUrl = "https://play.google.com/store/apps/dev?id=8656025420118431472";

  return (
    <div className="min-h-screen bg-[#090D16] text-[#E2E8F0] font-sans antialiased selection:bg-[#6366F1] selection:text-white">
      {/* ── Ambient Radial Glows ── */}
      <div className="fixed inset-0 pointer-events-none overflow-hidden z-0">
        <div className="absolute -top-40 left-1/2 -translate-x-1/2 w-[700px] h-[400px] bg-gradient-to-b from-[#4F46E5]/20 to-transparent blur-3xl rounded-full opacity-60" />
        <div className="absolute top-96 -left-32 w-80 h-80 bg-[#06B6D4]/10 blur-3xl rounded-full" />
        <div className="absolute top-[800px] -right-32 w-96 h-96 bg-[#8B5CF6]/10 blur-3xl rounded-full" />
      </div>

      <div className="relative z-10 max-w-4xl mx-auto px-6 py-12 sm:py-20">
        {/* ── Top Header & Navigation ── */}
        <header className="mb-12 border-b border-slate-800/80 pb-8">
          <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-[#6366F1] to-[#06B6D4] p-0.5 shadow-lg shadow-indigo-500/20">
                <div className="w-full h-full bg-[#0F172A] rounded-[14px] flex items-center justify-center text-xl">
                  📅
                </div>
              </div>
              <div>
                <h1 className="text-2xl sm:text-3xl font-black tracking-tight text-white flex items-center gap-2">
                  {appName}
                  <span className="text-xs px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-semibold tracking-normal">
                    v1.0.0
                  </span>
                </h1>
                <p className="text-xs sm:text-sm text-slate-400 font-mono mt-0.5">
                  Package: {packageName}
                </p>
              </div>
            </div>

            <a
              href={portfolioUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 text-xs font-semibold px-4 py-2 rounded-xl bg-slate-800/60 hover:bg-slate-800 text-slate-300 border border-slate-700/60 transition-all hover:border-slate-600 shadow-sm"
            >
              <span>Developer Portfolio</span>
              <svg
                className="w-3.5 h-3.5 text-slate-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                />
              </svg>
            </a>
          </div>

          <div className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 backdrop-blur-xl">
            <h2 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              Privacy Policy & Transparency Notice
            </h2>
            <p className="text-sm text-slate-300 leading-relaxed mb-4">
              Your privacy and data ownership are core principles of {appName}. We
              believe in complete transparency: this application does not sell,
              monetize, or track your personal information. All competitive
              programming data is stored securely on your device.
            </p>
            <div className="flex flex-wrap gap-y-2 gap-x-6 text-xs text-slate-400 font-medium pt-3 border-t border-slate-800/80">
              <div>
                <span className="text-slate-500">Effective Date:</span>{" "}
                <span className="text-slate-300">{lastUpdated}</span>
              </div>
              <div>
                <span className="text-slate-500">Developer:</span>{" "}
                <span className="text-indigo-400 font-semibold">{developerName}</span>
              </div>
              <div>
                <span className="text-slate-500">Contact:</span>{" "}
                <a
                  href={`mailto:${contactEmail}`}
                  className="text-cyan-400 hover:underline"
                >
                  {contactEmail}
                </a>
              </div>
            </div>
          </div>
        </header>

        {/* ── Quick Highlights Badges ── */}
        <section className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-3.5 mb-12">
          <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 backdrop-blur-md">
            <div className="text-2xl mb-2">🔒</div>
            <div className="text-xs font-bold text-white uppercase tracking-wider">Zero Ads & Tracking</div>
            <p className="text-xs text-slate-400 mt-1">No third-party ad networks or user behavior profiling.</p>
          </div>
          <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 backdrop-blur-md">
            <div className="text-2xl mb-2">💾</div>
            <div className="text-xs font-bold text-white uppercase tracking-wider">On-Device Storage</div>
            <p className="text-xs text-slate-400 mt-1">Your handles, active streak, and offline caches stay on device.</p>
          </div>
          <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 backdrop-blur-md">
            <div className="text-2xl mb-2">📅</div>
            <div className="text-xs font-bold text-white uppercase tracking-wider">Explicit Calendar Sync</div>
            <p className="text-xs text-slate-400 mt-1">Calendar permissions are only requested when you tap sync.</p>
          </div>
          <div className="p-4 rounded-2xl bg-slate-900/40 border border-slate-800/80 backdrop-blur-md">
            <div className="text-2xl mb-2">🛡️</div>
            <div className="text-xs font-bold text-white uppercase tracking-wider">Play Store Verified</div>
            <p className="text-xs text-slate-400 mt-1">Fully compliant with Google Play Data Safety standards.</p>
          </div>
        </section>

        {/* ── Detailed Policy Content ── */}
        <main className="space-y-10 text-slate-300 text-sm leading-relaxed">
          {/* Section 1 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">01.</span> Introduction
            </h3>
            <p className="mb-3">
              {appName} (&quot;we&quot;, &quot;our&quot;, or &quot;us&quot;) is a developer productivity and competitive programming aggregator application designed and maintained by <strong>{developerName}</strong>.
            </p>
            <p>
              This Privacy Policy explains how our Android mobile application collects, uses, and protects your information when you download, install, and use {appName} (package name: <code className="text-xs bg-slate-800 px-1.5 py-0.5 rounded text-cyan-300">{packageName}</code>) from the Google Play Store.
            </p>
          </section>

          {/* Section 2 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">02.</span> Information We Collect & How We Obtain It
            </h3>
            <p className="mb-4">
              We collect minimal information necessary to deliver live competitive programming contest alerts and developer statistics:
            </p>
            <div className="space-y-3 pl-2">
              <div className="border-l-2 border-indigo-500/50 pl-4 py-1">
                <strong className="text-white">A. Public Competitive Programming Handles:</strong>
                <p className="text-slate-400 text-xs mt-0.5">
                  When you connect platforms such as LeetCode, Codeforces, CodeChef, AtCoder, or GitHub, we store your public username/handle locally to query public API endpoints. We <strong>never</strong> ask for or store passwords.
                </p>
              </div>
              <div className="border-l-2 border-cyan-500/50 pl-4 py-1">
                <strong className="text-white">B. Local App Preferences & Daily Streak:</strong>
                <p className="text-slate-400 text-xs mt-0.5">
                  App theme choices (Dark, Light, System Default), notification reminder offsets (e.g. 15 minutes prior), and your daily login streak dates are persisted in your device&apos;s local storage.
                </p>
              </div>
              <div className="border-l-2 border-emerald-500/50 pl-4 py-1">
                <strong className="text-white">C. Non-Personal Technical Information:</strong>
                <p className="text-slate-400 text-xs mt-0.5">
                  Standard network connectivity status is checked locally on your device to seamlessly switch between live syncing and offline cached contest schedules.
                </p>
              </div>
            </div>
          </section>

          {/* Section 3 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">03.</span> Android Permissions Disclosure
            </h3>
            <p className="mb-4">
              In accordance with Google Play Developer Policy requirements, we provide an explicit disclosure of all permissions declared in the Android Manifest:
            </p>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-xs">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase tracking-wider">
                    <th className="py-2.5 px-3">Permission</th>
                    <th className="py-2.5 px-3">Type</th>
                    <th className="py-2.5 px-3">Purpose / Rationale</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  <tr>
                    <td className="py-2.5 px-3 font-mono text-cyan-400">POST_NOTIFICATIONS</td>
                    <td className="py-2.5 px-3 text-slate-400">Runtime (Android 13+)</td>
                    <td className="py-2.5 px-3 text-slate-300">Deliver 15-minute contest countdown reminders configured by the user.</td>
                  </tr>
                  <tr>
                    <td className="py-2.5 px-3 font-mono text-cyan-400">SCHEDULE_EXACT_ALARM</td>
                    <td className="py-2.5 px-3 text-slate-400">Alarm API</td>
                    <td className="py-2.5 px-3 text-slate-300">Schedule precise reminder alarms before live competitive programming contests begin.</td>
                  </tr>
                  <tr>
                    <td className="py-2.5 px-3 font-mono text-cyan-400">WRITE_CALENDAR & READ_CALENDAR</td>
                    <td className="py-2.5 px-3 text-slate-400">Optional / Runtime</td>
                    <td className="py-2.5 px-3 text-slate-300">Allow users to export tracked contests directly to their native Google / device calendar with 1 tap.</td>
                  </tr>
                  <tr>
                    <td className="py-2.5 px-3 font-mono text-cyan-400">INTERNET & ACCESS_NETWORK_STATE</td>
                    <td className="py-2.5 px-3 text-slate-400">Normal</td>
                    <td className="py-2.5 px-3 text-slate-300">Fetch up-to-date contest schedules from public competitive programming APIs.</td>
                  </tr>
                  <tr>
                    <td className="py-2.5 px-3 font-mono text-cyan-400">RECEIVE_BOOT_COMPLETED</td>
                    <td className="py-2.5 px-3 text-slate-400">Normal</td>
                    <td className="py-2.5 px-3 text-slate-300">Reschedule registered contest reminder alarms after device restart.</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          {/* Section 4 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">04.</span> Third-Party Services & Public APIs
            </h3>
            <p className="mb-3">
              To provide real-time contest feeds, ratings, and statistics, {appName} communicates directly with the following public services:
            </p>
            <ul className="list-disc list-inside space-y-1.5 text-xs text-slate-300 pl-2">
              <li><strong className="text-white">Kontests / Clist API:</strong> Public contest calendar aggregation.</li>
              <li><strong className="text-white">Codeforces Official API:</strong> Contest listings and user rating progression curves.</li>
              <li><strong className="text-white">LeetCode GraphQL API:</strong> Public profile stats, problems solved count, and contest rankings.</li>
              <li><strong className="text-white">GitHub REST API:</strong> Public repository inspection and commit contribution matrix.</li>
              <li><strong className="text-white">Google Play Services:</strong> Core Android OS platform runtime and in-app review flows.</li>
            </ul>
            <p className="text-xs text-slate-400 mt-3">
              We do not transmit user tracking IDs or unique hardware identifiers to any of these third-party endpoints.
            </p>
          </section>

          {/* Section 5 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">05.</span> Data Retention & User Control Rights
            </h3>
            <p className="mb-3">
              Because all user preferences and platform handles are stored locally on your mobile device:
            </p>
            <div className="space-y-2 text-xs text-slate-300">
              <p>
                • <strong>Instant Data Deletion:</strong> You can disconnect any platform handle at any time from the <em>Settings &gt; Connected Platforms</em> screen, or clear all app data immediately via <em>Android Settings &gt; Apps &gt; Code Calendar &gt; Clear Storage</em>.
              </p>
              <p>
                • <strong>No Cloud Retention:</strong> We do not operate secondary cloud databases retaining your personal logs or handles after you uninstall the application.
              </p>
            </div>
          </section>

          {/* Section 6 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">06.</span> Children&apos;s Privacy (COPPA Compliance)
            </h3>
            <p>
              {appName} is a competitive programming utility intended for software developers, students, and coding enthusiasts. We do not knowingly collect or solicit personal identifiable information from children under the age of 13. If you believe a child has provided us with personal data, please contact us at <a href={`mailto:${contactEmail}`} className="text-cyan-400 hover:underline">{contactEmail}</a> for immediate remediation.
            </p>
          </section>

          {/* Section 7 */}
          <section className="bg-slate-900/30 border border-slate-800/60 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-3 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">07.</span> Changes to This Privacy Policy
            </h3>
            <p>
              We may update our Privacy Policy periodically to reflect app updates or regulatory compliance adjustments. Any modifications will be posted on this page with an updated &quot;Effective Date&quot; timestamp. We encourage you to review this page periodically.
            </p>
          </section>

          {/* Section 8: Contact */}
          <section className="bg-gradient-to-r from-indigo-950/40 via-slate-900/60 to-cyan-950/40 border border-indigo-500/20 rounded-2xl p-6 sm:p-8">
            <h3 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <span className="text-indigo-400 font-mono text-sm">08.</span> Contact the Developer
            </h3>
            <p className="text-slate-300 text-xs mb-4">
              If you have questions, feedback, or privacy-related inquiries concerning {appName}, feel free to reach out directly:
            </p>
            <div className="flex flex-wrap gap-3">
              <a
                href={`mailto:${contactEmail}`}
                className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition shadow-md shadow-indigo-600/20"
              >
                <span>✉️ Email Developer</span>
              </a>
              <a
                href={portfolioUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 text-xs font-bold transition"
              >
                <span>🌐 Portfolio Website</span>
              </a>
              <a
                href={playDevUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-emerald-300 border border-slate-700 text-xs font-bold transition"
              >
                <span>★ Google Play Developer Page</span>
              </a>
            </div>
          </section>
        </main>

        {/* ── Footer ── */}
        <footer className="mt-16 pt-8 border-t border-slate-800 text-center text-xs text-slate-500">
          <p>© {new Date().getFullYear()} {appName} · Developed by {developerName}. All rights reserved.</p>
          <p className="mt-1">
            Compliant with Google Play Developer Program Policies & Data Safety Standards.
          </p>
        </footer>
      </div>
    </div>
  );
}
