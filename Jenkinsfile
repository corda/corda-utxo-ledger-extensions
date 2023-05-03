@Library('corda-shared-build-pipeline-steps@Hawk') _

cordaPipeline(
    publishRepoPrefix: 'corda-os-maven',
    slimBuild: true,
    runUnitTests: true,
    dedicatedJobForSnykDelta: false,
    gitHubComments: false,
    e2eTestName: 'corda-utxo-ledger-extensions-e2e-tests',
    runE2eTests: false,
    publishToMavenS3Repository: true,
    enableNotifications: false
    )
