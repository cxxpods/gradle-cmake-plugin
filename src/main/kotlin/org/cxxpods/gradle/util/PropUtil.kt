@file:Suppress("UnstableApiUsage")

package org.cxxpods.gradle.util

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

fun env(vararg names: String) = names
  .map { System.getenv(it) }
  .find { it != null }

class PropFactory<R>(private val instance: R, private val project: Project) {
  @Suppress("UNCHECKED_CAST")
  fun <T, P : Property<T>> propertyDelegate(type: KClass<*>, propTypeClazz: KClass<P>, defaultValue: T): ReadWriteProperty<R, T> {
    val p = when (propTypeClazz) {
      is DirectoryProperty -> project.objects.directoryProperty()
      else -> project.objects.property(type.java)
    } as P
    p.set(defaultValue)

    return object : ReadWriteProperty<R, T> {
      override fun getValue(thisRef: R, property: KProperty<*>): T {
        return p.orNull as T
      }

      override fun setValue(thisRef: R, property: KProperty<*>, value: T) {
        p.set(value)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  inline fun <reified T : Any?> prop(defaultValue: T = null as T) = propertyDelegate(T::class, Property::class as KClass<Property<T>>, defaultValue)

  @Suppress("UNCHECKED_CAST")
  inline fun <reified T : Any?, reified P : Property<T>> ext(defaultValue: T = null as T) = propertyDelegate(T::class, P::class,defaultValue)

  @Suppress("UNCHECKED_CAST")
  inline operator fun <reified T : Any?> invoke(defaultValue: T = null as T) = prop(defaultValue)
}
