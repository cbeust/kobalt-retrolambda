import com.beust.kobalt.plugin.kotlin.kotlinProject
import com.beust.kobalt.plugin.packaging.assemble

val p = kotlinProject {
    name = "kobalt-retrolambda"
    version = "0.1"

    dependencies {
        compile("com.beust:kobalt:")
    }

    assemble {
        mavenJars {
            fatJar = true
        }
    }
}
