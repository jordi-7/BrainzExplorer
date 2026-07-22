# AI Usage

This project was built with Claude Code as a pair-programming and architecture-discussion
partner throughout development. This document summarizes where and how AI was used, and
what AI-suggested output was rewritten or rejected.

## Tools used

- **Claude Code** — used interactively during implementation for scaffolding, boilerplate,
  tests, debugging, API exploration, and architectural discussion.

## What AI was used for

| Area | Use                                                                                                                                                                                                                                                    | Where |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--|
| API exploration | Explored the MusicBrainz API (search/lookup endpoints, `inc=release-groups` includes, response shapes) before wiring up Retrofit                                                                                                                       | `data/remote/MusicBrainzApi.kt` |
| Repository design | Suggested the repository method shape                                                                                                                                                                                                                  | `domain/repository/ArtistRepository.kt`, `data/repository/ArtistRepositoryImpl.kt` |
| Cover art endpoint | Suggested Cover Art Archive for artworks                                                                                                                                                                                                               | `data/mapper/ArtistMapper.kt` |
| Rate limit interceptor | Suggested and implemented request throttling to respect MusicBrainz's ~1 request/sec usage policy                                                                                                                                                      | `data/remote/RateLimitInterceptor.kt` |
| Home screen suggestions | Suggested the curated list of featured artists                                                                                                                                                                                                         | `data/repository/ArtistRepositoryImpl.kt` (`FEATURED_ARTIST_IDS`), `ui/home/HomeScreen.kt` |
| Tests | Wrote unit tests and suggested the testing stack                                                                                                                                                                                                       | `ArtistMapperTest`, `RateLimitInterceptorTest`, `UserAgentInterceptorTest`, `ArtistRepositoryImplTest`, `HomeViewModelTest`, `ArtistDetailViewModelTest` (JUnit4, MockK, Turbine, kotlinx-coroutines-test, Robolectric) |
| Dependencies | Added several Gradle dependencies in batch (networking, DI, testing, image loading, DataStore)                                                                                                                                                         | `app/build.gradle.kts` |
| Docs | Generated the initial README                                                                                                                                                                                                                           | `README.md` |
| Project structure | Reviewed package layout (`data` / `domain` / `ui` / `di` / `navigation`) for consistency                                                                                                                                                               | — |
| UX/text polish | Implemented the search field's expand/collapse + crossfade animation; suggested some string wording; filled in a few explanatory comments where the reasoning wasn't obvious from the code alone (e.g. why the rate limiter blocks the calling thread) | `ui/components/CollapsibleSearchField.kt`, `res/values/strings.xml` |
| This file | Proposed the template/structure for this file; content reviewed and completed by the user                                                                                                                                                              | `AI_USAGE.md` |

## What was rewritten or rejected

- **BuildConfig vs. resource values**: AI suggested exposing build/version info via generated
  `BuildConfig` fields. Rejected in favor of Gradle `resValue()` entries (`app_version_name`,
  `debug_mode` in `app/build.gradle.kts`), keeping these as Android resources instead of adding
  a `BuildConfig` class to the build.
