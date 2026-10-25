# X-Posed Patches

Morphe patches that bring [X-Posed](https://github.com/xaitax/x-account-location-device)
account location badges to the X Android app. Meant to be applied alongside
[piko](https://github.com/crimera/piko).

## Patches

**Account location badges**: adds X-Posed's badge (country flag and circled-i) after
names in post headers. Tap a badge for an account sheet with what X reports: country,
connection source, location accuracy warning and affiliation, plus a one-tap
highlight/hide toggle for that country and a link to the settings.

Lookups go to the X-Posed community cache first, in batches of up to 100 handles.
Misses fall back to X's own "About this account" query, sent through the app's own
authenticated client. Fresh English results are contributed back to the cache,
like the extension does. Results are cached on the device for 60 days.

**Country filters**: posts from chosen countries or X regions are highlighted (default,
X-Posed's tint and rail) or hidden. A filtered quoted account only affects its quote card.
Passive engagement with filtered posts (impressions, viewing time, video views) is not
reported to X; likes, replies and reposts still are.

**Settings**: badge style with a live preview, filter mode and countries, engagement and
community cache switches, in a page styled like X's own settings.

Shows what X reports, not anyone's live location or the device used for a post.

Supported: X 12.19.1-release.0 (APKM).

## Building

Requires JDK 21 and a GitHub token with `read:packages` for Morphe's Gradle plugin.

```sh
GITHUB_ACTOR=<user> GITHUB_TOKEN=$(gh auth token) ./gradlew build
```

The bundle is written to `patches/build/libs/patches-<version>.mpp`.
Hook notes for porting to new X versions are in [docs/HOOKS.md](docs/HOOKS.md).
