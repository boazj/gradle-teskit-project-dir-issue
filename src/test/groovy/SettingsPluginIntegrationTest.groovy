import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsPluginIntegrationTest {
    @Rule
    public final TemporaryFolder dir = new TemporaryFolder()

    List<File> pluginClasspathFiles
    List<String> pluginClasspath

    @Before
    void setup(){
        URL pluginClasspathResource = getClass().classLoader.findResource('plugin-classpath.txt')
        if (pluginClasspathResource == null) {
            throw new IllegalStateException('Did not find plugin classpath resource, run `testClasses` build task.')
        }

        pluginClasspathFiles = pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') }
                .collect { new File(it) }

        pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace('\\\\', '\\\\\\\\') }
                .collect { "\'${it}\'" }

    }

    @Test
    void test() {
        //Configure classpath and apply plugin on settings file
        File settingsFile = dir.newFile('settings.gradle')
        settingsFile << """ //  ---SC.1---
                        buildscript {
                            dependencies {
                                classpath files($pluginClasspath)
                            }
                        }
                        apply plugin: 'settings-plugin'
                        """

        //Creates some dummy build file
        File buildFile = dir.newFile('build.gradle')
        buildFile << """
                         apply plugin: 'java'
                         repositories {
                            jcenter()
                         }
                         dependencies {
                            compile 'junit:junit:4.12'
                         }
                     """

        //Creates the file the plugin access in the settings plugin
        File testFile = dir.newFile('test.test')
        testFile << 'bla bla bla'

        BuildResult result = GradleRunner.create()
                .withPluginClasspath(pluginClasspathFiles)     //This should have done the build script classpath configuration on the settings (see SC.1 comment)
                .withProjectDir(dir.getRoot())  //This should have done defined the working dir for the settings file too
                .withArguments('compileJava')
                .build();

        //FAILURE
        //Reason:
        //test.test (No such file or directory)
        //-----

        //if instead we'll use the absolute path - it will work. meaning the withProjectDir does not apply to settings evaluation of relative path references
        //same as withPluginClasspath
    }

}