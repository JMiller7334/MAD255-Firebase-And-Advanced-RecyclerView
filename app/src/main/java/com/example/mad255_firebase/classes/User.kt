package com.example.mad255_firebase.classes

/*NOTES ON DATA CLASSES.
must be declared with at least one primary constructor parameter that
is a val or var

reduces boiler platecode as constructor is delcared inline with the
class name.

cannot be extended by another class - these are final.

cannot be sealed, open, abstract, or inner.
*/
data class User(
    var id: String,
    var firstName: String?,
    var lastName: String?,
    var address: String?,
    var email: String?)