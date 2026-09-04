import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = r"c:\Users\Vishal.DESKTOP-VI3GMOT\PROJECTS\portfolio and app\MyCodeCalender\store_assets"
os.makedirs(OUTPUT_DIR, exist_ok=True)

ICON_PATH = r"c:\Users\Vishal.DESKTOP-VI3GMOT\PROJECTS\portfolio and app\MyCodeCalender\app\src\main\ic_launcher-playstore.png"
base_icon = Image.open(ICON_PATH).convert("RGBA")

def font(size, bold=False, semi=False):
    if bold:
        name = "segoeuib.ttf"
    elif semi:
        name = "segoeuisb.ttf"
    else:
        name = "segoeui.ttf"
    try:
        return ImageFont.truetype(name, size)
    except:
        return ImageFont.load_default()

def draw_phone_frame(draw, x1, y1, x2, y2, is_dark=True):
    # Outer body shadow
    draw.rounded_rectangle([x1-6, y1-6, x2+6, y2+6], radius=48, fill=(0, 0, 0, 140))
    # Outer titanium bezel
    draw.rounded_rectangle([x1-3, y1-3, x2+3, y2+3], radius=45, fill=(45, 55, 72, 255))
    # Screen body
    bg_col = (11, 15, 25, 255) if is_dark else (248, 250, 252, 255)
    draw.rounded_rectangle([x1, y1, x2, y2], radius=42, fill=bg_col)
    
    # Status bar
    f_stat = font(18, semi=True)
    draw.text((x1 + 38, y1 + 22), "09:41", fill=(255, 255, 255, 230), font=f_stat)
    # Dynamic island pill
    mid_x = (x1 + x2) // 2
    draw.rounded_rectangle([mid_x - 65, y1 + 18, mid_x + 65, y1 + 44], radius=13, fill=(0, 0, 0, 255))
    draw.ellipse([mid_x + 35, y1 + 26, mid_x + 47, y1 + 38], fill=(15, 23, 42, 255)) # Camera lens
    # 5G & Battery icon representations
    draw.text((x2 - 110, y1 + 20), "5G  📶 🔋", fill=(255, 255, 255, 220), font=font(16))

# ── 1. FEATURE GRAPHIC (1024 x 500) ──────────────────────────────────────────
def build_feature_graphic():
    w, h = 1024, 500
    img = Image.new("RGBA", (w, h), (8, 12, 20, 255))
    draw = ImageDraw.Draw(img)

    # Ambient radial glows
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([(-120, -100), (450, 450)], fill=(79, 70, 229, 60))
    g_draw.ellipse([(600, 100), (1150, 600)], fill=(255, 122, 0, 45))
    g_draw.ellipse([(350, 250), (750, 650)], fill=(124, 58, 237, 35))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    # Left: App Icon in 3D Card
    icon_w = 170
    icon_res = base_icon.resize((icon_w, icon_w), Image.Resampling.LANCZOS)
    mask = Image.new("L", (icon_w, icon_w), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, icon_w, icon_w], radius=38, fill=255)
    
    # Outer icon frame card
    draw.rounded_rectangle([68, 148, 262, 342], radius=44, fill=(0, 0, 0, 160))
    draw.rounded_rectangle([70, 150, 260, 340], radius=42, fill=(20, 26, 40, 255), outline=(99, 102, 241, 160), width=2)
    img.paste(icon_res, (80, 160), mask)

    # Right Content Area
    x_off = 295
    # Category Tag
    draw.rounded_rectangle([x_off, 100, x_off + 260, 136], radius=9, fill=(30, 41, 59, 230), outline=(255, 122, 0, 200), width=1)
    draw.text((x_off + 16, 107), "⚡ ALL-IN-ONE CONTEST HUB", fill=(255, 154, 60, 255), font=font(15, bold=True))

    # Main Title
    draw.text((x_off, 148), "MyCodeCalendar", fill=(255, 255, 255, 255), font=font(48, bold=True))
    # Subtitle
    draw.text((x_off, 218), "Live Coding Contests, Smart Push Alerts & DSA Sheets", fill=(203, 213, 225, 255), font=font(21, semi=True))

    # Platform Chips Row
    platforms = [
        ("Codeforces", (239, 68, 68)),
        ("LeetCode", (245, 158, 11)),
        ("CodeChef", (139, 92, 246)),
        ("AtCoder", (16, 185, 129)),
        ("HackerRank", (59, 130, 246)),
    ]
    cur_x = x_off
    chip_y = 270
    for name, col in platforms:
        c_w = len(name) * 11 + 36
        draw.rounded_rectangle([cur_x, chip_y, cur_x + c_w, chip_y + 36], radius=18, fill=(24, 33, 49, 240), outline=(51, 65, 85, 220), width=1)
        draw.ellipse([cur_x + 12, chip_y + 13, cur_x + 22, chip_y + 23], fill=col)
        draw.text((cur_x + 30, chip_y + 7), name, fill=(241, 245, 249, 255), font=font(16, bold=True))
        cur_x += c_w + 10

    # Bottom Highlights Bar
    highlights = [
        "✓ Real-time countdowns",
        "✓ Push notifications (15m & 1h)",
        "✓ Calendar sync",
        "✓ Striver & Blind 75"
    ]
    draw.text((x_off, 345), "   •   ".join(highlights), fill=(148, 163, 184, 255), font=font(15, semi=True))

    # Save
    out = os.path.join(OUTPUT_DIR, "feature_graphic_real.png")
    img.convert("RGB").save(out, "PNG", quality=100)
    print("Saved:", out)

