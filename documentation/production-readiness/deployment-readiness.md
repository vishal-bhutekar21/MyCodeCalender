# Deployment & CI/CD Audit

## 1. CI/CD GitHub Actions Pipeline Audit

The continuous integration workflow is defined in [.github/workflows/pr.yml](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml).

### 1.1 Guaranteed CI Failure on PRs (Critical Finding)
```yaml
# Lines 36-37 in pr.yml
- name: Build Ktor Backend
  run: ./gradlew :backend:build
```

#### Why This Fails in GitHub Actions
1. In [settings.gradle.kts](file:///d:/Projects2026/dsaapp/settings.gradle.kts), `:backend` is **not included**.
2. When Gradle runs `./gradlew :backend:build`, it immediately terminates with:
   `Project ':backend' not found in root project 'CodeCalendar'.`
3. As a result, **every pull request opened against `main` automatically fails the CI check**.

---

### 1.2 Incompatible Machine-Specific Java Path
In [gradle.properties:L4](file:///d:/Projects2026/dsaapp/gradle.properties#L4):
```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-21
```
* **Why This Breaks CI and Other Machines**:
  1. [.github/workflows/pr.yml](file:///d:/Projects2026/dsaapp/.github/workflows/pr.yml#L9) runs on `ubuntu-latest`.
  2. Linux CI runners do not have Windows file systems and do not have Java at `C:\Program Files\Java\jdk-21`.
  3. Running `./gradlew` on Linux or on any developer machine without this exact path fails immediately with:
     `Value 'C:\Program Files\Java\jdk-21' given for org.gradle.java.home Gradle property is invalid (Java home supplied is invalid).`
* **Remediation**: Remove `org.gradle.java.home` from `gradle.properties`. Use Gradle toolchains or the system `JAVA_HOME` environment variable configured via `actions/setup-java`.

---

## 2. Release APK Build Configuration

In [app/build.gradle.kts:L22-L40](file:///d:/Projects2026/dsaapp/app/build.gradle.kts#L22-L40):
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release.jks")
        storePassword = "8261830043"
        keyAlias = "key0"
        keyPassword = "8261830043"
        enableV1Signing = true
        enableV2Signing = true
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### Flaws Identified
1. **Missing Release Keystore**: `app/release.jks` does not exist in the repository. Running `./gradlew assembleRelease` fails with `Cannot find keystore file 'release.jks'`.
2. **Plaintext Keystore Passwords**: Passwords (`8261830043`) are committed directly to version control.
3. **Disabled Lint Verification**: Lines 55–58 specify:
   ```kotlin
   lint {
       checkReleaseBuilds = false
       abortOnError = false
   }
   ```
   This suppresses compiler warnings, unused resources, and critical Android API deprecations during release packaging.

---

## 3. Web Admin Portal Deployment Readiness (`codecalendar-admin`)

Located in [codecalendar-admin/](file:///d:/Projects2026/dsaapp/codecalendar-admin):
* **Build Script**: `npm run build` runs `tsc -b && vite build`.
* **Routing Rewrites**: [vercel.json](file:///d:/Projects2026/dsaapp/codecalendar-admin/vercel.json) correctly configures SPA routing rewrites:
  ```json
  {
    "rewrites": [{ "source": "/(.*)", "destination": "/" }]
  }
  ```
* **Environment Variable Vulnerability**: In [firebase.ts:L6-L13](file:///d:/Projects2026/dsaapp/codecalendar-admin/src/services/firebase.ts#L6-L13), if the production deployment environment fails to supply `VITE_FIREBASE_PROJECT_ID`, the web app silently falls back to `shetkari-mitra-7721` (a different application).
* **Remediation**: Remove hardcoded fallbacks in `firebase.ts`; fail the build with a descriptive error if environment variables are missing.
