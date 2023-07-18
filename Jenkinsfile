@Library('corda-shared-build-pipeline-steps@GA-July') _

cordaPipelineKubernetesAgent(
    publishRepoPrefix: 'corda-os-maven',
    slimBuild: true,
    runUnitTests: true,
    dedicatedJobForSnykDelta: false,
    gitHubComments: false,
    e2eTestName: 'corda-utxo-ledger-extensions-e2e-tests',
    runE2eTests: true,
    publishToMavenS3Repository: true,
    gradleAdditionalArgs: '-PcordaNotaryPluginsVersion=5.0.0.0-beta+'
    )
