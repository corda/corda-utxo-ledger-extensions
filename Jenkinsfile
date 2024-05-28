@Library('corda-shared-build-pipeline-steps@5.2.1-GA') _

cordaPipelineKubernetesAgent(
    publishRepoPrefix: 'corda-os-maven',
    slimBuild: true,
    runUnitTests: true,
    dedicatedJobForSnykDelta: false,
    gitHubComments: false,
    e2eTestName: 'corda-utxo-ledger-extensions-e2e-tests',
    runE2eTests: true,
    publishToMavenS3Repository: true,
    gradleAdditionalArgs: '-PcordaNotaryPluginsVersion=5.2.1-GA.0.0-beta+',
    javaVersion: '17'
    )
