#!/bin/bash
versions=(
  '1.15.2-31.2.0:snapshot:20190719-1.14.3'
  '1.14.4-28.2.0:snapshot:20190719-1.14.3'
)

publishing="$1"
case "$publishing" in
  local|remote);;
  *)
    echo "First parameter must be either local or remote"
    exit
    ;;
esac

# To catch errors with the curseforge publication early
if [ "$publishing" == "remote" ]; then
  if [ ! -f "changelog.md" ]; then
    echo The changelog does not exist
    exit
  fi
  if [ -f ".changelog.md.shasum" ]; then
    if shasum -c ".changelog.md.shasum"; then
      echo changelog has not been changed since last run!
      exit
    fi
  fi
  shasum changelog.md > ".changelog.md.shasum"
fi

for version in "${versions[@]}"; do
  save="$IFS"
  IFS=':'
  # shellcheck disable=SC2206
  versionMappingSplit=($version)
  IFS='-'
  # shellcheck disable=SC2206
  versionSplit=(${versionMappingSplit[0]})
  IFS="$save"
  mcVersion=${versionSplit[0]}
  forgeVersion=${versionSplit[1]}
  mappingsChannel=${versionMappingSplit[1]}
  mappingsVersion=${versionMappingSplit[2]}

  args=("-PminecraftVersion=${mcVersion}" "-PforgeVersion=${forgeVersion}" "-PmappingsVersion=${mappingsVersion}" "-PmappingsChannel=${mappingsChannel}")

  if [ "$publishing" == "local" ]; then
    if ! ./gradlew publishBoxlinPublicationToMavenLocal "${args[@]}"; then
      echo "Received non zero exit code. Stopping script."
      exit
    fi
  elif [ "$publishing" == "remote" ]; then
    if [ ! -f "changelog.md" ]; then
      echo The changelog does not exist
      exit
    fi
    exit
    if ! ./gradlew publishBoxlinPublicationToGitHubRepository "${args[@]}"; then
      echo "Received non zero exit code. Stopping script."
      exit
    fi
    if ! ./gradlew publishBoxlinPublicationToBintrayRepository "${args[@]}"; then
      echo "Received non zero exit code. Stopping script."
      exit
    fi
    if ! ./gradlew curseforge "${args[@]}"; then
      echo "Received non zero exit code. Stopping script."
      exit
    fi
  fi
done
