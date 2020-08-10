package io.opencubes.boxlin.adapter

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class FunctionalMod(val value: String = IMPLIED) {
  companion object {
    const val IMPLIED = "{IMPLIED_NAME_FROM_FUNCTION_NAME}"
  }
}
