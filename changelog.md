Changes:
- `getGuiConfig -> getGuiConfigScreen`
- `get(category, key, defaultValue, comment, values) -> get(category, key, defaultValue, comment)`
- `ConfigurationHandler.guiConfig -> ConfigurationHandler.guiConfigScreen`

Added:
- Javadoc
- Kotlin Reflect is now bundled with the jar
- `ProxyInjector` delegate to create proxies
- `useProxy` to create `ProxyInjector` instances
