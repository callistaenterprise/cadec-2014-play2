package util

import views.html.helper.FieldConstructor

import views.html.twitterBootstrapInput

/**
 *
 */
object ViewHelper {
    implicit val myFields = FieldConstructor(twitterBootstrapInput.f)
}