# ── 2. SCREENSHOT 1: LIVE CONTEST SCHEDULE ──────────────────────────────────
def build_screenshot_1():
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), (8, 12, 22, 255))
    draw = ImageDraw.Draw(img)

    # Header section
    draw.text((70, 95), "UPCOMING CONTESTS", fill=(255, 122, 0, 255), font=font(22, bold=True))
    draw.text((70, 135), "Never Miss a Coding Round", fill=(255, 255, 255, 255), font=font(52, bold=True))
    draw.text((70, 210), "Real-time countdowns & direct arena launch for all platforms", fill=(148, 163, 184, 255), font=font(24))

    # Phone Frame
    px1, py1, px2, py2 = 70, 280, 1010, 1860
    draw_phone_frame(draw, px1, py1, px2, py2)

    # In-App Top Bar
    draw.text((px1 + 35, py1 + 65), "MyCodeCalendar", fill=(255, 255, 255, 255), font=font(28, bold=True))
    draw.rounded_rectangle([px2 - 130, py1 + 60, px2 - 35, py1 + 98], radius=12, fill=(255, 122, 0, 40), outline=(255, 122, 0, 180), width=1)
    draw.text((px2 - 112, py1 + 68), "⚡ LIVE", fill=(255, 122, 0, 255), font=font(18, bold=True))

    # In-App Tabs: All, Codeforces, LeetCode, CodeChef
    tabs = ["All Contests", "Codeforces", "LeetCode", "CodeChef"]
    tx = px1 + 35
    ty = py1 + 125
    for i, t in enumerate(tabs):
        active = (i == 0)
        tw = len(t) * 11 + 28
        bg = (255, 122, 0, 255) if active else (24, 33, 49, 255)
        txt_col = (255, 255, 255, 255) if active else (148, 163, 184, 255)
        draw.rounded_rectangle([tx, ty, tx + tw, ty + 38], radius=19, fill=bg)
        draw.text((tx + 14, ty + 7), t, fill=txt_col, font=font(16, bold=True))
        tx += tw + 10

    # Contest Cards
    card_y = py1 + 185
    contests = [
        ("Codeforces Round 998 (Div. 3)", "Codeforces", (239, 68, 68), "In 1h 24m", "Today, 08:05 PM", "2h 15m"),
        ("LeetCode Weekly Contest 438", "LeetCode", (245, 158, 11), "Tomorrow", "Sunday, 08:00 AM", "1h 30m"),
        ("CodeChef Starters 174 (Div. 2)", "CodeChef", (139, 92, 246), "In 2 days", "Wednesday, 08:00 PM", "2h 00m"),
        ("AtCoder Beginner Contest 394", "AtCoder", (16, 185, 129), "In 3 days", "Saturday, 05:30 PM", "1h 40m"),
        ("HackerEarth Monthly Circuit", "HackerEarth", (236, 72, 153), "Active", "Ongoing now", "5 days left"),
    ]
    card_w = px2 - px1 - 70
    for title, plat, col, badge_txt, time_str, duration in contests:
        # Card body
        draw.rounded_rectangle([px1 + 35, card_y, px1 + 35 + card_w, card_y + 230], radius=22, fill=(20, 27, 42, 255), outline=(36, 48, 71, 255), width=1)
        # Platform pill
        draw.rounded_rectangle([px1 + 55, card_y + 20, px1 + 55 + len(plat)*12 + 32, card_y + 56], radius=9, fill=(11, 15, 25, 255))
        draw.ellipse([px1 + 67, card_y + 33, px1 + 77, card_y + 43], fill=col)
        draw.text((px1 + 86, card_y + 26), plat, fill=(241, 245, 249, 255), font=font(17, bold=True))

        # Countdown badge
        draw.rounded_rectangle([px1 + 35 + card_w - 180, card_y + 20, px1 + 35 + card_w - 20, card_y + 56], radius=9, fill=(79, 70, 229, 50), outline=(99, 102, 241, 160))
        draw.text((px1 + 35 + card_w - 165, card_y + 27), f"⏱ {badge_txt}", fill=(165, 180, 252, 255), font=font(16, bold=True))

        # Title
        draw.text((px1 + 55, card_y + 75), title, fill=(255, 255, 255, 255), font=font(24, bold=True))
        # Schedule Info
        draw.text((px1 + 55, card_y + 125), f"📅 {time_str}   •   ⏳ {duration}", fill=(148, 163, 184, 255), font=font(18, semi=True))

        # Bottom actions
        draw.rounded_rectangle([px1 + 55, card_y + 168, px1 + 220, card_y + 208], radius=10, fill=(255, 122, 0, 255))
        draw.text((px1 + 82, card_y + 176), "Enter Arena ➔", fill=(255, 255, 255, 255), font=font(16, bold=True))

        draw.rounded_rectangle([px1 + 35 + card_w - 160, card_y + 168, px1 + 35 + card_w - 20, card_y + 208], radius=10, fill=(30, 41, 59, 255), outline=(51, 65, 85, 255))
        draw.text((px1 + 35 + card_w - 142, card_y + 176), "🔔 Remind Me", fill=(226, 232, 240, 255), font=font(16, semi=True))

        card_y += 252

    out = os.path.join(OUTPUT_DIR, "screenshot_1_contests.png")
    img.convert("RGB").save(out, "PNG", quality=100)
    print("Saved:", out)

