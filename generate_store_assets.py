import os
from PIL import Image, ImageDraw, ImageFont

ASSETS_DIR = r"c:\Users\Vishal.DESKTOP-VI3GMOT\PROJECTS\portfolio and app\MyCodeCalender\store_assets"
os.makedirs(ASSETS_DIR, exist_ok=True)

ICON_PATH = r"c:\Users\Vishal.DESKTOP-VI3GMOT\PROJECTS\portfolio and app\MyCodeCalender\app\src\main\ic_launcher-playstore.png"
icon_img = Image.open(ICON_PATH).convert("RGBA")

def get_font(size, bold=False):
    font_names = [
        "segoeuib.ttf" if bold else "segoeui.ttf",
        "arialbd.ttf" if bold else "arial.ttf",
        "tahoma.ttf",
    ]
    for font_name in font_names:
        try:
            return ImageFont.truetype(font_name, size)
        except IOError:
            continue
    return ImageFont.load_default()

def generate_feature_graphic():
    w, h = 1024, 500
    img = Image.new("RGBA", (w, h), (13, 17, 28, 255))
    draw = ImageDraw.Draw(img)

    for y in range(h):
        r = int(11 + (22 - 11) * (y / h))
        g = int(15 + (30 - 15) * (y / h))
        b = int(32 + (58 - 32) * (y / h))
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.ellipse([(-100, -100), (400, 400)], fill=(79, 70, 229, 45))
    glow_draw.ellipse([(650, 150), (1150, 650)], fill=(124, 58, 237, 40))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    icon_resized = icon_img.resize((180, 180), Image.Resampling.LANCZOS)
    mask = Image.new("L", (180, 180), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([0, 0, 180, 180], radius=40, fill=255)
    
    draw.rounded_rectangle([78, 158, 262, 342], radius=42, fill=(0, 0, 0, 100))
    img.paste(icon_resized, (80, 160), mask)

    title_font = get_font(52, bold=True)
    sub_font = get_font(24, bold=False)
    tag_font = get_font(18, bold=True)
    badge_font = get_font(16, bold=True)

    draw.rounded_rectangle([290, 140, 580, 172], radius=8, fill=(30, 41, 59, 230), outline=(99, 102, 241, 180), width=1)
    draw.text((305, 146), "⚡ CODING CONTESTS & DSA", fill=(165, 180, 252, 255), font=badge_font)

    draw.text((290, 185), "MyCodeCalendar", fill=(255, 255, 255, 255), font=title_font)
    draw.text((290, 255), "Live Contests, Custom Alerts & DSA Preparation", fill=(203, 213, 225, 255), font=sub_font)

    platforms = ["Codeforces", "LeetCode", "CodeChef", "AtCoder", "HackerRank"]
    px = 290
    py = 310
    colors = [(239, 68, 68), (245, 158, 11), (139, 92, 246), (16, 185, 129), (59, 130, 246)]
    for idx, p in enumerate(platforms):
        p_len = len(p) * 11 + 24
        draw.rounded_rectangle([px, py, px + p_len, py + 34], radius=17, fill=(24, 32, 47, 240), outline=(51, 65, 85, 200))
        draw.ellipse([px + 10, py + 12, px + 18, py + 20], fill=colors[idx])
        draw.text((px + 26, py + 7), p, fill=(226, 232, 240, 255), font=tag_font)
        px += p_len + 12

    draw.text((290, 375), "✓ Real-time countdowns   ✓ Push notifications   ✓ Offline calendar sync", fill=(148, 163, 184, 255), font=get_font(17))

    out_path = os.path.join(ASSETS_DIR, "feature_graphic.png")
    img.convert("RGB").save(out_path, "PNG", quality=100)
    print(f"Feature graphic saved: {out_path}")

def generate_screenshot(filename, header_tag, main_title, subtitle, card_builder):
    w, h = 1080, 1920
    img = Image.new("RGBA", (w, h), (11, 15, 26, 255))
    draw = ImageDraw.Draw(img)

    for y in range(h):
        r = int(11 + (20 - 11) * (y / h))
        g = int(15 + (26 - 15) * (y / h))
        b = int(26 + (48 - 26) * (y / h))
        draw.line([(0, y), (w, y)], fill=(r, g, b, 255))

    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([(100, 200), (980, 900)], fill=(79, 70, 229, 25))
    img = Image.alpha_composite(img, glow)
    draw = ImageDraw.Draw(img)

    tag_font = get_font(26, bold=True)
    draw.rounded_rectangle([80, 110, 80 + len(header_tag)*15 + 30, 160], radius=10, fill=(30, 41, 59, 230), outline=(99, 102, 241, 200))
    draw.text((95, 120), header_tag, fill=(165, 180, 252, 255), font=tag_font)

    title_font = get_font(62, bold=True)
    draw.text((80, 180), main_title, fill=(255, 255, 255, 255), font=title_font)

    sub_font = get_font(30, bold=False)
    draw.text((80, 265), subtitle, fill=(148, 163, 184, 255), font=sub_font)

    mockup_x1, mockup_y1 = 70, 360
    mockup_x2, mockup_y2 = 1010, 1860
    draw.rounded_rectangle([mockup_x1 - 4, mockup_y1 - 4, mockup_x2 + 4, mockup_y2 + 4], radius=44, fill=(51, 65, 85, 200))
    draw.rounded_rectangle([mockup_x1, mockup_y1, mockup_x2, mockup_y2], radius=40, fill=(15, 23, 42, 255))

    draw.text((mockup_x1 + 40, mockup_y1 + 40), "MyCodeCalendar", fill=(255, 255, 255, 255), font=get_font(36, bold=True))
    draw.rounded_rectangle([mockup_x2 - 140, mockup_y1 + 35, mockup_x2 - 40, mockup_y1 + 80], radius=15, fill=(79, 70, 229, 255))
    draw.text((mockup_x2 - 120, mockup_y1 + 44), "LIVE", fill=(255, 255, 255, 255), font=get_font(22, bold=True))

    draw.line([(mockup_x1 + 40, mockup_y1 + 105), (mockup_x2 - 40, mockup_y1 + 105)], fill=(30, 41, 59, 255), width=2)

    card_builder(draw, mockup_x1 + 40, mockup_y1 + 130, mockup_x2 - mockup_x1 - 80)

    out_path = os.path.join(ASSETS_DIR, filename)
    img.convert("RGB").save(out_path, "PNG", quality=100)
    print(f"Screenshot saved: {out_path}")

def build_contests_cards(draw, x, y, width):
    contests = [
        ("Codeforces", "Codeforces Round 998 (Div. 3)", "In 1h 24m", "2h 15m", (239, 68, 68)),
        ("LeetCode", "Weekly Contest 438", "Tomorrow, 08:00 AM", "1h 30m", (245, 158, 11)),
        ("CodeChef", "Starters 174 (Div. 2)", "In 2 days", "2h 00m", (139, 92, 246)),
        ("AtCoder", "AtCoder Beginner Contest 394", "Saturday, 05:30 PM", "1h 40m", (16, 185, 129)),
        ("HackerRank", "ProjectEuler+ Coding Challenge", "Active Now", "Ongoing", (59, 130, 246)),
    ]
    cur_y = y
    for plat, title, time_str, duration, col in contests:
        draw.rounded_rectangle([x, cur_y, x + width, cur_y + 220], radius=24, fill=(24, 33, 50, 255), outline=(42, 55, 80, 255), width=2)
        draw.rounded_rectangle([x + 24, cur_y + 24, x + 24 + len(plat)*14 + 30, cur_y + 64], radius=10, fill=(15, 23, 42, 255))
        draw.ellipse([x + 36, cur_y + 39, x + 48, cur_y + 51], fill=col)
        draw.text((x + 58, cur_y + 32), plat, fill=(241, 245, 249, 255), font=get_font(22, bold=True))
        draw.rounded_rectangle([x + width - 210, cur_y + 24, x + width - 24, cur_y + 64], radius=10, fill=(79, 70, 229, 60), outline=(99, 102, 241, 160))
        draw.text((x + width - 195, cur_y + 32), time_str, fill=(165, 180, 252, 255), font=get_font(20, bold=True))
        draw.text((x + 24, cur_y + 85), title, fill=(255, 255, 255, 255), font=get_font(28, bold=True))
        draw.text((x + 24, cur_y + 145), f"⏱ Duration: {duration}  •  Public Contest", fill=(148, 163, 184, 255), font=get_font(22))
        draw.rounded_rectangle([x + width - 170, cur_y + 135, x + width - 24, cur_y + 185], radius=12, fill=(30, 41, 59, 255), outline=(71, 85, 105, 255))
        draw.text((x + width - 145, cur_y + 148), "🔔 Remind", fill=(226, 232, 240, 255), font=get_font(20, bold=True))
        cur_y += 245

def build_notification_cards(draw, x, y, width):
    cur_y = y
    draw.rounded_rectangle([x, cur_y, x + width, cur_y + 320], radius=24, fill=(30, 41, 59, 255), outline=(99, 102, 241, 255), width=2)
    draw.text((x + 30, cur_y + 30), "🔔 Push Notifications Active", fill=(255, 255, 255, 255), font=get_font(32, bold=True))
    draw.text((x + 30, cur_y + 80), "Automated 30m and 15m alerts before every bookmarked contest.", fill=(203, 213, 225, 255), font=get_font(22))

    options = [
        ("Alert 1 Hour Before", "Get ready and review editorial templates", True),
        ("Alert 15 Mins Before", "Direct link to contest arena launched", True),
        ("Contest End & Rating Sync", "Check results and updated ranks", False),
    ]
    sub_y = cur_y + 140
    for title, desc, active in options:
        draw.rounded_rectangle([x + 20, sub_y, x + width - 20, sub_y + 50], radius=10, fill=(15, 23, 42, 255))
        draw.text((x + 35, sub_y + 12), f"{'✓' if active else '○'}  {title}", fill=(241, 245, 249, 255), font=get_font(20, bold=True))
        sub_y += 56

    cur_y += 350
    draw.text((x, cur_y), "Recent Alerts Received", fill=(255, 255, 255, 255), font=get_font(30, bold=True))
    cur_y += 50
    notifs = [
        ("Codeforces Round 998 starts in 15 mins!", "Click to open problem set arena on mobile or browser.", "Just now"),
        ("LeetCode Weekly 438 is Live!", "Join 32,000+ programmers solving Problem A to D now.", "2 hours ago"),
        ("Google Kickstart Archives Added", "Explore practice questions curated by difficulty.", "Yesterday"),
    ]
    for n_title, n_body, n_time in notifs:
        draw.rounded_rectangle([x, cur_y, x + width, cur_y + 170], radius=20, fill=(24, 33, 50, 255), outline=(51, 65, 85, 200))
        draw.text((x + 25, cur_y + 20), n_title, fill=(255, 255, 255, 255), font=get_font(26, bold=True))
        draw.text((x + 25, cur_y + 65), n_body, fill=(148, 163, 184, 255), font=get_font(20))
        draw.text((x + 25, cur_y + 115), f"⏰ {n_time}", fill=(100, 116, 139, 255), font=get_font(18))
        cur_y += 190

def build_dsa_cards(draw, x, y, width):
    cur_y = y
    sheets = [
        ("Striver's SDE Sheet", "180 Core Interview Problems", "145 / 180 Solved", (16, 185, 129)),
        ("Blind 75 Curated List", "Most Asked FAANG Questions", "68 / 75 Solved", (245, 158, 11)),
        ("NeetCode 150 Patterns", "Sliding Window, Graphs, DP", "92 / 150 Solved", (99, 102, 241)),
        ("Dynamic Programming Mastery", "From Recursion to Bitmask DP", "42 / 60 Solved", (236, 72, 153)),
    ]
    for name, sub, progress, bar_col in sheets:
        draw.rounded_rectangle([x, cur_y, x + width, cur_y + 220], radius=24, fill=(24, 33, 50, 255), outline=(42, 55, 80, 255))
        draw.text((x + 30, cur_y + 25), name, fill=(255, 255, 255, 255), font=get_font(30, bold=True))
        draw.text((x + 30, cur_y + 70), sub, fill=(148, 163, 184, 255), font=get_font(22))
        draw.text((x + width - 260, cur_y + 70), progress, fill=(226, 232, 240, 255), font=get_font(22, bold=True))
        draw.rounded_rectangle([x + 30, cur_y + 130, x + width - 30, cur_y + 155], radius=10, fill=(15, 23, 42, 255))
        draw.rounded_rectangle([x + 30, cur_y + 130, x + int((width - 60) * 0.78), cur_y + 155], radius=10, fill=bar_col)
        draw.text((x + 30, cur_y + 175), "⚡ Solved with optimized time & space complexity", fill=(100, 116, 139, 255), font=get_font(18))
        cur_y += 250

def build_platforms_cards(draw, x, y, width):
    cur_y = y
    draw.rounded_rectangle([x, cur_y, x + width, cur_y + 80], radius=20, fill=(30, 41, 59, 255), outline=(51, 65, 85, 255))
    draw.text((x + 30, cur_y + 25), "🔍 Search contests, platforms or keywords...", fill=(148, 163, 184, 255), font=get_font(24))
    cur_y += 110

    platforms = [
        ("Codeforces", "Rating > 800-3500\nDiv 1, Div 2, Div 3, Div 4", (239, 68, 68)),
        ("LeetCode", "Biweekly & Weekly Contests\nGlobal Leaderboard", (245, 158, 11)),
        ("CodeChef", "Starters & Cook-Offs\nDivision 1 to 4", (139, 92, 246)),
        ("AtCoder", "Beginner & Regular (ABC, ARC)\nHigh-Quality Math", (16, 185, 129)),
        ("HackerRank", "University & Company Codesprints", (59, 130, 246)),
        ("HackerEarth", "Monthly Hackathons & Challenges", (236, 72, 153)),
    ]
    grid_w = (width - 30) // 2
    for idx, (p_name, desc, col) in enumerate(platforms):
        col_idx = idx % 2
        row_idx = idx // 2
        px = x + col_idx * (grid_w + 30)
        py = cur_y + row_idx * 250
        draw.rounded_rectangle([px, py, px + grid_w, py + 220], radius=22, fill=(24, 33, 50, 255), outline=(42, 55, 80, 255))
        draw.ellipse([px + 24, py + 24, px + 44, py + 44], fill=col)
        draw.text((px + 56, py + 20), p_name, fill=(255, 255, 255, 255), font=get_font(26, bold=True))
        draw.text((px + 24, py + 80), desc, fill=(148, 163, 184, 255), font=get_font(20))
        draw.rounded_rectangle([px + 24, py + 160, px + grid_w - 24, py + 198], radius=10, fill=(15, 23, 42, 255))
        draw.text((px + 40, py + 168), "✓ Synced to Calendar", fill=(16, 185, 129, 255), font=get_font(18, bold=True))

if __name__ == "__main__":
    generate_feature_graphic()
    generate_screenshot("screenshot_1.png", "LIVE SCHEDULE", "Never Miss a Contest", "Real-time countdowns across top competitive platforms", build_contests_cards)
    generate_screenshot("screenshot_2.png", "SMART NOTIFICATIONS", "Instant Push Alerts", "Get notified before every contest starts", build_notification_cards)
    generate_screenshot("screenshot_3.png", "DSA MASTERY", "Curated Problem Lists", "Track Striver, Blind 75 and NeetCode roadmaps", build_dsa_cards)
    generate_screenshot("screenshot_4.png", "ALL PLATFORMS", "Universal Hub", "Codeforces, LeetCode, CodeChef, AtCoder & more", build_platforms_cards)
    print("All store assets generated successfully!")
