# Architecture Fitness Functions

## Architecture boundaries

`verifyArchitectureDependencies` fails the build if any `:features:*` or `:core:*` module depends on `:app`.

## Secrets

`verifyNoHardcodedOpenWeatherSecrets` fails the build if an OpenWeather API key is committed into source/config files.

OpenWeather configuration must come from one of:

- Gradle property `OPENWEATHERMAP_API_KEY`
- Environment variable `OPENWEATHERMAP_API_KEY`
- Local untracked `local.properties`

`WEATHER_API_BASE_URL` can be changed to point the app at a backend/proxy without changing weather repository code.

## Behavioral checks

Unit tests protect these invariants:

- fresh weather cache is used without a remote request;
- stale weather cache is used as fallback when remote weather is unavailable;
- weather cache is updated after a successful remote request;
- wardrobe operations reject writes to another user's data.
