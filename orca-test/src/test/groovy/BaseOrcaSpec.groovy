import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Josef Sustacek
 */
class BaseOrcaSpec extends Specification {
    static def ORCA_TEST_RUNTIME_IMPL = System.getProperty('ORCA_TEST__RUNTIME_IMPL')

    @Shared
    Orca orca = switch (ORCA_TEST_RUNTIME_IMPL) {
        case 'docker' -> yield new DockerOrca()
        case 'native' -> yield new NativeOrca()
        default -> throw new IllegalArgumentException("Orca test runtime '${ORCA_TEST_RUNTIME_IMPL}' unknown.")
    }

    def setup() {
        orca.setup()
    }

    def cleanup() {
        orca.cleanup()
    }

    /**
     * Writes given stdout content (String) into a file in Orca build dir, so that
     * it can be archived.
     *
     * NOTE: When invoking this method multiple times per-feature, use varying
     * <code>fileNameSuffix</code> parameter for the calls.
     * @param stdout
     * @return
     */
    def dumpOrcaStdoutToFile(String stdout, String fileNameSuffix = '') {
        def targetFile = new File(orca.buildsPath(), "_orca-stdout${fileNameSuffix}.txt")

        targetFile.text = stdout
    }
}
