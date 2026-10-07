# CodeCalendar Release Signing Key Information

This document records the official production signing credentials and certificate details used to sign the Google Play Store Android App Bundle (`app-release.aab`).

---

## 1. Keystore File Details

- **File Path**: [`documentation/codecalendar-release.jks`](file:///d:/Projects2026/dsaapp/documentation/codecalendar-release.jks)
- **Keystore Type**: `PKCS12`
- **Keystore Password**: `8261830043`
- **Key Alias**: `key0`
- **Key Password**: `8261830043`
- **Algorithm**: `2048-bit RSA`
- **Signature Algorithm**: `SHA384withRSA`
- **Validity**: `10,000 days` (Valid until **22 February 2054**)

---

## 2. Certificate Fingerprints

- **SHA-1**: `96:D5:D2:28:D2:ED:67:19:0F:55:CC:71:A4:A2:96:58:55:05:2A:D7`
- **SHA-256**: `14:36:91:9E:0D:BA:35:9F:81:81:3E:A6:71:D6:54:30:34:CB:94:C5:E8:85:8D:B4:40:43:D0:DA:CC:F8:3E:4D`

> [!NOTE]
> If you configure Google Sign-In or Firebase Phone Authentication in the Firebase Console, add the **SHA-1** and **SHA-256** fingerprints listed above to your Android App settings in the Firebase Console.

---

## 3. How to Build the Signed Release Bundle (AAB)

To generate a new release AAB bundle:

```bash
./gradlew bundleRelease
```

The output file will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

This bundle is ready to be uploaded directly to the **Google Play Console** internal testing, closed testing, or production track.
