import org.gradle.kotlin.dsl.DependencyHandlerScope

internal fun DependencyHandlerScope.add(
    configurationName: String,
    name: String,
    version: String? = null
) = add(configurationName, "$name${if (version != null) ":$version" else ""}")

internal fun DependencyHandlerScope.api(name: String, version: String? = null) =
    add("api", name, version)

internal fun DependencyHandlerScope.implementation(name: String, version: String? = null) =
    add("implementation", name, version)

internal fun DependencyHandlerScope.kapt(name: String, version: String? = null) =
    add("kapt", name, version)