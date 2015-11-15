import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class SettingsPlugin implements Plugin<Settings> {

    @Override
    void apply(Settings settings) {
        println new File('test.test').text
    }

}