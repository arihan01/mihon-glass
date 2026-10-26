# Updating and installing this fork

This fork carries a Liquid Glass redesign on top of [Mihon](https://github.com/mihonapp/mihon). This
guide covers keeping it in sync with upstream, building a signed APK, and installing it on an
Android phone over a wireless adb connection.

## How the repository is set up

| | |
|---|---|
| `glass` branch | The redesign, as commits on top of upstream |
| `main` branch | An untouched copy of upstream Mihon |
| `origin` remote | This fork, `git@github.com:arihan01/mihon-glass.git` |
| `upstream` remote | Official Mihon, `https://github.com/mihonapp/mihon.git` |

The fork uses a single light and dark theme. The color themes, Monet and the AMOLED option from
upstream were removed on purpose.

### Signing

Release builds are signed with a personal key so each new build installs over the previous one.
The build reads the key's location and passwords from `keystore.properties` in the repository root:

```properties
storeFile=/path/to/mihon-release.jks
storePassword=...
keyAlias=mihon
keyPassword=...
```

`keystore.properties` is gitignored, and the `.jks` file lives outside the repository. Neither should
ever be committed. Keep a backup of both somewhere safe: without the original key, Android won't
accept a new build as an update, and the only way forward is to back up the app's data, uninstall,
install the new build, and restore.

To set up a new machine, copy the `.jks` file over and create `keystore.properties` pointing at it.
Machines without the key can still make debug builds; only release builds need it.

## Updating from upstream

Run all commands from the repository root.

**1. Fetch the latest upstream changes**

```bash
git fetch upstream
```

**2. Bring `main` up to date**

Move the local `main` to match upstream, then push it to the fork. Both only move `main` forward,
so no force is needed:

```bash
git branch -f main upstream/main
git push origin main
```

**3. Rebase the redesign onto it**

Make sure you're on `glass` and `git status` shows no uncommitted changes. Then:

```bash
git rebase main
```

`glass` follows upstream's `main`, not its release tags. Upstream tags releases on a separate
branch, so rebasing onto a tag would also replay every upstream commit made since that branch split
off, not just the redesign.

**4. Resolve conflicts, if any**

Fix the conflicting files, then `git add` them and run `git rebase --continue`. To give up and get
back to where you started, run `git rebase --abort`.

Conflicts are most likely in the files the redesign changes:

- `app/.../presentation/theme/`: every color scheme except `TachiyomiColorScheme.kt` was deleted
- `HomeScreen.kt`: the floating tab bar
- `presentation-core/.../material/Scaffold.kt` and `Glass.kt`: the glass header and materials
- `LibraryTab.kt`, `LibraryContent.kt`, `TabbedScreen.kt`: tab rows moved into the header
- `AppBar.kt`, `MangaToolbar.kt`, the reader bars, `colors_tachiyomi.xml`

If upstream adds a new color theme or changes the theme picker, leave that part out.

**5. Format, compile and run the contrast test**

```bash
./gradlew spotlessApply :app:compileDebugKotlin :app:testDebugUnitTest --tests '*TachiyomiColorSchemeContrastTest*'
```

The contrast test checks that every color pairing in the theme still meets WCAG AA.

**6. Push to the fork**

A rebase rewrites the redesign commits, so a plain push is rejected. `--force-with-lease` replaces
the branch only if nobody else has pushed to it in the meantime:

```bash
git push --force-with-lease origin glass
```

**7. Build the signed release APK**

```bash
./gradlew assembleRelease
```

The APKs land in `app/build/outputs/apk/release/`. Modern phones need `app-arm64-v8a-release.apk`;
the others are for other CPU types, and `app-universal-release.apk` works on any of them.

Local builds leave out telemetry and the in-app updater. Avoid `-Pdist=github` and `-Pdist=ci`: those
turn the updater on, and it offers official Mihon APKs that can't install over this signature.

**8. Install it on the phone**, as described below.

## Installing on a phone over wireless adb

Requires Android 11 or newer, with the phone and the computer on the same Wi-Fi network.

**1. Turn on Wireless debugging**

On the phone, open Settings → Developer options and turn on **Wireless debugging**. If Developer
options isn't there, open Settings → About phone → Software information and tap **Build number**
seven times.

**2. Check whether the phone is already connected**

```bash
adb devices
```

A line ending in `device` means it's connected; skip to step 5. If nothing is listed, or it says
`offline`, continue.

**3. Pair (first time on a computer only)**

On the phone, in Wireless debugging, tap **Pair device with pairing code**. It shows an address and
a six-digit code. Run the following with that address and enter the code when asked:

```bash
adb pair 192.168.1.23:37123
```

**4. Connect**

Use the address on the main Wireless debugging screen, whose port differs from the pairing one:

```bash
adb connect 192.168.1.23:41234
```

Run `adb devices` again to confirm the phone shows as `device`.

**5. Install**

```bash
adb install -r app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

`-r` replaces the installed app and keeps its library and settings. It should print `Success`. If
more than one device is listed, add `-s <serial>` after `adb`, using the serial from `adb devices`.

### Common install errors

- **`INSTALL_FAILED_UPDATE_INCOMPATIBLE`**: the installed app was signed with a different key, for
  example official Mihon. In that app, create a backup (Settings → Data and storage → Create backup),
  uninstall it, install this APK, then restore the backup. Downloaded chapters live in the storage
  folder you chose and survive the uninstall. Extensions stay installed but need to be trusted again.
- **`INSTALL_FAILED_VERSION_DOWNGRADE`**: the new APK has a lower version than the installed one,
  usually after rebasing onto an older upstream commit than the last build. Update `main` and
  rebase again.
- **`no devices/emulators found`**: Wireless debugging switches itself off after a reboot or a Wi-Fi
  change. Turn it back on and repeat steps 2 to 4.

### Without adb

Copy the APK to the phone (USB, a cloud drive, or by sending it to yourself) and open it from the
Files app. The first time, Android asks to allow installs from that app.
