import groovy.yaml.YamlSlurper

import java.util.concurrent.TimeUnit

class BuildSpec extends BaseOrcaSpec {

    def "'build' should create docker-compose.yml"(int timeoutSeconds) {
        when:
            // build some non-default (non-1.0.0) version
            def orcaRun = orca.startOrca('build', '2.1.42')
            orcaRun.waitFor(timeoutSeconds, TimeUnit.SECONDS)

        then:
            orcaRun.exitValue() == 0

            def stdout = orcaRun.inputStream.getText('utf-8')

            stdout.contains('Using the default single server configuration.')

            def dockerCompose = new File(orca.buildsPath(), '2.1.42/docker-compose.yml')
            dockerCompose.isFile()

            // Parse YAML, confirm the content
            def ys = new YamlSlurper()

            def dockerComposeYaml = ys.parseText(dockerCompose.text)

            dockerComposeYaml.services != null

            dockerComposeYaml.services.antivirus?.image == 'antivirus:2.1.42'
            dockerComposeYaml.services.backup?.image == 'backup:2.1.42'
            dockerComposeYaml.services.ci?.image == 'ci:2.1.42'
            dockerComposeYaml.services.db?.image == 'db:2.1.42'
            dockerComposeYaml.services.liferay?.image == 'liferay:2.1.42'
            dockerComposeYaml.services.'log-proxy'?.image == 'log-proxy:2.1.42'
            dockerComposeYaml.services.'log-server'?.image == 'log-server:2.1.42'
            dockerComposeYaml.services.search?.image == 'search:2.1.42'
            dockerComposeYaml.services.vault?.image == 'vault:2.1.42'
            dockerComposeYaml.services.'web-server'?.image == 'web-server:2.1.42'

        cleanup:
            dumpOrcaStdoutToFile(stdout)

        where:
            timeoutSeconds = 30
    }
}