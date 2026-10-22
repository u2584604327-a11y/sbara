from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).parent
manifest = root / "app/src/main/AndroidManifest.xml"
text = manifest.read_text(encoding="utf-8")
checks = {
    "NO_INTERNET_PERMISSION": "android.permission.INTERNET" not in text,
    "NO_ACCESSIBILITY_SERVICE": "AccessibilityService" not in text and "BIND_ACCESSIBILITY_SERVICE" not in text,
    "HAS_USAGE_STATS": "PACKAGE_USAGE_STATS" in text,
    "HAS_OVERLAY": "SYSTEM_ALERT_WINDOW" in text,
    "HAS_SPECIAL_USE_FGS": "FOREGROUND_SERVICE_SPECIAL_USE" in text and 'foregroundServiceType="specialUse"' in text,
    "HAS_BOOT_RECEIVER": "BOOT_COMPLETED" in text,
    "HAS_SPLASH_ACTIVITY": ".SplashActivity" in text,
    "HAS_ADAPTIVE_ICON": (root / "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").exists(),
    "HAS_BUILD_WORKFLOW": (root / ".github/workflows/android-build.yml").exists(),
}
ET.parse(manifest)
for path in (root / "app/src/main/res").rglob("*.xml"):
    ET.parse(path)
for key, value in checks.items():
    print(f"{key}={'PASS' if value else 'FAIL'}")
print("XML_PARSE=PASS")
if not all(checks.values()):
    raise SystemExit(1)
