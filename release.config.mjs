import config from 'semantic-release-preconfigured-conventional-commits' assert { type: "json" }

config.plugins.push(
    "@semantic-release/github",
    "@semantic-release/git",
)
config.tagFormat = "v${version}"

export default config
