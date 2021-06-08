import * as YAML from 'https://deno.land/std@0.97.0/encoding/yaml.ts'
import * as Path from 'https://deno.land/std@0.97.0/path/mod.ts'
import { Sha1 } from 'https://deno.land/std@0.97.0/hash/sha1.ts'
import { parseFlags } from 'https://deno.land/x/typed_flags@v1.0.0/mod.ts'

const flags = parseFlags({
  'dry-run': Boolean,
})

if (!await hasPermision({ name: 'env', variable: 'HOME' })) Deno.exit(1)

const VERSION_FILE = Path.join('.', 'VERSION')
const VERSIONS_FILE = Path.join('.', 'versions.yaml')
const CHANGELOG_FILE = Path.join('.', 'changelog.md')
const CHANGELOG_CHECKSUM_FILE = Path.join('.', '.changelog.md.shasum')
const GRADLE_PROPERTIES_FILE = Path.join(Deno.env.get('HOME')!, '.gradle', 'gradle.properties')

if (!await hasPermision({ name: 'read', path: VERSION_FILE })) Deno.exit(1)
if (!await hasPermision({ name: 'read', path: VERSIONS_FILE })) Deno.exit(1)
if (!await hasPermision({ name: 'read', path: CHANGELOG_FILE })) Deno.exit(1)
if (!await hasPermision({ name: 'read', path: CHANGELOG_CHECKSUM_FILE })) Deno.exit(1)
if (!await hasPermision({ name: 'read', path: GRADLE_PROPERTIES_FILE })) Deno.exit(1)

const CURSEFORGE_PROJECT_ID = '283350'
const CURRENT_VERSION = await Deno.readTextFile(VERSION_FILE)

const commandPath = '.' + (Deno.build.os === 'windows' ? '\\gradlew.bat' : '/gradlew')
const gradleProperties: Record<string, string> = Object.fromEntries((await Deno.readTextFile(GRADLE_PROPERTIES_FILE)).split(/\r?\n|\r/g).filter(Boolean).map(line => line.split(/=/)))

const changelogChecksum = (new Sha1).update(await Deno.readFile(CHANGELOG_FILE)).hex()
if (await exists(CHANGELOG_CHECKSUM_FILE)) {
  if ((await Deno.readTextFile(CHANGELOG_CHECKSUM_FILE)).split(' ')[0] === changelogChecksum) {
    console.error('Changelog has not been changed since last run!')
    Deno.exit(1)
  }
}

if (!flags['dry-run']) await Deno.writeTextFile(CHANGELOG_CHECKSUM_FILE, `${changelogChecksum}  changelog.md\n`)

if (!await hasPermision({ name: 'net', host: 'addons-ecs.forgesvc.net' })) Deno.exit(1)
console.log('Fetching uploaded versions...')
const res = JSON.parse(await (await fetch(`https://addons-ecs.forgesvc.net/api/v2/addon/${CURSEFORGE_PROJECT_ID}/files`, { headers: { 'X-Api-Token': gradleProperties['curseforgeKey'] } })).text(), dateReviver) as CurseForgeFile[]
console.log('Fetched uploaded versions')
const versions = YAML.parseAll(await Deno.readTextFile(VERSIONS_FILE)) as Version[]

for (const version of versions) {
  const filename = `boxlin-${CURRENT_VERSION}-${version.minecraft}-mod.jar`
  if (res.some(it => it.fileName === filename)) {
    console.log('The version %s exists for boxlin. Skipping.', `${CURRENT_VERSION}-${version.minecraft}`)
    continue
  }
  console.log('No uploaded version %s for boxlin exists. Uploading...', `${CURRENT_VERSION}-${version.minecraft}`)
  const args = [
    `-PminecraftVersion=${version.minecraft}`,
    `-PforgeVersion=${version.forge}`,
    `-PmappingsVersion=${version.mappings.version}`,
    `-PmappingsChannel=${version.mappings.channel}`
  ]
  const wd = Path.resolve('.', 'versions', version.workingDirectory)
  console.log('Found version directory %s', wd)
  console.log('Uploading boxlin version %s to MyRemote...', `${CURRENT_VERSION}-${version.minecraft}`)
  if (!flags['dry-run']) if (!await gradle(wd, 'publishBoxlinPublicationToMyRemoteRepository', ...args)) {
    console.error('Error while trying to publish')
    Deno.exit(1)
  }
  console.log('Uploading boxlin version %s to curseforge...', `${CURRENT_VERSION}-${version.minecraft}`)
  const versionChangelogFile = Path.join(wd, Path.basename(CHANGELOG_FILE))
  if (!flags['dry-run']) if (!hasPermision({ name: 'write', path: versionChangelogFile })) Deno.exit(1)
  await Deno.copyFile(CHANGELOG_FILE, versionChangelogFile)
  if (!flags['dry-run']) if (!await gradle(wd, 'curseforge', ...args)) {
    console.error('Error while trying to publish')
    Deno.exit(1)
  }
  await Deno.remove(versionChangelogFile)
  console.log('Uploaded boxlin version %s', `${CURRENT_VERSION}-${version.minecraft}`)
}

async function gradle(cwd: string, ...args: string[]) {
  if (!await hasPermision({ name: 'run', command: commandPath })) Deno.exit(1)
  return (await Deno.run({ cwd, cmd: [commandPath, ...args] }).status()).success
}

function dateReviver(_key: string, value: any) {
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d{1.3})?(?:Z(?:[+-]?\d{4})?)?$/.test(value)) return new Date(value)
  return value
}

async function hasPermision(permission: Deno.PermissionDescriptor) {
  const queryState = (await Deno.permissions.query(permission)).state
  if (queryState === 'granted') return true
  if (queryState === 'denied') return false
  if ((await Deno.permissions.request(permission)).state === 'granted') return true
  return false
}

async function exists(path: string) {
  try {
    await Deno.stat(path)
    return true
  } catch {
    return false
  }
}

interface Version {
  workingDirectory: string
  minecraft: string
  forge: string
  mappings: VersionMappings
}

interface VersionMappings {
  channel: string
  version: string
}

interface CurseForgeFile {
  id: number
  displayName: string
  fileName: string
  fileDate: Date
  fileLength: number
  releaseType: number
  fileStatus: number
  downloadUrl: string
  isAlternate: boolean
  alternateFileId: number
  dependencies: any[]
  isAvailable: boolean
  modules: object
  packageFingerprint: number
  gameVersion: string[]
  installMetadata: any
  serverPackFileId: any
  hasInstallScript: boolean
  gameVersionDateReleased: Date
  gameVersionFlavor: any
}
