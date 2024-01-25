import java.text.Normalizer
import java.util.*

fun slugify(word: String, replacement: String = "-") = Normalizer
    .normalize(word, Normalizer.Form.NFD)
    .replace("[^\\p{ASCII}]".toRegex(), "")
    .replace("[^a-zA-Z0-9\\s]+".toRegex(), "").trim()
    .replace("\\s+".toRegex(), replacement)
    .lowercase(Locale.getDefault())

fun loadGradleProperties() {
    val parents = ArrayList<Project>()

    var parent = project.parent
    while (parent != null) {
        parents.add(parent)
        parent = parent.parent
    }

    parents.reverse()

    for (parentItem in parents) {
        val propertiesFile = File("${parentItem.projectDir}", "gradle.properties")

        if (propertiesFile.exists()) {
            propertiesFile.inputStream().use { stream ->
                val properties = Properties()
                properties.load(stream)

                properties.forEach { key, value ->
                    val keyName = key.toString()
                    var keyVal = value.toString()

                    if (keyName.equals("id")) {
                        keyVal = slugify(value.toString())
                    }

                    setProperty(keyName, keyVal)
                }
            }
        }
    }
}

loadGradleProperties()