# ── 3. SCREENSHOT 2: SMART PUSH ALERTS ──────────────────────────────────────
def build_screenshot_2():
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), (8, 12, 22, 255))
    draw = ImageDraw.Draw(img)

    draw.text((70, 95), "PUSH NOTIFICATIONS", fill=(99, 102, 241, 255), font=font(22, bold=True))
    draw.text((70, 135), "Instant Contest Reminders", fill=(255, 255, 255, 255), font=font(52, bold=True))
    draw.text((70, 210), "Get notified 15 mins and 1 hour before rounds begin", fill=(148, 163, 184, 255), font=font(24))

    px1, py1, px2, py2 = 70, 280, 1010, 1860
    draw_phone_frame(draw, px1, py1, px2, py2)

    # In-App Content: Notification settings + Real Notifications overlay
    ny = py1 + 80
    draw.text((px1 + 35, ny), "Notification Center", fill=(255, 255, 255, 255), font=font(28, bold=True))
    draw.text((px1 + 35, ny + 38), "Spam-free alerts customized to your favorite platforms", fill=(148, 163, 184, 255), font=font(17))

    # Real System Heads-Up Notification Banner (Floating Android style)
    banner_y = ny + 95
    banner_w = px2 - px1 - 70
    draw.rounded_rectangle([px1 + 35, banner_y, px1 + 35 + banner_w, banner_y + 190], radius=24, fill=(30, 41, 59, 250), outline=(99, 102, 241, 255), width=2)
    # App icon in notification
    draw.rounded_rectangle([px1 + 55, banner_y + 24, px1 + 105, banner_y + 74], radius=12, fill=(255, 122, 0, 255))
    draw.text((px1 + 68, banner_y + 34), "⚡", fill=(255, 255, 255, 255), font=font(24))
    draw.text((px1 + 120, banner_y + 28), "MyCodeCalendar  •  Just now", fill=(148, 163, 184, 255), font=font(16, semi=True))
    draw.text((px1 + 120, banner_y + 55), "Codeforces Round 998 starts in 15 mins!", fill=(255, 255, 255, 255), font=font(22, bold=True))
    draw.text((px1 + 55, banner_y + 100), "Click to enter the contest arena now and review problem statements.", fill=(203, 213, 225, 255), font=font(18))
    # Quick action button in notification
    draw.rounded_rectangle([px1 + 55, banner_y + 138, px1 + 220, banner_y + 172], radius=8, fill=(79, 70, 229, 255))
    draw.text((px1 + 75, banner_y + 144), "Open Arena ➔", fill=(255, 255, 255, 255), font=font(15, bold=True))

    # Preference Toggles
    ty = banner_y + 225
    toggles = [
        ("Contest 15-min Heads-Up", "Alarm sound and heads-up banner before rounds", True, (255, 122, 0)),
        ("Contest 1-Hour Early Reminder", "Prepare editorial templates and environment", True, (99, 102, 241)),
        ("Daily Problem of the Day", "Morning reminder for LeetCode daily question", True, (245, 158, 11)),
        ("Calendar Auto-Sync", "Add registered rounds directly to device calendar", True, (16, 185, 129)),
        ("Hackathon Announcements", "Major developer contests and global codesprints", False, (148, 163, 184)),
    ]
    for t_title, t_desc, is_on, col in toggles:
        draw.rounded_rectangle([px1 + 35, ty, px1 + 35 + banner_w, ty + 130], radius=18, fill=(20, 27, 42, 255), outline=(36, 48, 71, 255), width=1)
        draw.text((px1 + 60, ty + 24), t_title, fill=(255, 255, 255, 255), font=font(22, bold=True))
        draw.text((px1 + 60, ty + 68), t_desc, fill=(148, 163, 184, 255), font=font(17))
        # Switch graphic
        sw_x = px1 + 35 + banner_w - 90
        sw_y = ty + 42
        draw.rounded_rectangle([sw_x, sw_y, sw_x + 60, sw_y + 34], radius=17, fill=col if is_on else (45, 55, 72, 255))
        thumb_x = sw_x + 32 if is_on else sw_x + 4
        draw.ellipse([thumb_x, sw_y + 4, thumb_x + 26, sw_y + 30], fill=(255, 255, 255, 255))
        ty += 150

    out = os.path.join(OUTPUT_DIR, "screenshot_2_notifications.png")
    img.convert("RGB").save(out, "PNG", quality=100)
    print("Saved:", out)

# ── 4. SCREENSHOT 3: DSA SHEETS & ROADMAPS ──────────────────────────────────
def build_screenshot_3():
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), (8, 12, 22, 255))
    draw = ImageDraw.Draw(img)

    draw.text((70, 95), "INTERVIEW PREPARATION", fill=(16, 185, 129, 255), font=font(22, bold=True))
    draw.text((70, 135), "Curated DSA Problem Sheets", fill=(255, 255, 255, 255), font=font(52, bold=True))
    draw.text((70, 210), "Track Striver's SDE Sheet, Blind 75 and NeetCode 150 roadmaps", fill=(148, 163, 184, 255), font=font(24))

    px1, py1, px2, py2 = 70, 280, 1010, 1860
    draw_phone_frame(draw, px1, py1, px2, py2)

    dy = py1 + 75
    draw.text((px1 + 35, dy), "DSA Mastery Trackers", fill=(255, 255, 255, 255), font=font(28, bold=True))
    draw.text((px1 + 35, dy + 38), "Organized topic-wise roadmaps for top tech companies", fill=(148, 163, 184, 255), font=font(17))

    card_w = px2 - px1 - 70
    cy = dy + 90
    sheets = [
        ("Striver's SDE Sheet", "180 Core Algorithmic Problems", "145 / 180 Solved", 0.80, (16, 185, 129)),
        ("Blind 75 Curated List", "Most Frequent FAANG Questions", "68 / 75 Solved", 0.90, (245, 158, 11)),
        ("NeetCode 150 Patterns", "Complete LeetCode Pattern Roadmap", "92 / 150 Solved", 0.61, (99, 102, 241)),
        ("Dynamic Programming Sheet", "From Basic Recursion to Bitmask DP", "42 / 60 Solved", 0.70, (236, 72, 153)),
    ]
    for title, sub, stats, progress, col in sheets:
        draw.rounded_rectangle([px1 + 35, cy, px1 + 35 + card_w, cy + 185], radius=20, fill=(20, 27, 42, 255), outline=(36, 48, 71, 255), width=1)
        draw.text((px1 + 55, cy + 22), title, fill=(255, 255, 255, 255), font=font(24, bold=True))
        draw.text((px1 + 55, cy + 62), sub, fill=(148, 163, 184, 255), font=font(17))
        draw.text((px1 + 35 + card_w - 200, cy + 62), stats, fill=(241, 245, 249, 255), font=font(18, bold=True))

        # Progress bar
        bar_w = card_w - 40
        draw.rounded_rectangle([px1 + 55, cy + 105, px1 + 55 + bar_w, cy + 125], radius=10, fill=(11, 15, 25, 255))
        draw.rounded_rectangle([px1 + 55, cy + 105, px1 + 55 + int(bar_w * progress), cy + 125], radius=10, fill=col)

        draw.text((px1 + 55, cy + 140), "✓ Tracked with optimal time & space complexity", fill=(100, 116, 139, 255), font=font(15, semi=True))
        cy += 205

    # Interactive Problem Item Preview
    draw.text((px1 + 35, cy + 10), "Recent Practice Problems", fill=(255, 255, 255, 255), font=font(22, bold=True))
    problems = [
        ("Longest Substring Without Repeating Characters", "Sliding Window", "Medium", (245, 158, 11)),
        ("Trapping Rain Water", "Two Pointers", "Hard", (239, 68, 68)),
        ("Invert Binary Tree", "Trees", "Easy", (16, 185, 129)),
    ]
    py = cy + 50
    for p_title, p_topic, p_diff, diff_col in problems:
        draw.rounded_rectangle([px1 + 35, py, px1 + 35 + card_w, py + 85], radius=14, fill=(24, 33, 49, 255), outline=(42, 55, 80, 255))
        draw.text((px1 + 55, py + 16), p_title[:42], fill=(255, 255, 255, 255), font=font(18, bold=True))
        draw.text((px1 + 55, py + 48), f"Topic: {p_topic}", fill=(148, 163, 184, 255), font=font(15))
        draw.rounded_rectangle([px1 + 35 + card_w - 110, py + 26, px1 + 35 + card_w - 20, py + 58], radius=8, fill=diff_col)
        draw.text((px1 + 35 + card_w - 95, py + 31), p_diff, fill=(255, 255, 255, 255), font=font(15, bold=True))
        py += 100

    out = os.path.join(OUTPUT_DIR, "screenshot_3_dsa.png")
    img.convert("RGB").save(out, "PNG", quality=100)
    print("Saved:", out)

# ── 5. SCREENSHOT 4: MINIMALIST SETTINGS ──────────────────────────────────────
def build_screenshot_4():
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), (8, 12, 22, 255))
    draw = ImageDraw.Draw(img)

    draw.text((70, 95), "STREAMLINED EXPERIENCE", fill=(255, 122, 0, 255), font=font(22, bold=True))
    draw.text((70, 135), "Clean Grouped Settings", fill=(255, 255, 255, 255), font=font(52, bold=True))
    draw.text((70, 210), "Minimalist preferences, creator links, and cloud sync", fill=(148, 163, 184, 255), font=font(24))

    px1, py1, px2, py2 = 70, 280, 1010, 1860
    draw_phone_frame(draw, px1, py1, px2, py2)

    sy = py1 + 75
    draw.text((px1 + 35, sy), "Settings", fill=(255, 255, 255, 255), font=font(34, bold=True))
    draw.text((px1 + 35, sy + 44), "Preferences & Profile", fill=(148, 163, 184, 255), font=font(17))

    card_w = px2 - px1 - 70
    # Profile Card
    p_y = sy + 90
    draw.rounded_rectangle([px1 + 35, p_y, px1 + 35 + card_w, p_y + 195], radius=22, fill=(20, 27, 42, 255), outline=(255, 122, 0, 160), width=1)
    # Avatar
    draw.ellipse([px1 + 55, p_y + 24, px1 + 115, p_y + 84], fill=(255, 122, 0, 255))
    draw.text((px1 + 74, p_y + 36), "V", fill=(255, 255, 255, 255), font=font(32, bold=True))
    draw.text((px1 + 130, p_y + 28), "Vishal Bhutekar  ✓", fill=(255, 255, 255, 255), font=font(22, bold=True))
    draw.text((px1 + 130, p_y + 58), "Google Account Sync Active", fill=(56, 189, 248, 255), font=font(16, semi=True))

    # 3-stat bar
    draw.rounded_rectangle([px1 + 55, p_y + 105, px1 + 35 + card_w - 20, p_y + 165], radius=12, fill=(11, 15, 25, 255))
    draw.text((px1 + 80, p_y + 124), "🔥 14d Streak", fill=(255, 122, 0, 255), font=font(17, bold=True))
    draw.text((px1 + 320, p_y + 124), "⚡ 4 Linked Handles", fill=(99, 102, 241, 255), font=font(17, bold=True))
    draw.text((px1 + 630, p_y + 124), "☁️ Cloud Backed", fill=(16, 185, 129, 255), font=font(17, bold=True))

    # Group 1: Preferences
    g1_y = p_y + 230
    draw.text((px1 + 45, g1_y), "PREFERENCES", fill=(148, 163, 184, 255), font=font(16, bold=True))
    draw.rounded_rectangle([px1 + 35, g1_y + 28, px1 + 35 + card_w, g1_y + 280], radius=20, fill=(20, 27, 42, 255), outline=(36, 48, 71, 255))

    p_items = [
        ("App Appearance", "Dark (OLED Obsidian)", "🌙"),
        ("Contest Alerts (15m)", "Instant notification before rounds", "🔔"),
        ("Daily Problem Reminder", "Morning LeetCode problem alert", "💡"),
        ("Calendar Auto-Sync", "Export to phone calendar", "📅"),
    ]
    iy = g1_y + 35
    for title, sub, icon_s in p_items:
        draw.text((px1 + 60, iy + 10), icon_s, fill=(255, 122, 0, 255), font=font(20))
        draw.text((px1 + 105, iy + 8), title, fill=(255, 255, 255, 255), font=font(20, bold=True))
        draw.text((px1 + 105, iy + 34), sub, fill=(148, 163, 184, 255), font=font(15))
        draw.text((px1 + 35 + card_w - 45, iy + 18), "➔", fill=(100, 116, 139, 255), font=font(18))
        iy += 60

    # Group 2: Creator & Support
    g2_y = g1_y + 315
    draw.text((px1 + 45, g2_y), "CREATOR & COMMUNITY", fill=(148, 163, 184, 255), font=font(16, bold=True))
    draw.rounded_rectangle([px1 + 35, g2_y + 28, px1 + 35 + card_w, g2_y + 220], radius=20, fill=(20, 27, 42, 255), outline=(36, 48, 71, 255))
    draw.text((px1 + 60, g2_y + 48), "Meet Vishal Bhutekar (Verified Engineer)", fill=(255, 255, 255, 255), font=font(20, bold=True))
    draw.text((px1 + 60, g2_y + 78), "Android & Full-Stack developer creating modern developer tools", fill=(148, 163, 184, 255), font=font(16))

    # Social Pills
    s_x = px1 + 60
    pills = [("Instagram", (225, 48, 108)), ("Play Store", (16, 185, 129)), ("Portfolio", (255, 122, 0)), ("JustU", (99, 102, 241))]
    for p_name, p_col in pills:
        draw.rounded_rectangle([s_x, g2_y + 125, s_x + 160, g2_y + 175], radius=10, fill=(11, 15, 25, 255), outline=p_col)
        draw.text((s_x + 35, g2_y + 138), p_name, fill=p_col, font=font(17, bold=True))
        s_x += 180

    out = os.path.join(OUTPUT_DIR, "screenshot_4_settings.png")
    img.convert("RGB").save(out, "PNG", quality=100)
    print("Saved:", out)

if __name__ == "__main__":
    build_feature_graphic()
    build_screenshot_1()
    build_screenshot_2()
    build_screenshot_3()
    build_screenshot_4()
    print("All real best store graphics generated!")
